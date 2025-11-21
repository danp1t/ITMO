package com.danp1t.backend.controller;

import com.danp1t.backend.dto.RangDTO;
import com.danp1t.backend.service.RangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rangs")
@CrossOrigin(origins = "*")
public class RangController {

    @Autowired
    private RangService rangService;

    @GetMapping
    public ResponseEntity<List<RangDTO>> getAllRangs() {
        return ResponseEntity.ok(rangService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RangDTO> getRangById(@PathVariable Integer id) {
        return rangService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RangDTO> createRang(@RequestBody RangDTO rangDTO) {
        if (rangService.existsByName(rangDTO.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        RangDTO created = rangService.save(rangDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RangDTO> updateRang(@PathVariable Integer id, @RequestBody RangDTO rangDTO) {
        try {
            RangDTO updated = rangService.update(id, rangDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRang(@PathVariable Integer id) {
        try {
            rangService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}