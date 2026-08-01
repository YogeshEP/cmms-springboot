package com.cmms.controller;

import com.cmms.entity.Technician;
import com.cmms.service.TechnicianService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService technicianService;

    @GetMapping
    public List<Technician> getAll() {
        return technicianService.getAll();
    }

    @GetMapping("/{id}")
    public Technician getById(@PathVariable Long id) {
        return technicianService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Technician create(@Valid @RequestBody Technician technician) {
        return technicianService.create(technician);
    }

    @PutMapping("/{id}")
    public Technician update(@PathVariable Long id, @Valid @RequestBody Technician technician) {
        return technicianService.update(id, technician);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        technicianService.delete(id);
    }
}
