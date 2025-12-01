package com.danp1t.backend.service;

import com.danp1t.backend.dto.ProductDTO;
import com.danp1t.backend.dto.ProductDetailDTO;
import com.danp1t.backend.dto.ProductInfoDTO;
import com.danp1t.backend.model.Product;
import com.danp1t.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private ProductDTO toDTO(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getCategory(), product.getBasePrice(), product.getPopularity());
    }

    private ProductDetailDTO toDetailDTO(Product product) {
        List<ProductInfoDTO> productInfos = product.getProductInfos().stream()
                .map(productInfo -> new ProductInfoDTO(
                        productInfo.getId(),
                        productInfo.getProduct().getId(),
                        productInfo.getProduct().getName(),
                        productInfo.getSizeName(),
                        productInfo.getCountItems(),
                        productInfo.getPrice()
                ))
                .collect(Collectors.toList());

        // IS12: Проверяем наличие товара
        boolean inStock = product.getProductInfos().stream()
                .anyMatch(pi -> pi.getCountItems() > 0);

        String availabilityMessage;
        if (!inStock) {
            availabilityMessage = "Товар отсутствует на складе";
        } else {
            long availableSizes = product.getProductInfos().stream()
                    .filter(pi -> pi.getCountItems() > 0)
                    .count();
            availabilityMessage = "Товар доступен в " + availableSizes + " размерах";
        }

        return new ProductDetailDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getBasePrice(),
                product.getPopularity(),
                productInfos,
                inStock,
                availabilityMessage
        );
    }

    // IS01, IS03, IS04: Получение товаров с сортировкой и фильтрацией
    public List<ProductDetailDTO> findAllWithFilters(String category, String size, String sortBy, String sortOrder) {
        List<Product> products = productRepository.findAll();

        // Фильтрация по категории (IS04)
        if (category != null && !category.isEmpty()) {
            products = products.stream()
                    .filter(p -> category.equalsIgnoreCase(p.getCategory()))
                    .collect(Collectors.toList());
        }

        // Фильтрация по размеру (IS04)
        if (size != null && !size.isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getProductInfos().stream()
                            .anyMatch(pi -> size.equalsIgnoreCase(pi.getSizeName())))
                    .collect(Collectors.toList());
        }

        // Сортировка (IS03)
        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Product> comparator = getComparator(sortBy);

            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }

            products = products.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return products.stream()
                .map(this::toDetailDTO)
                .collect(Collectors.toList());
    }

    private Comparator<Product> getComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "name":
                return Comparator.comparing(Product::getName);
            case "popularity":
                return Comparator.comparing(Product::getPopularity);
            case "price":
                return Comparator.comparing(Product::getBasePrice);
            default:
                return Comparator.comparing(Product::getId);
        }
    }

    // IS12: Проверка наличия товара
    public boolean isProductAvailable(Integer productId, String size, Integer quantity) {
        return productRepository.findById(productId)
                .map(product -> product.getProductInfos().stream()
                        .filter(pi -> size == null || size.equals(pi.getSizeName()))
                        .anyMatch(pi -> pi.getCountItems() >= quantity))
                .orElse(false);
    }

    // IS12: Получение информации о наличии
    public String getAvailabilityMessage(Integer productId) {
        return productRepository.findById(productId)
                .map(product -> {
                    long availableSizes = product.getProductInfos().stream()
                            .filter(pi -> pi.getCountItems() > 0)
                            .count();

                    if (availableSizes == 0) {
                        return "Товар отсутствует на складе";
                    } else {
                        return "Товар доступен в " + availableSizes + " размерах";
                    }
                })
                .orElse("Товар не найден");
    }

    // IS14: Получение остатков для админов
    public List<ProductInfoDTO> getStockInfo() {
        return productRepository.findAll().stream()
                .flatMap(product -> product.getProductInfos().stream()
                        .map(pi -> new ProductInfoDTO(
                                pi.getId(),
                                pi.getProduct().getId(),
                                pi.getProduct().getName(),
                                pi.getSizeName(),
                                pi.getCountItems(),
                                pi.getPrice()
                        )))
                .collect(Collectors.toList());
    }

    // IS15: Увеличение популярности при просмотре
    public void incrementPopularity(Integer productId) {
        productRepository.findById(productId)
                .ifPresent(product -> {
                    product.setPopularity(product.getPopularity() + 1);
                    productRepository.save(product);
                });
    }

    // Остальные методы остаются без изменений
    public List<ProductDTO> findAll() {
        return productRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<ProductDTO> findById(Integer id) {
        return productRepository.findById(id).map(this::toDTO);
    }

    public Optional<ProductDetailDTO> findByIdWithProductInfos(Integer id) {
        return productRepository.findByIdWithProductInfos(id).map(this::toDetailDTO);
    }

    public ProductDTO save(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    public ProductDTO update(Integer id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        Product updated = productRepository.save(product);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    public List<ProductDTO> findByNameContaining(String name) {
        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}