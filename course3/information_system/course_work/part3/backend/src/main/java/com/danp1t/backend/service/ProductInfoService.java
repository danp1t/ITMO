package com.danp1t.backend.service;

import com.danp1t.backend.dto.ProductInfoDTO;
import com.danp1t.backend.model.Product;
import com.danp1t.backend.model.ProductInfo;
import com.danp1t.backend.repository.ProductInfoRepository;
import com.danp1t.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductInfoService {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    @Autowired
    private ProductRepository productRepository;

    private ProductInfoDTO toDTO(ProductInfo productInfo) {
        return new ProductInfoDTO(
                productInfo.getId(),
                productInfo.getProduct().getId(),
                productInfo.getProduct().getName(),
                productInfo.getSizeName(),
                productInfo.getCountItems(),
                productInfo.getPrice()
        );
    }

    public List<ProductInfoDTO> findAll() {
        return productInfoRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<ProductInfoDTO> findById(Integer id) {
        return productInfoRepository.findById(id).map(this::toDTO);
    }

    public List<ProductInfoDTO> findByProductId(Integer productId) {
        return productInfoRepository.findByProductId(productId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ProductInfoDTO> findBySizeName(String sizeName) {
        return productInfoRepository.findBySizeName(sizeName).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ProductInfoDTO save(ProductInfoDTO productInfoDTO) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setSizeName(productInfoDTO.getSizeName());
        productInfo.setCountItems(productInfoDTO.getCountItems());
        productInfo.setPrice(productInfoDTO.getPrice());

        // Set Product
        Product product = productRepository.findById(productInfoDTO.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productInfoDTO.getProductId()));
        productInfo.setProduct(product);

        ProductInfo saved = productInfoRepository.save(productInfo);
        return toDTO(saved);
    }

    public ProductInfoDTO update(Integer id, ProductInfoDTO productInfoDTO) {
        ProductInfo existingProductInfo = productInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ProductInfo not found with id: " + id));

        existingProductInfo.setSizeName(productInfoDTO.getSizeName());
        existingProductInfo.setCountItems(productInfoDTO.getCountItems());

        ProductInfo updated = productInfoRepository.save(existingProductInfo);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!productInfoRepository.existsById(id)) {
            throw new RuntimeException("ProductInfo not found with id: " + id);
        }
        productInfoRepository.deleteById(id);
    }
}