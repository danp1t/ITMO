package com.danp1t.backend.service;

import com.danp1t.backend.dto.ProductDTO;
import com.danp1t.backend.dto.ProductDetailDTO;
import com.danp1t.backend.dto.ProductInfoDTO;
import com.danp1t.backend.model.Product;
import com.danp1t.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    private ProductDTO toDTO(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getDescription());
    }

    private ProductDetailDTO toDetailDTO(Product product) {
        List<ProductInfoDTO> productInfos = product.getProductInfos().stream()
                .map(productInfo -> new ProductInfoDTO(
                        productInfo.getId(),
                        productInfo.getProduct().getId(),
                        productInfo.getProduct().getName(),
                        productInfo.getSizeName(),
                        productInfo.getCountItems()
                ))
                .collect(Collectors.toList());

        return new ProductDetailDTO(product.getId(), product.getName(), product.getDescription(), productInfos);
    }

    private Product toEntity(ProductDTO dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        return product;
    }

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
        Product product = toEntity(productDTO);
        Product saved = productRepository.save(product);
        return toDTO(saved);
    }

    public ProductDTO update(Integer id, ProductDTO productDTO) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        Product product = toEntity(productDTO);
        product.setId(id);
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