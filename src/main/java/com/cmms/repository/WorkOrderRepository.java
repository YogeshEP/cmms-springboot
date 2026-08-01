package com.cmms.repository;

import com.cmms.entity.WorkOrder;
import com.cmms.enums.WorkOrderPriority;
import com.cmms.enums.WorkOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    List<WorkOrder> findByStatus(WorkOrderStatus status);
    List<WorkOrder> findByPriority(WorkOrderPriority priority);
    List<WorkOrder> findByAssetId(Long assetId);
    List<WorkOrder> findByTechnicianId(Long technicianId);
}
