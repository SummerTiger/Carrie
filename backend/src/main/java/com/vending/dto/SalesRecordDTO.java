package com.vending.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesRecordDTO {
    private UUID id;
    private LocalDate settlementDate;
    private String source;
    private Integer numberOfBatches;
    private Integer numberCompleted;
    private Integer numberSur;
    private Integer numberIncomplete;
    private BigDecimal approvedAmount;
    private BigDecimal feeAmount;
    private String fileName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
