package com.danp1t.backend.service;

import com.danp1t.backend.dto.RangDTO;
import com.danp1t.backend.model.Rang;
import com.danp1t.backend.repository.RangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RangService {

    @Autowired
    private RangRepository rangRepository;

    private RangDTO toDTO(Rang rang) {
        return new RangDTO(rang.getId(), rang.getName(), rang.getDescription());
    }

    private Rang toEntity(RangDTO dto) {
        Rang rang = new Rang();
        rang.setId(dto.getId());
        rang.setName(dto.getName());
        rang.setDescription(dto.getDescription());
        return rang;
    }

    @Transactional(readOnly = true)
    public List<RangDTO> findAll() {
        return rangRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<RangDTO> findById(Integer id) {
        return rangRepository.findById(id).map(this::toDTO);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public RangDTO save(RangDTO rangDTO) {
        try {
            Integer newId = rangRepository.callInsertRangFunction(
                    rangDTO.getName(),
                    rangDTO.getDescription()
            );

            if (newId == null || newId <= 0) {
                throw new RuntimeException("Failed to insert rang");
            }

            Rang savedRang = rangRepository.findById(newId)
                    .orElseThrow(() -> new RuntimeException("Rang not found after insertion"));

            return toDTO(savedRang);

        } catch (Exception e) {
            if (e.getMessage().contains("already exists")) {
                throw new RuntimeException("Rang with name '" + rangDTO.getName() + "' already exists");
            }
            throw new RuntimeException("Error saving rang: " + e.getMessage());
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public RangDTO update(Integer id, RangDTO rangDTO) {
        if (!rangRepository.existsById(id)) {
            throw new RuntimeException("Rang not found with id: " + id);
        }
        Rang rang = toEntity(rangDTO);
        rang.setId(id);
        Rang updated = rangRepository.save(rang);
        return toDTO(updated);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteById(Integer id) {
        if (!rangRepository.existsById(id)) {
            throw new RuntimeException("Rang not found with id: " + id);
        }
        rangRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return rangRepository.existsByName(name);
    }
}