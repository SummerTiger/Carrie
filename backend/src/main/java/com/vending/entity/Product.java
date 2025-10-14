package com.vending.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_category", columnList = "category"),
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_stock", columnList = "current_stock")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false, unique = true)
    private String name;

    @NotBlank(message = "Category is required")
    @Column(nullable = false)
    private String category;

    @Column(name = "unit_size")
    private String unitSize;

    @NotNull(message = "Current stock is required")
    @Column(name = "current_stock", nullable = false)
    @Builder.Default
    private Integer currentStock = 0;

    @Column(name = "minimum_stock")
    @Builder.Default
    private Integer minimumStock = 10;

    @Column(name = "hst_exempt", nullable = false)
    @Builder.Default
    private boolean hstExempt = false;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 1000)
    private String description;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "sku")
    private String sku;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @JsonIgnore
    @ManyToMany(mappedBy = "products")
    @Builder.Default
    private List<ProcurementBatch> batches = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MachineProductPrice> machinePrices = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "product")
    @Builder.Default
    private Set<RestockItem> restockItems = new HashSet<>();

    public boolean isLowStock() {
        if (minimumStock == null || minimumStock == 0) {
            return currentStock < 10;
        }
        return currentStock < minimumStock;
    }

    public void addStock(int quantity) {
        this.currentStock += quantity;
    }

    public void removeStock(int quantity) {
        this.currentStock = Math.max(0, this.currentStock - quantity);
    }

    public BigDecimal getPriceForMachine(UUID machineId) {
        return machinePrices.stream()
            .filter(mp -> mp.getMachine().getId().equals(machineId))
            .findFirst()
            .map(MachineProductPrice::getPrice)
            .orElse(basePrice);
    }

    public BigDecimal getWeightedAverageCost() {
        return batches.stream()
            .flatMap(batch -> batch.getItems().stream())
            .filter(item -> item.getProduct().getId().equals(this.id))
            .map(item -> item.getUnitCost())
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(Math.max(1, batches.size())), 2, java.math.RoundingMode.HALF_UP);
    }
}
