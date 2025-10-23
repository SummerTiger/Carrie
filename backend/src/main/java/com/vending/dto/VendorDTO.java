package com.vending.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorDTO {
    private UUID id;
    private String name;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String mobileNumber;
    private String faxNumber;
    private String website;

    // Address
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String province;
    private String postalCode;
    private String country;

    // Business info
    private String customerIdWithVendor;
    private String businessNumber;
    private String taxId;
    private String paymentTerms;
    private BigDecimal discountRate;
    private BigDecimal creditLimit;

    // Categories and notes
    private String productCategories;
    private String description;
    private String notes;

    // Status
    private Boolean active;
    private Boolean preferred;
    private Integer rating;

    // Delivery options
    private Boolean orderDeliver;
    private Boolean curbsidePickup;
    private Boolean inPersonOnly;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics (calculated fields)
    private Integer totalPurchases;
    private BigDecimal totalSpent;
    private LocalDateTime lastPurchaseDate;
}
