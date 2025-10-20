package com.vending.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MachinePerformanceDTO {
    private UUID machineId;
    private String machineBrand;
    private String machineModel;
    private String locationAddress;
    private Integer restockCount;
    private BigDecimal totalCashCollected;
    private BigDecimal averageCashPerRestock;
    private Integer totalItemsRestocked;
    private Integer maintenanceCount;
}
