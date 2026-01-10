package com.danp1t.backend.service;

import com.danp1t.backend.dto.OrderStatusDTO;
import com.danp1t.backend.model.OrderStatus;
import com.danp1t.backend.repository.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderStatusService {

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    private OrderStatusDTO toDTO(OrderStatus orderStatus) {
        return new OrderStatusDTO(orderStatus.getId(), orderStatus.getName(), orderStatus.getDescription());
    }

    private OrderStatus toEntity(OrderStatusDTO dto) {
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setId(dto.getId());
        orderStatus.setName(dto.getName());
        orderStatus.setDescription(dto.getDescription());
        return orderStatus;
    }

    @Transactional(readOnly = true)
    public List<OrderStatusDTO> findAll() {
        return orderStatusRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<OrderStatusDTO> findById(Integer id) {
        return orderStatusRepository.findById(id).map(this::toDTO);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public OrderStatusDTO save(OrderStatusDTO orderStatusDTO) {
        OrderStatus orderStatus = toEntity(orderStatusDTO);
        OrderStatus saved = orderStatusRepository.save(orderStatus);
        return toDTO(saved);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public OrderStatusDTO update(Integer id, OrderStatusDTO orderStatusDTO) {
        if (!orderStatusRepository.existsById(id)) {
            throw new RuntimeException("OrderStatus not found with id: " + id);
        }
        OrderStatus orderStatus = toEntity(orderStatusDTO);
        orderStatus.setId(id);
        OrderStatus updated = orderStatusRepository.save(orderStatus);
        return toDTO(updated);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteById(Integer id) {
        if (!orderStatusRepository.existsById(id)) {
            throw new RuntimeException("OrderStatus not found with id: " + id);
        }
        orderStatusRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return orderStatusRepository.existsByName(name);
    }
}