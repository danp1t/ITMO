package com.danp1t.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private Integer id;
    private String name;
    private String description;
    private String category;
    private Integer basePrice;
    private Integer popularity;
    private List<String> images;
    private List<ProductInfoDTO> productInfos;
    private Boolean inStock;
    private String availabilityMessage;
}