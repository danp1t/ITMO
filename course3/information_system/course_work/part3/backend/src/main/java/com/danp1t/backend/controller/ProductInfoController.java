package com.danp1t.backend.controller;

import com.danp1t.backend.dto.ProductInfoDTO;
import com.danp1t.backend.service.ProductInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/product-infos")
@CrossOrigin(origins = "*")
public class ProductInfoController {

    @Autowired
    private ProductInfoService productInfoService;

    @GetMapping
    public ResponseEntity<List<ProductInfoDTO>> getAllProductInfos() {
        return ResponseEntity.ok(productInfoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductInfoDTO> getProductInfoById(@PathVariable Integer id) {
        return productInfoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductInfoDTO>> getProductInfosByProduct(@PathVariable Integer productId) {
        return ResponseEntity.ok(productInfoService.findByProductId(productId));
    }

    @GetMapping("/size/{sizeName}")
    public ResponseEntity<List<ProductInfoDTO>> getProductInfosBySize(@PathVariable String sizeName) {
        return ResponseEntity.ok(productInfoService.findBySizeName(sizeName));
    }

    @PostMapping
    public ResponseEntity<ProductInfoDTO> createProductInfo(@RequestBody ProductInfoDTO productInfoDTO) {
        ProductInfoDTO created = productInfoService.save(productInfoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductInfoDTO> updateProductInfo(@PathVariable Integer id, @RequestBody ProductInfoDTO productInfoDTO) {
        try {
            ProductInfoDTO updated = productInfoService.update(id, productInfoDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductInfo(@PathVariable Integer id) {
        try {
            productInfoService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}