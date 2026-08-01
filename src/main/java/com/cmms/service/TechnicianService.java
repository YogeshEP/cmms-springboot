package com.cmms.service;

import com.cmms.entity.Technician;
import com.cmms.exception.ResourceNotFoundException;
import com.cmms.repository.TechnicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicianService {

    private final TechnicianRepository technicianRepository;

    public List<Technician> getAll() {
        return technicianRepository.findAll();
    }

    public Technician getById(Long id) {
        return technicianRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Technician not found with id: " + id));
    }

    public Technician create(Technician technician) {
        return technicianRepository.save(technician);
    }

    public Technician update(Long id, Technician updated) {
        Technician existing = getById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setSpecialization(updated.getSpecialization());
        return technicianRepository.save(existing);
    }

    public void delete(Long id) {
        Technician existing = getById(id);
        technicianRepository.delete(existing);
    }
}
