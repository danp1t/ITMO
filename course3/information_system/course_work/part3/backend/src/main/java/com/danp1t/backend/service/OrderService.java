package com.danp1t.backend.service;

import com.danp1t.backend.dto.OrderDTO;
import com.danp1t.backend.dto.ProductDTO;
import com.danp1t.backend.model.Order;
import com.danp1t.backend.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    private OrderDTO toDTO(Order order) {
        List<ProductDTO> products = order.getProducts().stream()
                .map(product -> new ProductDTO(product.getId(), product.getName(), product.getDescription()))
                .collect(Collectors.toList());

        return new OrderDTO(
                order.getId(),
                order.getAddress(),
                order.getCreatedAt(),
                order.getTotalAmount(),
                order.getAccount().getId(),
                order.getAccount().getName(),
                order.getOrderStatus().getId(),
                order.getOrderStatus().getName(),
                products
        );
    }

    private Order toEntity(OrderDTO dto) {
        Order order = new Order();
        order.setId(dto.getId());
        order.setAddress(dto.getAddress());
        order.setCreatedAt(dto.getCreatedAt());
        order.setTotalAmount(dto.getTotalAmount());
        // Account, OrderStatus и Products устанавливаются отдельно через ID
        return order;
    }

    public List<OrderDTO> findAll() {
        return orderRepository.findAllWithDetails().stream().map(this::toDTO).collect(Collectors.toList());
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

    public OrderDTO save(OrderDTO orderDTO) {
        Order order = toEntity(orderDTO);
        Order saved = orderRepository.save(order);
        return toDTO(saved);
    }

    public OrderDTO update(Integer id, OrderDTO orderDTO) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        Order order = toEntity(orderDTO);
        order.setId(id);
        Order updated = orderRepository.save(order);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }
}