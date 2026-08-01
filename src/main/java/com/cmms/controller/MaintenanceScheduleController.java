package com.cmms.controller;

import com.cmms.entity.MaintenanceSchedule;
import com.cmms.service.MaintenanceScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-schedules")
@RequiredArgsConstructor
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService scheduleService;

    @GetMapping
    public List<MaintenanceSchedule> getAll() {
        return scheduleService.getAll();
    }

    @GetMapping("/{id}")
    public MaintenanceSchedule getById(@PathVariable Long id) {
        return scheduleService.getById(id);
    }

    @GetMapping("/asset/{assetId}")
    public List<MaintenanceSchedule> getByAsset(@PathVariable Long assetId) {
        return scheduleService.getByAsset(assetId);
    }

    @GetMapping("/due")
    public List<MaintenanceSchedule> getDue() {
        return scheduleService.getDueSchedules();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceSchedule create(@Valid @RequestBody MaintenanceSchedule schedule) {
        return scheduleService.create(schedule);
    }

    @PutMapping("/{id}")
    public MaintenanceSchedule update(@PathVariable Long id, @Valid @RequestBody MaintenanceSchedule schedule) {
        return scheduleService.update(id, schedule);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        scheduleService.delete(id);
    }
}
