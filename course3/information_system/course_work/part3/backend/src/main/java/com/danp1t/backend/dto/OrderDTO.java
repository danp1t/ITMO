package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Integer id;
    private String address;
    private LocalDateTime createdAt;
    private Integer totalAmount;
    private Integer accountId;
    private String accountName;
    private Integer orderStatusId;
    private String orderStatusName;
    private List<ProductDTO> products;
}