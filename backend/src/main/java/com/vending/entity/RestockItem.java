package com.vending.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "restock_items", indexes = {
    @Index(name = "idx_restock_log", columnList = "restocking_log_id"),
    @Index(name = "idx_restock_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restocking_log_id", nullable = false)
    @NotNull
    private RestockingLog restockingLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "stock_before")
    private Integer stockBefore;

    @Column(name = "stock_after")
    private Integer stockAfter;

    @Column(name = "expired_items_removed")
    @Builder.Default
    private Integer expiredItemsRemoved = 0;

    @PrePersist
    public void calculateStockAfter() {
        if (stockBefore != null) {
            this.stockAfter = stockBefore + quantity - (expiredItemsRemoved != null ? expiredItemsRemoved : 0);
        }
    }
}
