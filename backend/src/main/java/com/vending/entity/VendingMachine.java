package com.vending.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "vending_machines", indexes = {
    @Index(name = "idx_machine_location", columnList = "location_address"),
    @Index(name = "idx_machine_brand", columnList = "brand")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendingMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Machine ID is required")
    @Size(min = 1, max = 50, message = "Machine ID must be between 1 and 50 characters")
    @Column(name = "machine_id", unique = true, nullable = false)
    private String machineId;

    @Size(max = 200, message = "Machine name must not exceed 200 characters")
    @Column(name = "machine_name")
    private String machineName;

    @NotBlank(message = "Brand is required")
    @Size(min = 1, max = 100, message = "Brand must be between 1 and 100 characters")
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 100, message = "Model must be between 1 and 100 characters")
    @Column(nullable = false)
    private String model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private MachineBrand machineBrand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private MachineModel machineModel;

    @Size(max = 100, message = "Model number must not exceed 100 characters")
    @Column(name = "model_number")
    private String modelNumber;

    @Size(max = 100, message = "Serial number must not exceed 100 characters")
    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "date_purchased")
    private java.time.LocalDate datePurchased;

    @Column(name = "purchased_price", precision = 10, scale = 2)
    private java.math.BigDecimal purchasedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition")
    private MachineCondition condition;

    @Column(nullable = false)
    @Builder.Default
    private boolean deployed = false;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MachineStatus status = MachineStatus.ACTIVE;

    @Column(name = "has_cash_bill_reader")
    private boolean hasCashBillReader;

    @Column(name = "has_cashless_pos")
    private boolean hasCashlessPos;

    @Size(max = 100, message = "POS serial number must not exceed 100 characters")
    @Column(name = "pos_serial_number")
    private String posSerialNumber;

    @Column(name = "has_coin_changer")
    private boolean hasCoinChanger;

    @Size(max = 100, message = "Coin changer serial number must not exceed 100 characters")
    @Column(name = "coin_changer_serial_number")
    private String coinChangerSerialNumber;

    @Embedded
    private Location location;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "machine_allowed_categories",
                     joinColumns = @JoinColumn(name = "machine_id"))
    @Column(name = "category")
    @Builder.Default
    private Set<String> allowedCategories = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "machine_forbidden_categories",
                     joinColumns = @JoinColumn(name = "machine_id"))
    @Column(name = "category")
    @Builder.Default
    private Set<String> forbiddenCategories = new HashSet<>();

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    @Column(length = 2000)
    private String notes;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "machine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MachineProductPrice> productPrices = new HashSet<>();

    @OneToMany(mappedBy = "machine")
    @Builder.Default
    private Set<RestockingLog> restockingLogs = new HashSet<>();

    public boolean isProductAllowed(String category) {
        if (!forbiddenCategories.isEmpty() && forbiddenCategories.contains(category)) {
            return false;
        }
        return allowedCategories.isEmpty() || allowedCategories.contains(category);
    }

    public void addProductPrice(MachineProductPrice price) {
        productPrices.add(price);
        price.setMachine(this);
    }

    public void removeProductPrice(MachineProductPrice price) {
        productPrices.remove(price);
        price.setMachine(null);
    }

    public enum MachineCondition {
        NEW,
        USED,
        REFURBISHED
    }

    public enum MachineStatus {
        ACTIVE,
        BROKEN,
        INACTIVE,
        SOLD
    }
}
