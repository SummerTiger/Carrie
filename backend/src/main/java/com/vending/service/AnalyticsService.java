package com.vending.service;

import com.vending.dto.analytics.*;
import com.vending.entity.Product;
import com.vending.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ProductRepository productRepository;
    private final VendingMachineRepository machineRepository;
    private final RestockingLogRepository restockingLogRepository;
    private final ProcurementBatchRepository procurementBatchRepository;
    private final ProcurementItemRepository procurementItemRepository;
    private final RestockItemRepository restockItemRepository;

    /**
     * Get overall analytics summary
     */
    @Cacheable(value = "analytics-summary", key = "'summary-' + #startDate + '-' + #endDate")
    public AnalyticsSummaryDTO getSummary(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating analytics summary from {} to {}", startDate, endDate);

        // Total cash collected from restocking
        BigDecimal totalCashCollected = restockingLogRepository.findByTimestampBetween(startDate, endDate)
            .stream()
            .map(log -> log.getCashCollected() != null ? log.getCashCollected() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Total procurement cost
        BigDecimal totalProcurementCost = procurementBatchRepository.findByPurchaseDateBetween(startDate, endDate)
            .stream()
            .map(batch -> batch.getTotalAmount() != null ? batch.getTotalAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Profit margin
        BigDecimal profitMargin = BigDecimal.ZERO;
        if (totalCashCollected.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profit = totalCashCollected.subtract(totalProcurementCost);
            profitMargin = profit.divide(totalCashCollected, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        }

        // Product counts
        List<Product> products = productRepository.findAll();
        int lowStockCount = (int) products.stream()
            .filter(p -> p.getCurrentStock() < p.getMinimumStock())
            .count();
        int outOfStockCount = (int) products.stream()
            .filter(p -> p.getCurrentStock() == 0)
            .count();

        return AnalyticsSummaryDTO.builder()
            .totalRevenue(totalCashCollected)
            .totalProcurementCost(totalProcurementCost)
            .totalCashCollected(totalCashCollected)
            .profitMargin(profitMargin)
            .totalProducts((int) productRepository.count())
            .totalMachines((int) machineRepository.count())
            .totalRestockingSessions((int) restockingLogRepository.countByTimestampBetween(startDate, endDate))
            .lowStockProducts(lowStockCount)
            .outOfStockProducts(outOfStockCount)
            .build();
    }

    /**
     * Get revenue data over time
     */
    @Cacheable(value = "analytics-revenue", key = "'revenue-' + #startDate + '-' + #endDate")
    public List<RevenueDataDTO> getRevenueData(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating revenue data from {} to {}", startDate, endDate);

        var restockLogs = restockingLogRepository.findByTimestampBetween(startDate, endDate);
        var procurementBatches = procurementBatchRepository.findByPurchaseDateBetween(startDate, endDate);

        // Group by date
        Map<LocalDate, BigDecimal> cashByDate = restockLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTimestamp().toLocalDate(),
                Collectors.reducing(BigDecimal.ZERO,
                    log -> log.getCashCollected() != null ? log.getCashCollected() : BigDecimal.ZERO,
                    BigDecimal::add)
            ));

        Map<LocalDate, BigDecimal> costByDate = procurementBatches.stream()
            .collect(Collectors.groupingBy(
                batch -> batch.getPurchaseDate().toLocalDate(),
                Collectors.reducing(BigDecimal.ZERO,
                    batch -> batch.getTotalAmount() != null ? batch.getTotalAmount() : BigDecimal.ZERO,
                    BigDecimal::add)
            ));

        Map<LocalDate, Long> restockCountByDate = restockLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTimestamp().toLocalDate(),
                Collectors.counting()
            ));

        // Merge data
        List<RevenueDataDTO> result = new ArrayList<>();
        LocalDate current = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();

        while (!current.isAfter(end)) {
            BigDecimal cash = cashByDate.getOrDefault(current, BigDecimal.ZERO);
            BigDecimal cost = costByDate.getOrDefault(current, BigDecimal.ZERO);
            BigDecimal profit = cash.subtract(cost);

            result.add(RevenueDataDTO.builder()
                .date(current)
                .cashCollected(cash)
                .procurementCost(cost)
                .profit(profit)
                .restockCount(restockCountByDate.getOrDefault(current, 0L).intValue())
                .build());

            current = current.plusDays(1);
        }

        return result;
    }

    /**
     * Get inventory trends over time
     */
    @Cacheable(value = "analytics-inventory", key = "'inventory-' + #startDate + '-' + #endDate")
    public List<InventoryTrendDTO> getInventoryTrends(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating inventory trends from {} to {}", startDate, endDate);

        var restockLogs = restockingLogRepository.findByTimestampBetween(startDate, endDate);
        var restockItems = restockItemRepository.findAll(); // Get all restock items

        // Group by date
        Map<LocalDate, List<Integer>> stockByDate = restockLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTimestamp().toLocalDate(),
                Collectors.mapping(
                    log -> log.getInventoryStatus() != null ? log.getInventoryStatus().getTotalProducts() : 0,
                    Collectors.toList()
                )
            ));

        Map<LocalDate, Long> lowStockByDate = restockLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTimestamp().toLocalDate(),
                Collectors.summingLong(log ->
                    log.getInventoryStatus() != null ? log.getInventoryStatus().getLowStockProducts() : 0L)
            ));

        Map<LocalDate, Long> outOfStockByDate = restockLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTimestamp().toLocalDate(),
                Collectors.summingLong(log ->
                    log.getInventoryStatus() != null ? log.getInventoryStatus().getOutOfStockProducts() : 0L)
            ));

        Map<LocalDate, Integer> restockedCountByDate = restockLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTimestamp().toLocalDate(),
                Collectors.summingInt(log -> log.getItemsRestocked().stream()
                    .mapToInt(item -> item.getQuantity())
                    .sum())
            ));

        // Build result
        List<InventoryTrendDTO> result = new ArrayList<>();
        LocalDate current = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();

        while (!current.isAfter(end)) {
            List<Integer> stocks = stockByDate.getOrDefault(current, List.of(0));
            int avgStock = stocks.isEmpty() ? 0 : (int) stocks.stream().mapToInt(Integer::intValue).average().orElse(0);

            result.add(InventoryTrendDTO.builder()
                .date(current)
                .totalStock(avgStock)
                .lowStockCount(lowStockByDate.getOrDefault(current, 0L).intValue())
                .outOfStockCount(outOfStockByDate.getOrDefault(current, 0L).intValue())
                .restockedItemsCount(restockedCountByDate.getOrDefault(current, 0))
                .build());

            current = current.plusDays(1);
        }

        return result;
    }

    /**
     * Get machine performance metrics
     */
    @Cacheable(value = "analytics-machines", key = "'machines-' + #startDate + '-' + #endDate")
    public List<MachinePerformanceDTO> getMachinePerformance(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating machine performance from {} to {}", startDate, endDate);

        return machineRepository.findAll().stream()
            .map(machine -> {
                var logs = restockingLogRepository.findByMachineAndTimestampBetween(machine, startDate, endDate);

                BigDecimal totalCash = logs.stream()
                    .map(log -> log.getCashCollected() != null ? log.getCashCollected() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                int restockCount = logs.size();
                BigDecimal avgCash = restockCount > 0
                    ? totalCash.divide(new BigDecimal(restockCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

                int totalItemsRestocked = logs.stream()
                    .mapToInt(log -> log.getItemsRestocked().stream()
                        .mapToInt(item -> item.getQuantity())
                        .sum())
                    .sum();

                long maintenanceCount = logs.stream()
                    .filter(log -> log.isMaintenancePerformed())
                    .count();

                return MachinePerformanceDTO.builder()
                    .machineId(machine.getId())
                    .machineBrand(machine.getBrand())
                    .machineModel(machine.getModel())
                    .locationAddress(machine.getLocation().getAddress())
                    .restockCount(restockCount)
                    .totalCashCollected(totalCash)
                    .averageCashPerRestock(avgCash)
                    .totalItemsRestocked(totalItemsRestocked)
                    .maintenanceCount((int) maintenanceCount)
                    .build();
            })
            .sorted((a, b) -> b.getTotalCashCollected().compareTo(a.getTotalCashCollected()))
            .collect(Collectors.toList());
    }

    /**
     * Get product analytics
     */
    @Cacheable(value = "analytics-products", key = "'products-' + #startDate + '-' + #endDate")
    public List<ProductAnalyticsDTO> getProductAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating product analytics from {} to {}", startDate, endDate);

        return productRepository.findAll().stream()
            .map(product -> {
                // Total restocked
                int totalRestocked = restockItemRepository.findByProduct(product).stream()
                    .filter(item -> {
                        var log = item.getRestockingLog();
                        return log.getTimestamp().isAfter(startDate) && log.getTimestamp().isBefore(endDate);
                    })
                    .mapToInt(item -> item.getQuantity())
                    .sum();

                // Total procured and cost
                var procurementItems = procurementItemRepository.findByProduct(product).stream()
                    .filter(item -> {
                        var batch = item.getBatch();
                        return batch.getPurchaseDate().isAfter(startDate) && batch.getPurchaseDate().isBefore(endDate);
                    })
                    .collect(Collectors.toList());

                int totalProcured = procurementItems.stream()
                    .mapToInt(item -> item.getQuantity())
                    .sum();

                BigDecimal totalCost = procurementItems.stream()
                    .map(item -> item.getUnitCost().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal avgCost = totalProcured > 0
                    ? totalCost.divide(new BigDecimal(totalProcured), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

                return ProductAnalyticsDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .category(product.getCategory())
                    .currentStock(product.getCurrentStock())
                    .totalRestocked(totalRestocked)
                    .totalProcured(totalProcured)
                    .totalProcurementCost(totalCost)
                    .averageUnitCost(avgCost)
                    .minimumStock(product.getMinimumStock())
                    .isLowStock(product.getCurrentStock() < product.getMinimumStock())
                    .build();
            })
            .sorted((a, b) -> Integer.compare(b.getTotalRestocked(), a.getTotalRestocked()))
            .collect(Collectors.toList());
    }

    /**
     * Get category breakdown
     */
    @Cacheable(value = "analytics-categories", key = "'categories-' + #startDate + '-' + #endDate")
    public List<CategoryBreakdownDTO> getCategoryBreakdown(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating category breakdown from {} to {}", startDate, endDate);

        Map<String, List<Product>> productsByCategory = productRepository.findAll().stream()
            .collect(Collectors.groupingBy(Product::getCategory));

        return productsByCategory.entrySet().stream()
            .map(entry -> {
                String category = entry.getKey();
                List<Product> products = entry.getValue();

                int productCount = products.size();
                int totalStock = products.stream().mapToInt(Product::getCurrentStock).sum();

                BigDecimal totalValue = products.stream()
                    .map(p -> {
                        BigDecimal price = p.getBasePrice() != null ? p.getBasePrice() : BigDecimal.ZERO;
                        return price.multiply(new BigDecimal(p.getCurrentStock()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Total restocked in date range
                int totalRestocked = products.stream()
                    .mapToInt(product ->
                        restockItemRepository.findByProduct(product).stream()
                            .filter(item -> {
                                var log = item.getRestockingLog();
                                return log.getTimestamp().isAfter(startDate) && log.getTimestamp().isBefore(endDate);
                            })
                            .mapToInt(item -> item.getQuantity())
                            .sum()
                    )
                    .sum();

                // Procurement cost in date range
                BigDecimal procurementCost = products.stream()
                    .map(product ->
                        procurementItemRepository.findByProduct(product).stream()
                            .filter(item -> {
                                var batch = item.getBatch();
                                return batch.getPurchaseDate().isAfter(startDate) && batch.getPurchaseDate().isBefore(endDate);
                            })
                            .map(item -> item.getUnitCost().multiply(new BigDecimal(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                return CategoryBreakdownDTO.builder()
                    .category(category)
                    .productCount(productCount)
                    .totalStock(totalStock)
                    .totalValue(totalValue)
                    .totalRestocked(totalRestocked)
                    .procurementCost(procurementCost)
                    .build();
            })
            .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
            .collect(Collectors.toList());
    }
}
