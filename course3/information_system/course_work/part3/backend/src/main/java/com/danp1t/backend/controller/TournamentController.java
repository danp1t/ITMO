package com.danp1t.backend.controller;

import com.danp1t.backend.dto.TournamentDTO;
import com.danp1t.backend.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "*")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getAllTournaments(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        return ResponseEntity.ok(tournamentService.findAll(sortBy, sortDirection));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getTournamentById(@PathVariable Integer id) {
        return tournamentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TournamentDTO>> searchTournaments(
            @RequestParam String name,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        return ResponseEntity.ok(tournamentService.findByNameContaining(name, sortBy, sortDirection));
    }

    @GetMapping("/rang/{rangId}")
    public ResponseEntity<List<TournamentDTO>> getTournamentsByRang(
            @PathVariable Integer rangId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        return ResponseEntity.ok(tournamentService.findByRangId(rangId, sortBy, sortDirection));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('OAPI:ROLE:PublishTournament')")
    public ResponseEntity<TournamentDTO> createTournament(@RequestBody TournamentDTO tournamentDTO) {
        TournamentDTO created = tournamentService.save(tournamentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OAPI:ROLE:EditTournament')")
    public ResponseEntity<TournamentDTO> updateTournament(@PathVariable Integer id, @RequestBody TournamentDTO tournamentDTO) {
        try {
            TournamentDTO updated = tournamentService.update(id, tournamentDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OAPI:ROLE:DeleteTournament')")
    public ResponseEntity<Void> deleteTournament(@PathVariable Integer id) {
        try {
            tournamentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/archive-old")
    @PreAuthorize("hasAuthority('OAPI:ROLE:EditTournament')")
    public ResponseEntity<Void> archiveOldTournaments() {
        tournamentService.manualArchiveOldTournaments();
        return ResponseEntity.ok().build();
    }
}