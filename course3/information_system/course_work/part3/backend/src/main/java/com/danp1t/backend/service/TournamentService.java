package com.danp1t.backend.service;

import com.danp1t.backend.dto.TournamentDTO;
import com.danp1t.backend.model.Tournament;
import com.danp1t.backend.repository.TournamentRepository;
import com.danp1t.backend.repository.RangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
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
                tournament.getRang().getName(),
                tournament.getArchived(),
                tournament.getMinimalAge()
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
        tournament.setArchived(dto.getArchived() != null ? dto.getArchived() : false);
        tournament.setMinimalAge(dto.getMinimalAge());
        return tournament;
    }

    public List<TournamentDTO> findAll(String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return tournamentRepository.findAll(sort).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TournamentDTO> findAll() {
        return findAll("startDate", "desc");
    }

    public Optional<TournamentDTO> findById(Integer id) {
        return tournamentRepository.findById(id).map(this::toDTO);
    }

    public TournamentDTO save(TournamentDTO tournamentDTO) {
        Tournament tournament = toEntity(tournamentDTO);
        rangRepository.findById(tournamentDTO.getRangId()).ifPresent(tournament::setRang);
        LocalDateTime now = LocalDateTime.now();

        if (tournament.getFinishDate() != null && tournament.getFinishDate().isBefore(now)) {
            tournament.setArchived(true);
        } else {
            if (tournament.getArchived() == null) {
                tournament.setArchived(false);
            }
        }

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
        LocalDateTime now = LocalDateTime.now();

        if (tournament.getFinishDate() != null && tournament.getFinishDate().isBefore(now)) {
            tournament.setArchived(true);
        } else {
            Boolean archivedFromDTO = tournamentDTO.getArchived();
            if (archivedFromDTO != null) {
                tournament.setArchived(archivedFromDTO);
            } else {
                tournament.setArchived(false);
            }
        }

        Tournament updated = tournamentRepository.save(tournament);
        return toDTO(updated);
    }

    public void deleteById(Integer id) {
        if (!tournamentRepository.existsById(id)) {
            throw new RuntimeException("Tournament not found with id: " + id);
        }
        tournamentRepository.deleteById(id);
    }

    public List<TournamentDTO> findByNameContaining(String name, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return tournamentRepository.findByNameContainingIgnoreCase(name, sort).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<TournamentDTO> findByRangId(Integer rangId, String sortBy, String sortDirection) {
        Sort sort = createSort(sortBy, sortDirection);
        return tournamentRepository.findByRangId(rangId, sort).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void archiveOldTournaments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime seasonEnd;
        if (now.getMonthValue() >= 9) {
            seasonEnd = LocalDateTime.of(now.getYear(), 8, 31, 23, 59, 59);
        } else {
            seasonEnd = LocalDateTime.of(now.getYear() - 1, 8, 31, 23, 59, 59);
        }

        tournamentRepository.archiveOldTournaments(seasonEnd);
    }

    public void manualArchiveOldTournaments() {
        archiveOldTournaments();
    }

    private Sort createSort(String sortBy, String sortDirection) {
        if (sortBy == null) {
            return Sort.by(Sort.Direction.DESC, "startDate");
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if ("desc".equalsIgnoreCase(sortDirection)) {
            direction = Sort.Direction.DESC;
        }

        switch (sortBy.toLowerCase()) {
            case "name":
                return Sort.by(direction, "name");
            case "startdate":
            case "start_date":
                return Sort.by(direction, "startDate");
            case "minimalage":
            case "minimal_age":
                return Sort.by(direction, "minimalAge");
            case "rang":
            case "rangname":
            case "rang_name":
                return Sort.by(direction, "rang.name");
            default:
                return Sort.by(Sort.Direction.DESC, "startDate");
        }
    }
}