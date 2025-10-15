package com.vending.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ReceiptImageDto(
    UUID id,
    String imageUrl,
    String imageName,
    String contentType,
    Long fileSize,
    LocalDateTime uploadDate,
    Integer imageOrder
) {}
