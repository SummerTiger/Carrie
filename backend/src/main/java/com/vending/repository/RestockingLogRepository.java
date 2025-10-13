package com.vending.repository;

import com.vending.entity.RestockingLog;
import com.vending.entity.VendingMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RestockingLogRepository extends JpaRepository<RestockingLog, UUID> {

    List<RestockingLog> findByMachine(VendingMachine machine);

    List<RestockingLog> findByMachineIdOrderByTimestampDesc(UUID machineId);

    List<RestockingLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<RestockingLog> findByPerformedBy(String performedBy);

    @Query("SELECT rl FROM RestockingLog rl " +
           "WHERE rl.machine.id = :machineId " +
           "ORDER BY rl.timestamp DESC")
    List<RestockingLog> findLatestByMachine(@Param("machineId") UUID machineId);

    @Query("SELECT rl FROM RestockingLog rl " +
           "WHERE rl.timestamp >= :startDate " +
           "ORDER BY rl.timestamp DESC")
    List<RestockingLog> findRecentLogs(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT rl FROM RestockingLog rl " +
           "WHERE rl.machine.id = :machineId " +
           "AND rl.timestamp = (SELECT MAX(rl2.timestamp) FROM RestockingLog rl2 WHERE rl2.machine.id = :machineId)")
    RestockingLog findLatestByMachineId(@Param("machineId") UUID machineId);

    @Query("SELECT COUNT(rl) FROM RestockingLog rl " +
           "WHERE rl.timestamp >= :startDate")
    long countLogsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(rl.cashCollected) FROM RestockingLog rl " +
           "WHERE rl.timestamp BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalCashCollected(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT rl FROM RestockingLog rl " +
           "WHERE rl.maintenancePerformed = true " +
           "AND rl.timestamp >= :startDate")
    List<RestockingLog> findLogsWithMaintenance(@Param("startDate") LocalDateTime startDate);
}
