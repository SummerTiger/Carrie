package com.vending.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "peripherals", indexes = {
    @Index(name = "idx_peripheral_type", columnList = "type"),
    @Index(name = "idx_peripheral_status", columnList = "status"),
    @Index(name = "idx_peripheral_machine", columnList = "machine_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Peripheral {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeripheralType type;

    @Size(max = 100, message = "Brand must not exceed 100 characters")
    private String brand;

    @Size(max = 100, message = "Model must not exceed 100 characters")
    private String model;

    @Size(max = 100, message = "Model number must not exceed 100 characters")
    @Column(name = "model_number")
    private String modelNumber;

    @Size(max = 100, message = "Serial number must not exceed 100 characters")
    @Column(name = "serial_number")
    private String serialNumber;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PeripheralStatus status = PeripheralStatus.ACTIVE;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    @Column(length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", foreignKey = @ForeignKey(name = "fk_peripheral_machine"))
    private VendingMachine machine;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PeripheralType {
        CASH_BILL_READER,
        CASHLESS_POS,
        COIN_CHANGER
    }

    public enum PeripheralStatus {
        ACTIVE,
        BROKEN,
        INACTIVE,
        RETIRED
    }
}
