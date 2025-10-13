package com.vending.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RestockItemDto(
    UUID id,
    UUID productId,
    String productName,
    String productCategory,
    @NotNull @Min(1) Integer quantity,
    Integer stockBefore,
    Integer stockAfter,
    Integer expiredItemsRemoved
) {}
