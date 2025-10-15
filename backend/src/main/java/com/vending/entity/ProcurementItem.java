package com.vending.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "procurement_items", indexes = {
    @Index(name = "idx_procurement_batch", columnList = "batch_id"),
    @Index(name = "idx_procurement_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    @NotNull
    private ProcurementBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull
    private Product product;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @NotNull(message = "Pack quantity is required")
    @Min(value = 1, message = "Pack quantity must be at least 1")
    @Column(name = "pack_quantity", nullable = false)
    @Builder.Default
    private Integer packQuantity = 1;

    @NotNull(message = "Unit cost is required")
    @DecimalMin(value = "0.01", message = "Unit cost must be greater than 0")
    @Column(name = "unit_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "hst_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal hstAmount = BigDecimal.ZERO;

    @Column(name = "hst_exempt")
    @Builder.Default
    private boolean hstExempt = false;

    public BigDecimal getTotalCost() {
        return unitCost.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getTotalWithHst() {
        return getTotalCost().add(hstAmount);
    }

    @PrePersist
    @PreUpdate
    public void calculateHst() {
        if (!hstExempt && product != null) {
            this.hstExempt = product.isHstExempt();
        }

        if (!hstExempt) {
            BigDecimal hstRate = new BigDecimal("0.13"); // Ontario HST
            this.hstAmount = getTotalCost().multiply(hstRate).setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
            this.hstAmount = BigDecimal.ZERO;
        }
    }
}
