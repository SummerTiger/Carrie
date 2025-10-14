package com.vending.controller;

import com.vending.entity.VendingMachine;
import com.vending.repository.VendingMachineRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vending-machines")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class VendingMachineController {

    @Autowired
    private VendingMachineRepository vendingMachineRepository;

    @GetMapping
    public ResponseEntity<List<VendingMachine>> getAllMachines() {
        return ResponseEntity.ok(vendingMachineRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendingMachine> getMachineById(@PathVariable UUID id) {
        return vendingMachineRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<VendingMachine>> getActiveMachines() {
        return ResponseEntity.ok(vendingMachineRepository.findByActiveTrue());
    }

    @PostMapping
    public ResponseEntity<VendingMachine> createMachine(@Valid @RequestBody VendingMachine machine) {
        VendingMachine saved = vendingMachineRepository.save(machine);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VendingMachine> updateMachine(@PathVariable UUID id, @Valid @RequestBody VendingMachine machineUpdate) {
        return vendingMachineRepository.findById(id)
                .map(machine -> {
                    if (machineUpdate.getBrand() != null) machine.setBrand(machineUpdate.getBrand());
                    if (machineUpdate.getModel() != null) machine.setModel(machineUpdate.getModel());
                    machine.setActive(machineUpdate.isActive());
                    return ResponseEntity.ok(vendingMachineRepository.save(machine));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable UUID id) {
        if (!vendingMachineRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vendingMachineRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
