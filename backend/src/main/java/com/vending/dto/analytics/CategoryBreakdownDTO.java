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
public class CategoryBreakdownDTO {
    private String category;
    private Integer productCount;
    private Integer totalStock;
    private BigDecimal totalValue;
    private Integer totalRestocked;
    private BigDecimal procurementCost;
}
