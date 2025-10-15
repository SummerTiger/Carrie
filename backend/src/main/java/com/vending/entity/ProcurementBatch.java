package com.vending.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "procurement_batches", indexes = {
    @Index(name = "idx_batch_purchase_date", columnList = "purchase_date"),
    @Index(name = "idx_batch_supplier", columnList = "supplier")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Purchase date is required")
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    @NotBlank(message = "Supplier is required")
    @Column(nullable = false)
    private String supplier;

    @Column(name = "supplier_contact")
    private String supplierContact;

    @ManyToMany
    @JoinTable(
        name = "batch_products",
        joinColumns = @JoinColumn(name = "batch_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProcurementItem> items = new ArrayList<>();

    @Column(name = "total_hst", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalHst = BigDecimal.ZERO;

    @Column(name = "subtotal", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;

    @Column(length = 1000)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addItem(ProcurementItem item) {
        items.add(item);
        item.setBatch(this);
        recalculateTotals();
    }

    public void removeItem(ProcurementItem item) {
        items.remove(item);
        item.setBatch(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotal = items.stream()
            .map(ProcurementItem::getTotalCost)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalHst = items.stream()
            .map(ProcurementItem::getHstAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalAmount = this.subtotal.add(this.totalHst);
    }

    @PostLoad
    public void recalculateTotalsAfterLoad() {
        recalculateTotals();
    }

    public int getTotalItemsCount() {
        return items.stream()
            .mapToInt(ProcurementItem::getQuantity)
            .sum();
    }
}
