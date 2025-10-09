package com.danp1t.service;

import com.danp1t.bean.Location;
import com.danp1t.repository.LocationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class LocationService {

    @Inject
    private LocationRepository locationRepository;

    public Location createLocation(Location location) {
        location.validate();

        return locationRepository.save(location);
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id);
    }
}
