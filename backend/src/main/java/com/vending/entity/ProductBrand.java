package com.vending.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "product_brands", indexes = {
    @Index(name = "idx_brand_name", columnList = "name"),
    @Index(name = "idx_brand_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductBrand {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Brand name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 255)
    private String website;

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

    @OneToMany(mappedBy = "productBrand", fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
