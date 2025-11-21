package com.danp1t.backend.controller;

import com.danp1t.backend.dto.TournamentDTO;
import com.danp1t.backend.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@CrossOrigin(origins = "*")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping
    public ResponseEntity<List<TournamentDTO>> getAllTournaments() {
        return ResponseEntity.ok(tournamentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TournamentDTO> getTournamentById(@PathVariable Integer id) {
        return tournamentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TournamentDTO>> searchTournaments(@RequestParam String name) {
        return ResponseEntity.ok(tournamentService.findByNameContaining(name));
    }

    @GetMapping("/rang/{rangId}")
    public ResponseEntity<List<TournamentDTO>> getTournamentsByRang(@PathVariable Integer rangId) {
        return ResponseEntity.ok(tournamentService.findByRangId(rangId));
    }

    @PostMapping
    public ResponseEntity<TournamentDTO> createTournament(@RequestBody TournamentDTO tournamentDTO) {
        TournamentDTO created = tournamentService.save(tournamentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TournamentDTO> updateTournament(@PathVariable Integer id, @RequestBody TournamentDTO tournamentDTO) {
        try {
            TournamentDTO updated = tournamentService.update(id, tournamentDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTournament(@PathVariable Integer id) {
        try {
            tournamentService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}