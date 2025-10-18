package com.vending.controller;

import com.vending.dto.ProcurementBatchDto;
import com.vending.service.ProcurementBatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/procurement-batches")
@RequiredArgsConstructor
public class ProcurementBatchController {

    private final ProcurementBatchService procurementBatchService;

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<List<ProcurementBatchDto>> getAllBatches() {
        return ResponseEntity.ok(procurementBatchService.getAllBatches());
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<ProcurementBatchDto> getBatchById(@PathVariable UUID id) {
        return ResponseEntity.ok(procurementBatchService.getBatchById(id));
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/supplier/{supplier}")
    public ResponseEntity<List<ProcurementBatchDto>> getBatchesBySupplier(@PathVariable String supplier) {
        return ResponseEntity.ok(procurementBatchService.getBatchesBySupplier(supplier));
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/suppliers")
    public ResponseEntity<List<String>> getAllSuppliers() {
        return ResponseEntity.ok(procurementBatchService.getAllSuppliers());
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<ProcurementBatchDto> createBatch(@Valid @RequestBody ProcurementBatchDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(procurementBatchService.createBatch(dto));
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProcurementBatchDto> updateBatch(
            @PathVariable UUID id,
            @Valid @RequestBody ProcurementBatchDto dto) {
        return ResponseEntity.ok(procurementBatchService.updateBatch(id, dto));
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBatch(@PathVariable UUID id) {
        procurementBatchService.deleteBatch(id);
        return ResponseEntity.noContent().build();
    }
}
