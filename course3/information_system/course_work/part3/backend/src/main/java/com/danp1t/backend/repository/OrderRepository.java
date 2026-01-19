package com.danp1t.backend.repository;

import com.danp1t.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
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
}