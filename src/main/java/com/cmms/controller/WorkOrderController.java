package com.cmms.controller;

import com.cmms.entity.WorkOrder;
import com.cmms.enums.WorkOrderPriority;
import com.cmms.enums.WorkOrderStatus;
import com.cmms.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @GetMapping
    public List<WorkOrder> getAll() {
        return workOrderService.getAll();
    }

    @GetMapping("/{id}")
    public WorkOrder getById(@PathVariable Long id) {
        return workOrderService.getById(id);
    }

    @GetMapping("/status/{status}")
    public List<WorkOrder> getByStatus(@PathVariable WorkOrderStatus status) {
        return workOrderService.getByStatus(status);
    }

    @GetMapping("/priority/{priority}")
    public List<WorkOrder> getByPriority(@PathVariable WorkOrderPriority priority) {
        return workOrderService.getByPriority(priority);
    }

    @GetMapping("/asset/{assetId}")
    public List<WorkOrder> getByAsset(@PathVariable Long assetId) {
        return workOrderService.getByAsset(assetId);
    }

    @GetMapping("/technician/{technicianId}")
    public List<WorkOrder> getByTechnician(@PathVariable Long technicianId) {
        return workOrderService.getByTechnician(technicianId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrder create(@Valid @RequestBody WorkOrder workOrder) {
        return workOrderService.create(workOrder);
    }

    @PutMapping("/{id}")
    public WorkOrder update(@PathVariable Long id, @Valid @RequestBody WorkOrder workOrder) {
        return workOrderService.update(id, workOrder);
    }

    @PatchMapping("/{id}/status")
    public WorkOrder updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        WorkOrderStatus status = WorkOrderStatus.valueOf(body.get("status").toUpperCase());
        return workOrderService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workOrderService.delete(id);
    }
}
