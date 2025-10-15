package com.vending.repository;

import com.vending.entity.Peripheral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PeripheralRepository extends JpaRepository<Peripheral, UUID> {

    List<Peripheral> findByType(Peripheral.PeripheralType type);

    List<Peripheral> findByStatus(Peripheral.PeripheralStatus status);

    List<Peripheral> findByMachineId(UUID machineId);

    @Query("SELECT p FROM Peripheral p WHERE p.machine.id = :machineId AND p.type = :type")
    List<Peripheral> findByMachineIdAndType(
            @Param("machineId") UUID machineId,
            @Param("type") Peripheral.PeripheralType type);

    @Query("SELECT p FROM Peripheral p WHERE p.machine IS NULL")
    List<Peripheral> findUnassignedPeripherals();

    @Query("SELECT COUNT(p) FROM Peripheral p WHERE p.status = :status")
    long countByStatus(@Param("status") Peripheral.PeripheralStatus status);
}
