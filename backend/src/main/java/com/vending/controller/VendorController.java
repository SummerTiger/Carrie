package com.vending.controller;

import com.vending.dto.VendorDTO;
import com.vending.service.VendorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vendors", description = "Vendor management endpoints")
@CrossOrigin(origins = "*")
public class VendorController {

    private final VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get all vendors")
    public ResponseEntity<List<VendorDTO>> getAllVendors() {
        return ResponseEntity.ok(vendorService.getAllVendors());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get active vendors")
    public ResponseEntity<List<VendorDTO>> getActiveVendors() {
        return ResponseEntity.ok(vendorService.getActiveVendors());
    }

    @GetMapping("/preferred")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get preferred vendors")
    public ResponseEntity<List<VendorDTO>> getPreferredVendors() {
        return ResponseEntity.ok(vendorService.getPreferredVendors());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get vendor by ID")
    public ResponseEntity<VendorDTO> getVendorById(@PathVariable UUID id) {
        return ResponseEntity.ok(vendorService.getVendorById(id));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Search vendors")
    public ResponseEntity<List<VendorDTO>> searchVendors(@RequestParam String query) {
        return ResponseEntity.ok(vendorService.searchVendors(query));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Create new vendor")
    public ResponseEntity<VendorDTO> createVendor(@RequestBody VendorDTO vendorDTO) {
        VendorDTO created = vendorService.createVendor(vendorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Update vendor")
    public ResponseEntity<VendorDTO> updateVendor(
            @PathVariable UUID id,
            @RequestBody VendorDTO vendorDTO) {
        return ResponseEntity.ok(vendorService.updateVendor(id, vendorDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete vendor")
    public ResponseEntity<Void> deleteVendor(@PathVariable UUID id) {
        vendorService.deleteVendor(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Toggle vendor active status")
    public ResponseEntity<VendorDTO> toggleVendorStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(vendorService.toggleVendorStatus(id));
    }
}
