package com.danp1t.backend.repository;

import com.danp1t.backend.model.Product;
import com.danp1t.backend.model.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    boolean existsByName(String name);
    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productInfos WHERE p.id = :id")
    Optional<Product> findByIdWithProductInfos(@Param("id") Integer id);

    @Modifying
    @Query(value = "INSERT INTO \"ProductInfo\" (product_id, size_name, count_items, price) VALUES (:#{#productInfo.product.id}, :#{#productInfo.sizeName}, :#{#productInfo.countItems}, :#{#productInfo.price})", nativeQuery = true)
    void saveProductInfo(@Param("productInfo") ProductInfo productInfo);
}