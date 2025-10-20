package com.vending.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAnalyticsDTO {
    private UUID productId;
    private String productName;
    private String category;
    private Integer currentStock;
    private Integer totalRestocked;
    private Integer totalProcured;
    private BigDecimal totalProcurementCost;
    private BigDecimal averageUnitCost;
    private Integer minimumStock;
    private Boolean isLowStock;
}
