package com.vending.dto;

import com.vending.entity.InventoryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record RestockingLogDto(
    UUID id,
    @NotNull LocalDateTime timestamp,
    UUID machineId,
    String machineBrand,
    String machineModel,
    String machineLocation,
    List<RestockItemDto> itemsRestocked,
    InventoryStatus inventoryStatus,
    String notes,
    String performedBy,
    BigDecimal cashCollected,
    boolean maintenancePerformed,
    String maintenanceNotes,
    Integer totalItemsRestocked,
    Integer distinctProductCount,
    LocalDateTime createdAt
) {}
