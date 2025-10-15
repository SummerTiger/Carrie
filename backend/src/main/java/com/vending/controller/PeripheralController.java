package com.vending.controller;

import com.vending.entity.Peripheral;
import com.vending.service.PeripheralService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/peripherals")
@RequiredArgsConstructor
public class PeripheralController {

    private final PeripheralService peripheralService;

    @GetMapping
    public ResponseEntity<List<Peripheral>> getAllPeripherals() {
        return ResponseEntity.ok(peripheralService.getAllPeripherals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Peripheral> getPeripheralById(@PathVariable UUID id) {
        return ResponseEntity.ok(peripheralService.getPeripheralById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Peripheral>> getPeripheralsByType(@PathVariable Peripheral.PeripheralType type) {
        return ResponseEntity.ok(peripheralService.getPeripheralsByType(type));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Peripheral>> getPeripheralsByStatus(@PathVariable Peripheral.PeripheralStatus status) {
        return ResponseEntity.ok(peripheralService.getPeripheralsByStatus(status));
    }

    @GetMapping("/machine/{machineId}")
    public ResponseEntity<List<Peripheral>> getPeripheralsByMachine(@PathVariable UUID machineId) {
        return ResponseEntity.ok(peripheralService.getPeripheralsByMachine(machineId));
    }

    @GetMapping("/unassigned")
    public ResponseEntity<List<Peripheral>> getUnassignedPeripherals() {
        return ResponseEntity.ok(peripheralService.getUnassignedPeripherals());
    }

    @PostMapping
    public ResponseEntity<Peripheral> createPeripheral(@Valid @RequestBody Peripheral peripheral) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(peripheralService.createPeripheral(peripheral));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Peripheral> updatePeripheral(
            @PathVariable UUID id,
            @Valid @RequestBody Peripheral peripheral) {
        return ResponseEntity.ok(peripheralService.updatePeripheral(id, peripheral));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePeripheral(@PathVariable UUID id) {
        peripheralService.deletePeripheral(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{peripheralId}/assign/{machineId}")
    public ResponseEntity<Peripheral> assignToMachine(
            @PathVariable UUID peripheralId,
            @PathVariable UUID machineId) {
        return ResponseEntity.ok(peripheralService.assignToMachine(peripheralId, machineId));
    }

    @PutMapping("/{peripheralId}/unassign")
    public ResponseEntity<Peripheral> unassignFromMachine(@PathVariable UUID peripheralId) {
        return ResponseEntity.ok(peripheralService.unassignFromMachine(peripheralId));
    }
}
