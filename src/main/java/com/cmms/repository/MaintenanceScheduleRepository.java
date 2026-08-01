package com.cmms.repository;

import com.cmms.entity.MaintenanceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MaintenanceScheduleRepository extends JpaRepository<MaintenanceSchedule, Long> {
    List<MaintenanceSchedule> findByAssetId(Long assetId);
    List<MaintenanceSchedule> findByNextMaintenanceDateLessThanEqualAndActiveTrue(LocalDate date);
}
