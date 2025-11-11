package com.danp1t.service;

import com.danp1t.model.Coordinates;
import com.danp1t.repository.CoordinatesRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class CoordinatesService {

    @Inject
    private CoordinatesRepository coordinatesRepository;

    public Coordinates createCoordinates(Coordinates coordinates) {

        coordinates.validate();
        if (coordinates.getX() == null) {
            throw new IllegalArgumentException("Координата X не может быть null");
        }
        if (coordinates.getY() == null) {
            throw new IllegalArgumentException("Координата Y не может быть null");
        }

        try {
            return coordinatesRepository.save(coordinates);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка создания координат: " + e.getMessage(), e);
        }
    }

    public List<Coordinates> getAllCoordinates() {
        try {
            return coordinatesRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска координат: " + e.getMessage(), e);
        }
    }

    public Coordinates getCoordinatesById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть NULL");
        }

        try {
            return coordinatesRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка поиска координат: " + e.getMessage(), e);
        }
    }

    public Coordinates updateCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Координаты не могут быть NULL");
        }
        if (coordinates.getId() == null) {
            throw new IllegalArgumentException("ID координат не может быть NULL");
        }

        coordinates.validate();

        Coordinates existingCoordinates = coordinatesRepository.findById(coordinates.getId());
        if (existingCoordinates == null) {
            return null;
        }

        try {
            return coordinatesRepository.update(coordinates);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка обновления координат: " + e.getMessage(), e);
        }
    }

    public boolean deleteCoordinates(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID не может быть NULL");
        }

        try {
            coordinatesRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка удаления координат: " + e.getMessage(), e);
        }
    }
}