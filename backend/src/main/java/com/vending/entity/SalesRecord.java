package com.vending.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sales_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesRecord {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "settlement_date", nullable = false)
    private LocalDate settlementDate;

    @Column(name = "source", nullable = false, length = 50)
    private String source; // 'CSV' or 'EXCEL'

    @Column(name = "number_of_batches")
    private Integer numberOfBatches;

    @Column(name = "number_completed")
    private Integer numberCompleted;

    @Column(name = "number_sur")
    private Integer numberSur;

    @Column(name = "number_incomplete")
    private Integer numberIncomplete;

    @Column(name = "approved_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal approvedAmount = BigDecimal.ZERO;

    @Column(name = "fee_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
