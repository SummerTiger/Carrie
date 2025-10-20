package com.vending.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTrendDTO {
    private LocalDate date;
    private Integer totalStock;
    private Integer lowStockCount;
    private Integer outOfStockCount;
    private Integer restockedItemsCount;
}
