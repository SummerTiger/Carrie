package com.vending.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDTO {
    private BigDecimal totalRevenue;
    private BigDecimal totalProcurementCost;
    private BigDecimal totalCashCollected;
    private BigDecimal profitMargin;
    private Integer totalProducts;
    private Integer totalMachines;
    private Integer totalRestockingSessions;
    private Integer lowStockProducts;
    private Integer outOfStockProducts;
}
