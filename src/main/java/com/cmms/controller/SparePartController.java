package com.cmms.controller;

import com.cmms.entity.SparePart;
import com.cmms.service.SparePartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
public class SparePartController {

    private final SparePartService sparePartService;

    @GetMapping
    public List<SparePart> getAll() {
        return sparePartService.getAll();
    }

    @GetMapping("/{id}")
    public SparePart getById(@PathVariable Long id) {
        return sparePartService.getById(id);
    }

    @GetMapping("/low-stock")
    public List<SparePart> getLowStock() {
        return sparePartService.getLowStock();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SparePart create(@Valid @RequestBody SparePart sparePart) {
        return sparePartService.create(sparePart);
    }

    @PutMapping("/{id}")
    public SparePart update(@PathVariable Long id, @Valid @RequestBody SparePart sparePart) {
        return sparePartService.update(id, sparePart);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        sparePartService.delete(id);
    }
}
