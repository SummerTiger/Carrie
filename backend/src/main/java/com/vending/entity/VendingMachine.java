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

    @NotBlank(message = "Brand is required")
    @Size(min = 1, max = 100, message = "Brand must be between 1 and 100 characters")
    @Column(nullable = false)
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(min = 1, max = 100, message = "Model must be between 1 and 100 characters")
    @Column(nullable = false)
    private String model;

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
}
