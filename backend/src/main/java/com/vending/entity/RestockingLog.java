package com.vending.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restocking_logs", indexes = {
    @Index(name = "idx_restock_machine", columnList = "machine_id"),
    @Index(name = "idx_restock_timestamp", columnList = "timestamp"),
    @Index(name = "idx_restock_user", columnList = "performed_by")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestockingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    @NotNull
    private VendingMachine machine;

    @OneToMany(mappedBy = "restockingLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RestockItem> itemsRestocked = new ArrayList<>();

    @Embedded
    private InventoryStatus inventoryStatus;

    @Column(length = 2000)
    private String notes;

    @Column(name = "performed_by")
    private String performedBy;

    @Column(name = "cash_collected", precision = 10, scale = 2)
    private java.math.BigDecimal cashCollected;

    @Column(name = "maintenance_performed")
    @Builder.Default
    private boolean maintenancePerformed = false;

    @Column(name = "maintenance_notes", length = 1000)
    private String maintenanceNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void addRestockItem(RestockItem item) {
        itemsRestocked.add(item);
        item.setRestockingLog(this);
    }

    public void removeRestockItem(RestockItem item) {
        itemsRestocked.remove(item);
        item.setRestockingLog(null);
    }

    public int getTotalItemsRestocked() {
        return itemsRestocked.stream()
            .mapToInt(RestockItem::getQuantity)
            .sum();
    }

    public int getDistinctProductCount() {
        return (int) itemsRestocked.stream()
            .map(item -> item.getProduct().getId())
            .distinct()
            .count();
    }
}
