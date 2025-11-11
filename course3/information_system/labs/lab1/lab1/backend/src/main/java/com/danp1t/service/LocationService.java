package com.danp1t.service;

import com.danp1t.model.Location;
import com.danp1t.repository.LocationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class LocationService {

    @Inject
    private LocationRepository locationRepository;

    public Location createLocation(Location location) {
        location.validate();
        return locationRepository.save(location);
    }

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Location getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    public Location updateLocation(Location location) {
        location.validate();

        Location existingLocation = locationRepository.findById(location.getId());
        if (existingLocation == null) {
            return null;
        }

        return locationRepository.update(location);
    }

    public boolean deleteLocation(Long id) {
        Location location = locationRepository.findById(id);
        if (location != null) {
            locationRepository.delete(location);
            return true;
        }
        return false;
    }
}