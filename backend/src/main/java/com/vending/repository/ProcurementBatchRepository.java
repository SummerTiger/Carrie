package com.vending.repository;

import com.vending.entity.ProcurementBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProcurementBatchRepository extends JpaRepository<ProcurementBatch, UUID> {

    Optional<ProcurementBatch> findByInvoiceNumber(String invoiceNumber);

    List<ProcurementBatch> findBySupplier(String supplier);

    List<ProcurementBatch> findByPurchaseDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT pb FROM ProcurementBatch pb ORDER BY pb.purchaseDate DESC")
    List<ProcurementBatch> findAllOrderByPurchaseDateDesc();

    @Query("SELECT pb FROM ProcurementBatch pb " +
           "WHERE pb.purchaseDate >= :startDate " +
           "ORDER BY pb.purchaseDate DESC")
    List<ProcurementBatch> findRecentBatches(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT DISTINCT pb.supplier FROM ProcurementBatch pb ORDER BY pb.supplier")
    List<String> findAllSuppliers();

    @Query("SELECT SUM(pb.totalAmount) FROM ProcurementBatch pb " +
           "WHERE pb.purchaseDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalProcurementCost(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT SUM(pb.totalHst) FROM ProcurementBatch pb " +
           "WHERE pb.purchaseDate BETWEEN :startDate AND :endDate")
    java.math.BigDecimal getTotalHstPaid(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(pb) FROM ProcurementBatch pb " +
           "WHERE pb.purchaseDate >= :startDate")
    long countBatchesSince(@Param("startDate") LocalDateTime startDate);

    List<ProcurementBatch> findByVendorId(UUID vendorId);
}
