package com.danp1t.backend.repository;

import com.danp1t.backend.model.Rang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RangRepository extends JpaRepository<Rang, Integer> {
    boolean existsByName(String name);

    @Query(value = "SELECT insert_rang(:name, :description)",
            nativeQuery = true)
    Integer callInsertRangFunction(@Param("name") String name,
                                   @Param("description") String description);
}