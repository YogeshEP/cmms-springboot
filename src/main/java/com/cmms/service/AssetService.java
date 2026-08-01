package com.cmms.service;

import com.cmms.entity.Asset;
import com.cmms.entity.Location;
import com.cmms.enums.AssetStatus;
import com.cmms.exception.ResourceNotFoundException;
import com.cmms.repository.AssetRepository;
import com.cmms.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final LocationRepository locationRepository;

    public List<Asset> getAll() {
        return assetRepository.findAll();
    }

    public Asset getById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id: " + id));
    }

    public List<Asset> getByStatus(AssetStatus status) {
        return assetRepository.findByStatus(status);
    }

    public List<Asset> getByLocation(Long locationId) {
        return assetRepository.findByLocationId(locationId);
    }

    public Asset create(Asset asset) {
        if (asset.getLocation() != null && asset.getLocation().getId() != null) {
            Location location = locationRepository.findById(asset.getLocation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Location not found with id: " + asset.getLocation().getId()));
            asset.setLocation(location);
        }
        return assetRepository.save(asset);
    }

    public Asset update(Long id, Asset updated) {
        Asset existing = getById(id);
        existing.setName(updated.getName());
        existing.setAssetCode(updated.getAssetCode());
        existing.setCategory(updated.getCategory());
        existing.setManufacturer(updated.getManufacturer());
        existing.setModel(updated.getModel());
        existing.setSerialNumber(updated.getSerialNumber());
        existing.setPurchaseDate(updated.getPurchaseDate());
        existing.setStatus(updated.getStatus());

        if (updated.getLocation() != null && updated.getLocation().getId() != null) {
            Location location = locationRepository.findById(updated.getLocation().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Location not found with id: " + updated.getLocation().getId()));
            existing.setLocation(location);
        }

        return assetRepository.save(existing);
    }

    public void delete(Long id) {
        Asset existing = getById(id);
        assetRepository.delete(existing);
    }
}
