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

    // Добавляем поле для изображений (храним как JSON)
    @Column(name = "images", columnDefinition = "TEXT")
    private String images;

    @ManyToMany(mappedBy = "products")
    private List<Order> orders;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductInfo> productInfos;

    // Геттер для преобразования строки в список
    public List<String> getImagesList() {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        // В реальном приложении нужно парсить JSON
        return List.of(images.split(","));
    }

    // Сеттер для сохранения списка как строки
    public void setImagesList(List<String> imagesList) {
        if (imagesList == null || imagesList.isEmpty()) {
            this.images = null;
        } else {
            this.images = String.join(",", imagesList);
        }
    }
}