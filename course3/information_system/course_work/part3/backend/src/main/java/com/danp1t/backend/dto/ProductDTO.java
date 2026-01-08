package com.danp1t.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Integer id;
    private String name;
    private String description;
    private String category;
    private Integer basePrice;
    private Integer popularity;
    private List<String> images;

    @JsonProperty("productInfos")
    private List<ProductInfoDTO> productInfos;

}