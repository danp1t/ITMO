package com.danp1t.service;

import com.danp1t.bean.Coordinates;
import com.danp1t.repository.CoordinatesRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class CoordinatesService {

    @Inject
    private CoordinatesRepository coordinatesRepository;

    public Coordinates createCoordinates(Coordinates coordinates) {

        coordinates.validate();
        if (coordinates.getX() == null) {
            throw new IllegalArgumentException("X coordinate cannot be null");
        }
        if (coordinates.getY() == null) {
            throw new IllegalArgumentException("Y coordinate cannot be null");
        }

        try {
            Coordinates savedCoordinates = coordinatesRepository.save(coordinates);
            return savedCoordinates;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create coordinates: " + e.getMessage(), e);
        }
    }

    public List<Coordinates> getAllCoordinates() {
        try {
            List<Coordinates> coordinates = coordinatesRepository.findAll();
            return coordinates;
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve coordinates: " + e.getMessage(), e);
        }
    }

    public Coordinates getCoordinatesById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        try {
            return coordinatesRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve coordinates: " + e.getMessage(), e);
        }
    }

    public Coordinates updateCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        if (coordinates.getId() == null) {
            throw new IllegalArgumentException("Coordinates ID cannot be null for update");
        }

        coordinates.validate();

        Coordinates existingCoordinates = coordinatesRepository.findById(coordinates.getId());
        if (existingCoordinates == null) {
            return null;
        }

        try {
            Coordinates updatedCoordinates = coordinatesRepository.update(coordinates);
            return updatedCoordinates;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update coordinates: " + e.getMessage(), e);
        }
    }

    public boolean deleteCoordinates(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }

        try {
            coordinatesRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete coordinates: " + e.getMessage(), e);
        }
    }

    public List<Coordinates> createMultipleCoordinates(List<Coordinates> coordinatesList) {
        if (coordinatesList == null || coordinatesList.isEmpty()) {
            throw new IllegalArgumentException("Coordinates list cannot be null or empty");
        }

        for (Coordinates coordinates : coordinatesList) {
            coordinates.validate();
        }

        try {
            for (Coordinates coordinates : coordinatesList) {
                coordinatesRepository.save(coordinates);
            }
            return coordinatesList;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create multiple coordinates: " + e.getMessage(), e);
        }
    }
}