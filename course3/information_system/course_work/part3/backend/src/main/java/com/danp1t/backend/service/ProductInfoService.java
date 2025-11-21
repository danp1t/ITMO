package com.danp1t.backend.service;

import com.danp1t.backend.dto.ProductInfoDTO;
import com.danp1t.backend.model.ProductInfo;
import com.danp1t.backend.repository.ProductInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductInfoService {

    @Autowired
    private ProductInfoRepository productInfoRepository;

    private ProductInfoDTO toDTO(ProductInfo productInfo) {
        return new ProductInfoDTO(
                productInfo.getId(),
                productInfo.getProduct().getId(),
                productInfo.getProduct().getName(),
                productInfo.getSizeName(),
                productInfo.getCountItems()
        );
    }

    private ProductInfo toEntity(ProductInfoDTO dto) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setId(dto.getId());
        productInfo.setSizeName(dto.getSizeName());
        productInfo.setCountItems(dto.getCountItems());
        // Product устанавливается отдельно через ID
        return productInfo;
    }

    public List<ProductInfoDTO> findAll() {
        return productInfoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
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
        ProductInfo productInfo = toEntity(productInfoDTO);
        ProductInfo saved = productInfoRepository.save(productInfo);
        return toDTO(saved);
    }

    public ProductInfoDTO update(Integer id, ProductInfoDTO productInfoDTO) {
        if (!productInfoRepository.existsById(id)) {
            throw new RuntimeException("ProductInfo not found with id: " + id);
        }
        ProductInfo productInfo = toEntity(productInfoDTO);
        productInfo.setId(id);
        ProductInfo updated = productInfoRepository.save(productInfo);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!productInfoRepository.existsById(id)) {
            throw new RuntimeException("ProductInfo not found with id: " + id);
        }
        productInfoRepository.deleteById(id);
    }
}