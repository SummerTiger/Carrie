package com.vending.controller;

import com.vending.dto.analytics.*;
import com.vending.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Analytics", description = "Analytics and reporting endpoints")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get analytics summary", description = "Get overall analytics summary for the specified date range")
    public ResponseEntity<AnalyticsSummaryDTO> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("GET /api/analytics/summary - Date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(analyticsService.getSummary(startDate, endDate));
    }

    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get revenue data", description = "Get revenue data over time for charts")
    public ResponseEntity<List<RevenueDataDTO>> getRevenueData(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("GET /api/analytics/revenue - Date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(analyticsService.getRevenueData(startDate, endDate));
    }

    @GetMapping("/inventory-trends")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get inventory trends", description = "Get inventory trends over time for charts")
    public ResponseEntity<List<InventoryTrendDTO>> getInventoryTrends(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("GET /api/analytics/inventory-trends - Date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(analyticsService.getInventoryTrends(startDate, endDate));
    }

    @GetMapping("/machine-performance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get machine performance", description = "Get performance metrics for all machines")
    public ResponseEntity<List<MachinePerformanceDTO>> getMachinePerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("GET /api/analytics/machine-performance - Date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(analyticsService.getMachinePerformance(startDate, endDate));
    }

    @GetMapping("/product-analytics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get product analytics", description = "Get analytics for all products")
    public ResponseEntity<List<ProductAnalyticsDTO>> getProductAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("GET /api/analytics/product-analytics - Date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(analyticsService.getProductAnalytics(startDate, endDate));
    }

    @GetMapping("/category-breakdown")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get category breakdown", description = "Get breakdown of products by category")
    public ResponseEntity<List<CategoryBreakdownDTO>> getCategoryBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        log.info("GET /api/analytics/category-breakdown - Date range: {} to {}", startDate, endDate);
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(startDate, endDate));
    }
}
