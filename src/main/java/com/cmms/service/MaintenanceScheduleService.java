package com.cmms.service;

import com.cmms.entity.Asset;
import com.cmms.entity.MaintenanceSchedule;
import com.cmms.exception.ResourceNotFoundException;
import com.cmms.repository.AssetRepository;
import com.cmms.repository.MaintenanceScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MaintenanceScheduleService {

    private final MaintenanceScheduleRepository scheduleRepository;
    private final AssetRepository assetRepository;

    public List<MaintenanceSchedule> getAll() {
        return scheduleRepository.findAll();
    }

    public MaintenanceSchedule getById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance schedule not found with id: " + id));
    }

    public List<MaintenanceSchedule> getByAsset(Long assetId) {
        return scheduleRepository.findByAssetId(assetId);
    }

    public List<MaintenanceSchedule> getDueSchedules() {
        return scheduleRepository.findByNextMaintenanceDateLessThanEqualAndActiveTrue(LocalDate.now());
    }

    public MaintenanceSchedule create(MaintenanceSchedule schedule) {
        Asset asset = assetRepository.findById(schedule.getAsset().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + schedule.getAsset().getId()));
        schedule.setAsset(asset);
        return scheduleRepository.save(schedule);
    }

    public MaintenanceSchedule update(Long id, MaintenanceSchedule updated) {
        MaintenanceSchedule existing = getById(id);
        existing.setFrequency(updated.getFrequency());
        existing.setDescription(updated.getDescription());
        existing.setLastMaintenanceDate(updated.getLastMaintenanceDate());
        existing.setNextMaintenanceDate(updated.getNextMaintenanceDate());
        existing.setActive(updated.getActive());
        return scheduleRepository.save(existing);
    }

    public void delete(Long id) {
        MaintenanceSchedule existing = getById(id);
        scheduleRepository.delete(existing);
    }
}
