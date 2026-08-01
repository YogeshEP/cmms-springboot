package com.cmms.service;

import com.cmms.entity.Location;
import com.cmms.exception.ResourceNotFoundException;
import com.cmms.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    public List<Location> getAll() {
        return locationRepository.findAll();
    }

    public Location getById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }

    public Location create(Location location) {
        return locationRepository.save(location);
    }

    public Location update(Long id, Location updated) {
        Location existing = getById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return locationRepository.save(existing);
    }

    public void delete(Long id) {
        Location existing = getById(id);
        locationRepository.delete(existing);
    }
}
