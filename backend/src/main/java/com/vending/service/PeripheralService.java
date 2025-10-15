package com.vending.service;

import com.vending.entity.Peripheral;
import com.vending.entity.VendingMachine;
import com.vending.repository.PeripheralRepository;
import com.vending.repository.VendingMachineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PeripheralService {

    private final PeripheralRepository peripheralRepository;
    private final VendingMachineRepository vendingMachineRepository;

    @Transactional(readOnly = true)
    public List<Peripheral> getAllPeripherals() {
        return peripheralRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Peripheral getPeripheralById(UUID id) {
        return peripheralRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Peripheral not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Peripheral> getPeripheralsByType(Peripheral.PeripheralType type) {
        return peripheralRepository.findByType(type);
    }

    @Transactional(readOnly = true)
    public List<Peripheral> getPeripheralsByStatus(Peripheral.PeripheralStatus status) {
        return peripheralRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Peripheral> getPeripheralsByMachine(UUID machineId) {
        return peripheralRepository.findByMachineId(machineId);
    }

    @Transactional(readOnly = true)
    public List<Peripheral> getUnassignedPeripherals() {
        return peripheralRepository.findUnassignedPeripherals();
    }

    @Transactional
    public Peripheral createPeripheral(Peripheral peripheral) {
        return peripheralRepository.save(peripheral);
    }

    @Transactional
    public Peripheral updatePeripheral(UUID id, Peripheral updatedPeripheral) {
        Peripheral peripheral = getPeripheralById(id);

        peripheral.setType(updatedPeripheral.getType());
        peripheral.setBrand(updatedPeripheral.getBrand());
        peripheral.setModel(updatedPeripheral.getModel());
        peripheral.setModelNumber(updatedPeripheral.getModelNumber());
        peripheral.setSerialNumber(updatedPeripheral.getSerialNumber());
        peripheral.setStatus(updatedPeripheral.getStatus());

        return peripheralRepository.save(peripheral);
    }

    @Transactional
    public void deletePeripheral(UUID id) {
        Peripheral peripheral = getPeripheralById(id);
        peripheralRepository.delete(peripheral);
    }

    @Transactional
    public Peripheral assignToMachine(UUID peripheralId, UUID machineId) {
        Peripheral peripheral = getPeripheralById(peripheralId);
        VendingMachine machine = vendingMachineRepository.findById(machineId)
                .orElseThrow(() -> new RuntimeException("Vending machine not found with id: " + machineId));

        peripheral.setMachine(machine);
        return peripheralRepository.save(peripheral);
    }

    @Transactional
    public Peripheral unassignFromMachine(UUID peripheralId) {
        Peripheral peripheral = getPeripheralById(peripheralId);
        peripheral.setMachine(null);
        return peripheralRepository.save(peripheral);
    }
}
