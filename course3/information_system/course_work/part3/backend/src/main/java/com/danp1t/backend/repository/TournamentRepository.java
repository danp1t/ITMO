package com.danp1t.backend.repository;

import com.danp1t.backend.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Integer> {
    List<Tournament> findAll(Sort sort);
    List<Tournament> findByNameContainingIgnoreCase(String name, Sort sort);
    List<Tournament> findByRangId(Integer rangId, Sort sort);

    @Modifying
    @Transactional
    @Query("UPDATE Tournament t SET t.archived = true WHERE t.finishDate < :seasonEnd AND t.archived = false")
    void archiveOldTournaments(@Param("seasonEnd") LocalDateTime seasonEnd);

}