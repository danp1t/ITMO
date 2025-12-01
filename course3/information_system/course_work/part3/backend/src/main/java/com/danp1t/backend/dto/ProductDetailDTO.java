package com.danp1t.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private Integer id;
    private String name;
    private String description;
    private String category;
    private Integer basePrice;
    private Integer popularity;
    private List<ProductInfoDTO> productInfos;
    private Boolean inStock;
    private String availabilityMessage;
}