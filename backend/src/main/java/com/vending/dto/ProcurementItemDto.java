package com.vending.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public record ProcurementItemDto(
    UUID id,
    UUID productId,
    String productName,
    @NotNull @Min(1) Integer quantity,
    @NotNull @DecimalMin("0.01") BigDecimal unitCost,
    BigDecimal hstAmount,
    boolean hstExempt,
    BigDecimal totalCost,
    BigDecimal totalWithHst
) {}
