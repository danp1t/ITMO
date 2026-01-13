package com.danp1t.backend.repository;

import com.danp1t.backend.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByAccountId(Integer accountId);
    List<Order> findByOrderStatusId(Integer orderStatusId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.account LEFT JOIN FETCH o.orderStatus LEFT JOIN FETCH o.products WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.account LEFT JOIN FETCH o.orderStatus ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetails();

    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findAccountById(@Param("id") Integer id);

    @Query("SELECT os FROM OrderStatus os WHERE os.id = :id")
    Optional<OrderStatus> findOrderStatusById(@Param("id") Integer id);

    @Query("SELECT os FROM OrderStatus os WHERE os.name = :name")
    Optional<OrderStatus> findOrderStatusByName(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findProductsByIds(@Param("ids") List<Integer> ids);

    @Query("SELECT pi FROM ProductInfo pi WHERE pi.id = :id")
    Optional<ProductInfo> findProductInfoById(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE ProductInfo pi SET pi.countItems = :countItems WHERE pi.id = :id")
    void updateProductInfoCount(@Param("id") Integer id, @Param("countItems") Integer countItems);

    @Query(value = "INSERT INTO product_info (product_id, size_name, count_items, price) VALUES (?, ?, ?, ?)", nativeQuery = true)
    void saveProductInfo(ProductInfo productInfo);
}