package com.cmms.service;

import com.cmms.entity.Asset;
import com.cmms.entity.Technician;
import com.cmms.entity.WorkOrder;
import com.cmms.enums.WorkOrderPriority;
import com.cmms.enums.WorkOrderStatus;
import com.cmms.exception.ResourceNotFoundException;
import com.cmms.repository.AssetRepository;
import com.cmms.repository.TechnicianRepository;
import com.cmms.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final AssetRepository assetRepository;
    private final TechnicianRepository technicianRepository;

    public List<WorkOrder> getAll() {
        return workOrderRepository.findAll();
    }

    public WorkOrder getById(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work order not found with id: " + id));
    }

    public List<WorkOrder> getByStatus(WorkOrderStatus status) {
        return workOrderRepository.findByStatus(status);
    }

    public List<WorkOrder> getByPriority(WorkOrderPriority priority) {
        return workOrderRepository.findByPriority(priority);
    }

    public List<WorkOrder> getByAsset(Long assetId) {
        return workOrderRepository.findByAssetId(assetId);
    }

    public List<WorkOrder> getByTechnician(Long technicianId) {
        return workOrderRepository.findByTechnicianId(technicianId);
    }

    public WorkOrder create(WorkOrder workOrder) {
        Asset asset = assetRepository.findById(workOrder.getAsset().getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id: " + workOrder.getAsset().getId()));
        workOrder.setAsset(asset);

        if (workOrder.getTechnician() != null && workOrder.getTechnician().getId() != null) {
            Technician technician = technicianRepository.findById(workOrder.getTechnician().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Technician not found with id: " + workOrder.getTechnician().getId()));
            workOrder.setTechnician(technician);
        }

        return workOrderRepository.save(workOrder);
    }

    public WorkOrder update(Long id, WorkOrder updated) {
        WorkOrder existing = getById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setType(updated.getType());
        existing.setPriority(updated.getPriority());
        existing.setDueDate(updated.getDueDate());

        if (updated.getTechnician() != null && updated.getTechnician().getId() != null) {
            Technician technician = technicianRepository.findById(updated.getTechnician().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Technician not found with id: " + updated.getTechnician().getId()));
            existing.setTechnician(technician);
        }

        return workOrderRepository.save(existing);
    }

    public WorkOrder updateStatus(Long id, WorkOrderStatus status) {
        WorkOrder existing = getById(id);
        existing.setStatus(status);
        if (status == WorkOrderStatus.COMPLETED) {
            existing.setCompletedDate(LocalDateTime.now());
        }
        return workOrderRepository.save(existing);
    }

    public void delete(Long id) {
        WorkOrder existing = getById(id);
        workOrderRepository.delete(existing);
    }
}
