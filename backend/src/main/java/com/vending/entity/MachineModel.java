package com.vending.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "machine_models", indexes = {
    @Index(name = "idx_model_name", columnList = "name"),
    @Index(name = "idx_model_brand", columnList = "brand_id"),
    @Index(name = "idx_model_active", columnList = "active")
}, uniqueConstraints = {
    @UniqueConstraint(name = "idx_brand_model", columnNames = {"brand_id", "name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MachineBrand machineBrand;

    @NotBlank(message = "Model name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "model_number", length = 100)
    private String modelNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer capacity;

    @Column(length = 100)
    private String dimensions;

    @Column(name = "weight_kg", precision = 10, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "power_requirements", length = 100)
    private String powerRequirements;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "machineModel", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<VendingMachine> vendingMachines = new ArrayList<>();
}
