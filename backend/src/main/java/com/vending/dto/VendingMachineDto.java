package com.vending.dto;

import com.vending.entity.Location;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Builder
public record VendingMachineDto(
    UUID id,
    @NotBlank String brand,
    UUID brandId,
    @NotBlank String model,
    UUID modelId,
    boolean hasCashBillReader,
    boolean hasCashlessPos,
    String posSerialNumber,
    boolean hasCoinChanger,
    String coinChangerSerialNumber,
    Location location,
    Set<String> allowedCategories,
    Set<String> forbiddenCategories,
    String notes,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
