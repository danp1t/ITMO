package com.danp1t.backend.repository;

import com.danp1t.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByName(String name);
    boolean existsByName(String name);
    List<Product> findByNameContainingIgnoreCase(String name);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productInfos WHERE p.id = :id")
    Optional<Product> findByIdWithProductInfos(@Param("id") Integer id);
}