package com.danp1t.service;

import com.danp1t.bean.Coordinates;
import com.danp1t.repository.CoordinatesRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CoordinatesService {

    @Inject
    private CoordinatesRepository coordinatesRepository;

    public Coordinates createCoordinates(Coordinates coordinates) {
        coordinates.validate();

        return coordinatesRepository.save(coordinates);
    }

    public Coordinates getCoordinatesById(Long id) {
        return coordinatesRepository.findById(id);
    }
}