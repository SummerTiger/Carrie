package com.vending.repository;

import com.vending.entity.SalesRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface SalesRecordRepository extends JpaRepository<SalesRecord, UUID> {
    List<SalesRecord> findBySettlementDateBetweenOrderBySettlementDateDesc(LocalDate startDate, LocalDate endDate);
    List<SalesRecord> findBySourceOrderBySettlementDateDesc(String source);
    List<SalesRecord> findAllByOrderBySettlementDateDesc();
}
