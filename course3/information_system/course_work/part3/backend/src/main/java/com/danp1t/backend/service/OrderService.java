package com.danp1t.backend.service;

import com.danp1t.backend.dto.OrderDTO;
import com.danp1t.backend.dto.ProductDTO;
import com.danp1t.backend.model.*;
import com.danp1t.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private OrderDTO toDTO(Order order) {
        List<ProductDTO> products = order.getProducts().stream()
                .map(product -> new ProductDTO(
                        product.getId(),
                        product.getName(),
                        product.getDescription(),
                        product.getCategory(),
                        product.getBasePrice(),
                        product.getPopularity()))
                .collect(Collectors.toList());

        return new OrderDTO(
                order.getId(),
                order.getAddress(),
                order.getPhone(), // IS15: добавляем телефон
                order.getCreatedAt(),
                order.getTotalAmount(),
                order.getAccount().getId(),
                order.getAccount().getName(),
                order.getOrderStatus().getId(),
                order.getOrderStatus().getName(),
                products
        );
    }

    // IS15: Создание заказа с телефоном
    public OrderDTO createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        order.setAddress(orderDTO.getAddress());
        order.setPhone(orderDTO.getPhone()); // Сохраняем телефон
        order.setCreatedAt(orderDTO.getCreatedAt() != null ?
                orderDTO.getCreatedAt() : java.time.LocalDateTime.now());
        order.setTotalAmount(orderDTO.getTotalAmount());

        // Устанавливаем статус "pending" по умолчанию
        OrderStatus pendingStatus = orderStatusRepository.findByName("pending")
                .orElseThrow(() -> new RuntimeException("Pending status not found"));
        order.setOrderStatus(pendingStatus);

        // Устанавливаем аккаунт
        Account account = accountRepository.findById(orderDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        order.setAccount(account);

        // Устанавливаем продукты
        if (orderDTO.getProducts() != null && !orderDTO.getProducts().isEmpty()) {
            List<Product> products = orderDTO.getProducts().stream()
                    .map(productDTO -> productRepository.findById(productDTO.getId())
                            .orElseThrow(() -> new RuntimeException("Product not found")))
                    .collect(Collectors.toList());
            order.setProducts(products);
        }

        // Уменьшаем количество товаров на складе
        updateProductStock(orderDTO);

        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    // Обновление остатков товаров
    private void updateProductStock(OrderDTO orderDTO) {
        // Здесь должна быть логика уменьшения количества товаров
        // В реальном приложении нужно учитывать конкретные размеры и количество
        // Для упрощения уменьшаем на 1 каждый товар
        orderDTO.getProducts().forEach(productDTO -> {
            productRepository.findById(productDTO.getId()).ifPresent(product -> {
                if (product.getProductInfos() != null && !product.getProductInfos().isEmpty()) {
                    // Уменьшаем первый доступный размер
                    product.getProductInfos().stream()
                            .filter(pi -> pi.getCountItems() > 0)
                            .findFirst()
                            .ifPresent(pi -> {
                                pi.setCountItems(pi.getCountItems() - 1);
                                productInfoRepository.save(pi);
                            });
                }
            });
        });
    }

    // Остальные методы остаются
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
        existingOrder.setPhone(orderDTO.getPhone()); // Обновляем телефон
        existingOrder.setTotalAmount(orderDTO.getTotalAmount());

        // Обновляем статус если изменился
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