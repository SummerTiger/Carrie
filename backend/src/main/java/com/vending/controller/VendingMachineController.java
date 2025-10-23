package com.vending.controller;

import com.vending.dto.VendingMachineDto;
import com.vending.entity.MachineBrand;
import com.vending.entity.MachineModel;
import com.vending.entity.VendingMachine;
import com.vending.exception.DuplicateResourceException;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.MachineBrandRepository;
import com.vending.repository.MachineModelRepository;
import com.vending.repository.VendingMachineRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/vending-machines")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class VendingMachineController {

    @Autowired
    private VendingMachineRepository vendingMachineRepository;

    @Autowired
    private MachineBrandRepository machineBrandRepository;

    @Autowired
    private MachineModelRepository machineModelRepository;

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping
    public ResponseEntity<?> getAllMachines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "brand") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Boolean active
    ) {
        // If no pagination parameters provided, return simple list for backward compatibility
        if (page == 0 && size == 10 && search == null && city == null && active == null) {
            return ResponseEntity.ok(vendingMachineRepository.findAll());
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ?
                    Sort.by(sortBy).ascending() :
                    Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<VendingMachine> machinesPage;

        // Apply filters
        if (search != null && !search.trim().isEmpty()) {
            machinesPage = vendingMachineRepository.findByBrandContainingIgnoreCaseOrModelContainingIgnoreCase(
                search, search, pageable);
        } else if (city != null && !city.trim().isEmpty() && active != null) {
            machinesPage = vendingMachineRepository.findByLocationCityAndActive(city, active, pageable);
        } else if (city != null && !city.trim().isEmpty()) {
            machinesPage = vendingMachineRepository.findByLocationCity(city, pageable);
        } else if (active != null) {
            machinesPage = vendingMachineRepository.findByActive(active, pageable);
        } else {
            machinesPage = vendingMachineRepository.findAll(pageable);
        }

        // Create response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("machines", machinesPage.getContent());
        response.put("currentPage", machinesPage.getNumber());
        response.put("totalItems", machinesPage.getTotalElements());
        response.put("totalPages", machinesPage.getTotalPages());
        response.put("pageSize", machinesPage.getSize());

        return ResponseEntity.ok(response);
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<VendingMachine> getMachineById(@PathVariable UUID id) {
        VendingMachine machine = vendingMachineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendingMachine", "id", id));
        return ResponseEntity.ok(machine);
    }

    // Security: Read access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @GetMapping("/active")
    public ResponseEntity<List<VendingMachine>> getActiveMachines() {
        return ResponseEntity.ok(vendingMachineRepository.findByActiveTrue());
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping
    public ResponseEntity<VendingMachine> createMachine(@Valid @RequestBody VendingMachineDto machineDto) {
        // Check for duplicate POS serial number
        if (machineDto.posSerialNumber() != null && !machineDto.posSerialNumber().isEmpty()
                && vendingMachineRepository.findByPosSerialNumber(machineDto.posSerialNumber()).isPresent()) {
            throw new DuplicateResourceException("VendingMachine", "posSerialNumber", machineDto.posSerialNumber());
        }

        // Create VendingMachine entity from DTO
        VendingMachine machine = new VendingMachine();
        machine.setBrand(machineDto.brand());
        machine.setModel(machineDto.model());
        machine.setHasCashBillReader(machineDto.hasCashBillReader());
        machine.setHasCashlessPos(machineDto.hasCashlessPos());
        machine.setPosSerialNumber(machineDto.posSerialNumber());
        machine.setHasCoinChanger(machineDto.hasCoinChanger());
        machine.setCoinChangerSerialNumber(machineDto.coinChangerSerialNumber());
        machine.setLocation(machineDto.location());
        machine.setAllowedCategories(machineDto.allowedCategories());
        machine.setForbiddenCategories(machineDto.forbiddenCategories());
        machine.setNotes(machineDto.notes());
        machine.setActive(machineDto.active());

        // Handle foreign key relationships
        if (machineDto.brandId() != null) {
            MachineBrand brand = machineBrandRepository.findById(machineDto.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("MachineBrand", "id", machineDto.brandId()));
            machine.setMachineBrand(brand);
        }

        if (machineDto.modelId() != null) {
            MachineModel model = machineModelRepository.findById(machineDto.modelId())
                .orElseThrow(() -> new ResourceNotFoundException("MachineModel", "id", machineDto.modelId()));
            machine.setMachineModel(model);
        }

        VendingMachine saved = vendingMachineRepository.save(machine);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<VendingMachine> updateMachine(@PathVariable UUID id, @Valid @RequestBody VendingMachineDto machineDto) {
        VendingMachine machine = vendingMachineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VendingMachine", "id", id));

        // Check for duplicate POS serial number (excluding current machine)
        if (machineDto.posSerialNumber() != null && !machineDto.posSerialNumber().isEmpty()
                && !machineDto.posSerialNumber().equals(machine.getPosSerialNumber())) {
            vendingMachineRepository.findByPosSerialNumber(machineDto.posSerialNumber()).ifPresent(m -> {
                throw new DuplicateResourceException("VendingMachine", "posSerialNumber", machineDto.posSerialNumber());
            });
        }

        // Update basic fields
        if (machineDto.brand() != null) machine.setBrand(machineDto.brand());
        if (machineDto.model() != null) machine.setModel(machineDto.model());
        machine.setHasCashBillReader(machineDto.hasCashBillReader());
        machine.setHasCashlessPos(machineDto.hasCashlessPos());
        if (machineDto.posSerialNumber() != null) machine.setPosSerialNumber(machineDto.posSerialNumber());
        machine.setHasCoinChanger(machineDto.hasCoinChanger());
        if (machineDto.coinChangerSerialNumber() != null) machine.setCoinChangerSerialNumber(machineDto.coinChangerSerialNumber());
        if (machineDto.location() != null) machine.setLocation(machineDto.location());
        if (machineDto.allowedCategories() != null) machine.setAllowedCategories(machineDto.allowedCategories());
        if (machineDto.forbiddenCategories() != null) machine.setForbiddenCategories(machineDto.forbiddenCategories());
        if (machineDto.notes() != null) machine.setNotes(machineDto.notes());
        machine.setActive(machineDto.active());

        // Handle foreign key relationships
        if (machineDto.brandId() != null) {
            MachineBrand brand = machineBrandRepository.findById(machineDto.brandId())
                .orElseThrow(() -> new ResourceNotFoundException("MachineBrand", "id", machineDto.brandId()));
            machine.setMachineBrand(brand);
        }

        if (machineDto.modelId() != null) {
            MachineModel model = machineModelRepository.findById(machineDto.modelId())
                .orElseThrow(() -> new ResourceNotFoundException("MachineModel", "id", machineDto.modelId()));
            machine.setMachineModel(model);
        }

        return ResponseEntity.ok(vendingMachineRepository.save(machine));
    }

    // Security: Write access for ADMIN and MANAGER roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMachine(@PathVariable UUID id) {
        if (!vendingMachineRepository.existsById(id)) {
            throw new ResourceNotFoundException("VendingMachine", "id", id);
        }
        vendingMachineRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
