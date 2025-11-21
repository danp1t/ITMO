package com.danp1t.backend.service;

import com.danp1t.backend.dto.TournamentDTO;
import com.danp1t.backend.model.Tournament;
import com.danp1t.backend.repository.TournamentRepository;
import com.danp1t.backend.repository.RangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private RangRepository rangRepository;

    private TournamentDTO toDTO(Tournament tournament) {
        return new TournamentDTO(
                tournament.getId(),
                tournament.getName(),
                tournament.getStartDate(),
                tournament.getFinishDate(),
                tournament.getAddress(),
                tournament.getLink(),
                tournament.getRang().getId(),
                tournament.getRang().getName()
        );
    }

    private Tournament toEntity(TournamentDTO dto) {
        Tournament tournament = new Tournament();
        tournament.setId(dto.getId());
        tournament.setName(dto.getName());
        tournament.setStartDate(dto.getStartDate());
        tournament.setFinishDate(dto.getFinishDate());
        tournament.setAddress(dto.getAddress());
        tournament.setLink(dto.getLink());
        // Rang устанавливается отдельно через ID
        return tournament;
    }

    public List<TournamentDTO> findAll() {
        return tournamentRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public Optional<TournamentDTO> findById(Integer id) {
        return tournamentRepository.findById(id).map(this::toDTO);
    }

    public TournamentDTO save(TournamentDTO tournamentDTO) {
        Tournament tournament = toEntity(tournamentDTO);
        // Установка Rang по ID
        rangRepository.findById(tournamentDTO.getRangId()).ifPresent(tournament::setRang);
        Tournament saved = tournamentRepository.save(tournament);
        return toDTO(saved);
    }

    public TournamentDTO update(Integer id, TournamentDTO tournamentDTO) {
        if (!tournamentRepository.existsById(id)) {
            throw new RuntimeException("Tournament not found with id: " + id);
        }
        Tournament tournament = toEntity(tournamentDTO);
        tournament.setId(id);
        rangRepository.findById(tournamentDTO.getRangId()).ifPresent(tournament::setRang);
        Tournament updated = tournamentRepository.save(tournament);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!tournamentRepository.existsById(id)) {
            throw new RuntimeException("Tournament not found with id: " + id);
        }
        tournamentRepository.deleteById(id);
    }

    public List<TournamentDTO> findByNameContaining(String name) {
        return tournamentRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TournamentDTO> findByRangId(Integer rangId) {
        return tournamentRepository.findByRangId(rangId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}