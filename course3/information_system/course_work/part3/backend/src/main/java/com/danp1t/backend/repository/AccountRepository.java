package com.danp1t.backend.repository;

import com.danp1t.backend.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.roles WHERE a.id = :id")
    Optional<Account> findByIdWithRoles(@Param("id") Integer id);

    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.posts LEFT JOIN FETCH a.comments LEFT JOIN FETCH a.roles WHERE a.id = :id")
    Optional<Account> findByIdWithDetails(@Param("id") Integer id);
}