package com.danp1t.backend.service;

import com.danp1t.backend.dto.OrderDTO;
import com.danp1t.backend.dto.ProductDTO;
import com.danp1t.backend.dto.OrderItemDTO;
import com.danp1t.backend.model.*;
import com.danp1t.backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDTO toDTO(Order order) {
        OrderDTO dto = new OrderDTO();

        dto.setId(order.getId());
        dto.setAddress(order.getAddress());
        dto.setPhone(order.getPhone());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setEmail(order.getEmail());
        dto.setCustomerName(order.getCustomerName());
        dto.setDeliveryMethod(order.getDeliveryMethod());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPostalCode(order.getPostalCode());
        dto.setNotes(order.getNotes());

        if (order.getAccount() != null) {
            dto.setAccountId(order.getAccount().getId());
            dto.setAccountName(order.getAccount().getName());
        }

        if (order.getOrderStatus() != null) {
            dto.setOrderStatusId(order.getOrderStatus().getId());
            dto.setOrderStatusName(order.getOrderStatus().getName());
        }

        List<ProductDTO> products = (order.getProducts() != null) ?
                order.getProducts().stream()
                        .map(product -> new ProductDTO(
                                product.getId(),
                                product.getName(),
                                product.getDescription(),
                                product.getCategory(),
                                product.getBasePrice(),
                                product.getPopularity(),
                                product.getImagesList(),
                                null))
                        .collect(Collectors.toList()) :
                new ArrayList<>();
        dto.setProducts(products);

        if (order.getOrderItemsJson() != null && !order.getOrderItemsJson().isEmpty()) {
            try {
                List<OrderItemDTO> orderProducts = objectMapper.readValue(
                        order.getOrderItemsJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, OrderItemDTO.class)
                );
                dto.setOrderProducts(orderProducts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return dto;
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setAddress(orderDTO.getAddress());
        order.setPhone(orderDTO.getPhone());
        order.setEmail(orderDTO.getEmail());
        order.setCustomerName(orderDTO.getCustomerName());
        order.setCreatedAt(orderDTO.getCreatedAt() != null ?
                orderDTO.getCreatedAt() : java.time.LocalDateTime.now());
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setDeliveryMethod(orderDTO.getDeliveryMethod());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPostalCode(orderDTO.getPostalCode());
        order.setNotes(orderDTO.getNotes());

        if (orderDTO.getOrderProducts() != null) {
            try {
                String orderItemsJson = objectMapper.writeValueAsString(orderDTO.getOrderProducts());
                order.setOrderItemsJson(orderItemsJson);
            } catch (Exception e) {
                throw new RuntimeException("Error serializing order items", e);
            }
        }

        OrderStatus pendingStatus = orderStatusRepository.findByName("pending")
                .orElseThrow(() -> new RuntimeException("Pending status not found"));
        order.setOrderStatus(pendingStatus);

        Account account = accountRepository.findById(orderDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        order.setAccount(account);

        if (orderDTO.getOrderProducts() != null && !orderDTO.getOrderProducts().isEmpty()) {
            List<Integer> productIds = orderDTO.getOrderProducts().stream()
                    .map(OrderItemDTO::getProductId)
                    .distinct()
                    .collect(Collectors.toList());

            if (!productIds.isEmpty()) {
                List<Product> products = productRepository.findAllById(productIds);
                order.setProducts(products);
            }
        }

        if (orderDTO.getOrderProducts() != null && !orderDTO.getOrderProducts().isEmpty()) {
            updateProductStock(orderDTO.getOrderProducts());
        }

        Order saved = orderRepository.save(order);

        Order loadedOrder = orderRepository.findByIdWithDetails(saved.getId())
                .orElseThrow(() -> new RuntimeException("Order not found after creation"));

        return toDTO(loadedOrder);
    }

    private void updateProductStock(List<OrderItemDTO> orderItems) {
        for (OrderItemDTO orderItem : orderItems) {
            ProductInfo productInfo = productInfoRepository.findById(orderItem.getProductInfoId())
                    .orElseThrow(() -> new RuntimeException("ProductInfo not found with id: " + orderItem.getProductInfoId()));

            if (productInfo.getCountItems() < orderItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + orderItem.getProductName() +
                        ", size: " + orderItem.getSize());
            }

            productInfo.setCountItems(productInfo.getCountItems() - orderItem.getQuantity());
            productInfoRepository.save(productInfo);
        }
    }

    public List<OrderDTO> findAll() {
        return orderRepository.findAllWithDetails().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<OrderDTO> findById(Integer id) {
        return orderRepository.findByIdWithDetails(id).map(this::toDTO);
    }

    public List<OrderDTO> findByAccountId(Integer accountId) {
        return orderRepository.findByAccountId(accountId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> findByOrderStatusId(Integer orderStatusId) {
        return orderRepository.findByOrderStatusId(orderStatusId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO update(Integer id, OrderDTO orderDTO) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        existingOrder.setAddress(orderDTO.getAddress());
        existingOrder.setPhone(orderDTO.getPhone());
        existingOrder.setEmail(orderDTO.getEmail());
        existingOrder.setCustomerName(orderDTO.getCustomerName());
        existingOrder.setTotalAmount(orderDTO.getTotalAmount());
        existingOrder.setDeliveryMethod(orderDTO.getDeliveryMethod());
        existingOrder.setPaymentMethod(orderDTO.getPaymentMethod());
        existingOrder.setPostalCode(orderDTO.getPostalCode());
        existingOrder.setNotes(orderDTO.getNotes());

        if (!existingOrder.getOrderStatus().getId().equals(orderDTO.getOrderStatusId())) {
            OrderStatus status = orderStatusRepository.findById(orderDTO.getOrderStatusId())
                    .orElseThrow(() -> new RuntimeException("OrderStatus not found"));
            existingOrder.setOrderStatus(status);
        }

        Order updated = orderRepository.save(existingOrder);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found");
        }
        orderRepository.deleteById(id);
    }
}