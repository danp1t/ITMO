package com.danp1t.backend.controller;

import com.danp1t.backend.dto.OrderStatusDTO;
import com.danp1t.backend.service.OrderStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/order-statuses")
@CrossOrigin(origins = "*")
public class OrderStatusController {

    @Autowired
    private OrderStatusService orderStatusService;

    @GetMapping
    public ResponseEntity<List<OrderStatusDTO>> getAllOrderStatuses() {
        return ResponseEntity.ok(orderStatusService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderStatusDTO> getOrderStatusById(@PathVariable Integer id) {
        return orderStatusService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderStatusDTO> createOrderStatus(@RequestBody OrderStatusDTO orderStatusDTO) {
        if (orderStatusService.existsByName(orderStatusDTO.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        OrderStatusDTO created = orderStatusService.save(orderStatusDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderStatusDTO> updateOrderStatus(@PathVariable Integer id, @RequestBody OrderStatusDTO orderStatusDTO) {
        try {
            OrderStatusDTO updated = orderStatusService.update(id, orderStatusDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderStatus(@PathVariable Integer id) {
        try {
            orderStatusService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}