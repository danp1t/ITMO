package com.danp1t.backend.repository;

import com.danp1t.backend.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    List<Tournament> findByNameContainingIgnoreCase(String name);
    List<Tournament> findByRangId(Integer rangId);

    @Query("SELECT t FROM Tournament t LEFT JOIN FETCH t.rang WHERE t.id = :id")
    Optional<Tournament> findByIdWithRang(@Param("id") Integer id);
}