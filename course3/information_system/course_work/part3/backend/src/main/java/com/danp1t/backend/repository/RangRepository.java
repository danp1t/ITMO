package com.danp1t.backend.repository;

import com.danp1t.backend.model.Rang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RangRepository extends JpaRepository<Rang, Integer> {
    Optional<Rang> findByName(String name);
    boolean existsByName(String name);
}