package com.vending.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDataDTO {
    private LocalDate date;
    private BigDecimal cashCollected;
    private BigDecimal procurementCost;
    private BigDecimal profit;
    private Integer restockCount;
}
