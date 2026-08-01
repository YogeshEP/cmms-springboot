package com.cmms.service;

import com.cmms.entity.SparePart;
import com.cmms.exception.ResourceNotFoundException;
import com.cmms.repository.SparePartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SparePartService {

    private final SparePartRepository sparePartRepository;

    public List<SparePart> getAll() {
        return sparePartRepository.findAll();
    }

    public SparePart getById(Long id) {
        return sparePartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Spare part not found with id: " + id));
    }

    public List<SparePart> getLowStock() {
        return sparePartRepository.findLowStockParts();
    }

    public SparePart create(SparePart sparePart) {
        return sparePartRepository.save(sparePart);
    }

    public SparePart update(Long id, SparePart updated) {
        SparePart existing = getById(id);
        existing.setName(updated.getName());
        existing.setPartNumber(updated.getPartNumber());
        existing.setQuantityInStock(updated.getQuantityInStock());
        existing.setReorderLevel(updated.getReorderLevel());
        existing.setUnitCost(updated.getUnitCost());
        return sparePartRepository.save(existing);
    }

    public void delete(Long id) {
        SparePart existing = getById(id);
        sparePartRepository.delete(existing);
    }
}
