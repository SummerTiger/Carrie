package com.vending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ProductDto(
    UUID id,
    @NotBlank String name,
    @NotBlank String category,
    UUID categoryId,
    UUID brandId,
    String unitSize,
    @NotNull @Min(0) Integer currentStock,
    Integer minimumStock,
    boolean hstExempt,
    BigDecimal basePrice,
    String description,
    String barcode,
    String sku,
    boolean active,
    boolean lowStock,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
