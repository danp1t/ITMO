package com.danp1t.backend.repository;

import com.danp1t.backend.model.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductInfoRepository extends JpaRepository<ProductInfo, Integer> {
    List<ProductInfo> findByProductId(Integer productId);
    List<ProductInfo> findBySizeName(String sizeName);
}