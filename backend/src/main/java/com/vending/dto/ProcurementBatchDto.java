package com.vending.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record ProcurementBatchDto(
    UUID id,
    @NotNull LocalDateTime purchaseDate,
    @NotBlank String supplier,
    String supplierContact,
    List<ProcurementItemDto> items,
    BigDecimal totalHst,
    BigDecimal subtotal,
    BigDecimal totalAmount,
    String invoiceNumber,
    String notes,
    Integer totalItemsCount,
    LocalDateTime createdAt,
    List<ReceiptImageDto> receiptImages
) {}
