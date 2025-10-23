package com.vending.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Email
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "mobile_number", length = 20)
    private String mobileNumber;

    @Column(name = "fax_number", length = 20)
    private String faxNumber;

    @Column(name = "website", length = 255)
    private String website;

    // Address information
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "province", length = 50)
    private String province;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 50)
    @Builder.Default
    private String country = "Canada";

    // Business information
    @Column(name = "customer_id_with_vendor", length = 100)
    private String customerIdWithVendor;

    @Column(name = "business_number", length = 50)
    private String businessNumber;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountRate = BigDecimal.ZERO;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    // Categories and notes
    @Column(name = "product_categories", columnDefinition = "TEXT")
    private String productCategories;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // Status and tracking
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "preferred")
    @Builder.Default
    private Boolean preferred = false;

    @Column(name = "rating")
    @Builder.Default
    private Integer rating = 0;

    // Delivery options
    @Column(name = "order_deliver")
    @Builder.Default
    private Boolean orderDeliver = false;

    @Column(name = "curbside_pickup")
    @Builder.Default
    private Boolean curbsidePickup = false;

    @Column(name = "in_person_only")
    @Builder.Default
    private Boolean inPersonOnly = false;

    // Timestamps
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
