package com.vending.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStatus {

    @Column(name = "total_slots")
    private Integer totalSlots;

    @Column(name = "occupied_slots")
    private Integer occupiedSlots;

    @Column(name = "empty_slots")
    private Integer emptySlots;

    @Column(name = "total_products")
    private Integer totalProducts;

    @Column(name = "low_stock_products")
    @Builder.Default
    private Integer lowStockProducts = 0;

    @Column(name = "out_of_stock_products")
    @Builder.Default
    private Integer outOfStockProducts = 0;

    public double getOccupancyRate() {
        if (totalSlots == null || totalSlots == 0) {
            return 0.0;
        }
        return (double) (occupiedSlots != null ? occupiedSlots : 0) / totalSlots * 100;
    }

    public boolean needsRestocking() {
        return (lowStockProducts != null && lowStockProducts > 0) ||
               (outOfStockProducts != null && outOfStockProducts > 0);
    }
}
