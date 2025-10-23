package com.vending.controller;

import com.vending.entity.MachineModel;
import com.vending.service.MachineModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/machine-models")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@RequiredArgsConstructor
public class MachineModelController {

    private final MachineModelService modelService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<?> getAllModels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "displayOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search
    ) {
        if (page == 0 && size == 10 && search == null) {
            return ResponseEntity.ok(modelService.getAllModels());
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MachineModel> modelsPage;
        if (search != null && !search.trim().isEmpty()) {
            modelsPage = modelService.searchModels(search, pageable);
        } else {
            modelsPage = modelService.getAllModels(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("models", modelsPage.getContent());
        response.put("currentPage", modelsPage.getNumber());
        response.put("totalItems", modelsPage.getTotalElements());
        response.put("totalPages", modelsPage.getTotalPages());
        response.put("pageSize", modelsPage.getSize());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/active")
    public ResponseEntity<List<MachineModel>> getActiveModels() {
        return ResponseEntity.ok(modelService.getAllActiveModels());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<List<MachineModel>> getModelsByBrand(@PathVariable UUID brandId) {
        return ResponseEntity.ok(modelService.getModelsByBrand(brandId));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<MachineModel> getModelById(@PathVariable UUID id) {
        return ResponseEntity.ok(modelService.getModelById(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<MachineModel> createModel(@Valid @RequestBody MachineModel model) {
        MachineModel created = modelService.createModel(model);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<MachineModel> updateModel(
            @PathVariable UUID id,
            @Valid @RequestBody MachineModel model
    ) {
        MachineModel updated = modelService.updateModel(id, model);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModel(@PathVariable UUID id) {
        modelService.deleteModel(id);
        return ResponseEntity.noContent().build();
    }
}
