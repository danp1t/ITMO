package com.danp1t.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category")
    private String category;

    @Column(name = "base_price")
    private Integer basePrice;

    @Column(name = "popularity")
    private Integer popularity = 0;

    @Column(name = "images", columnDefinition = "TEXT")
    private String images;

    @ManyToMany(mappedBy = "products")
    private List<Order> orders;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductInfo> productInfos;

    public List<String> getImagesList() {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return List.of(images.split(","));
    }
}