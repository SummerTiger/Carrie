package com.vending.controller;

import com.vending.dto.SalesRecordDTO;
import com.vending.service.SalesRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Sales Records", description = "Sales record import and management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class SalesRecordController {

    private final SalesRecordService salesRecordService;

    @GetMapping
    @Operation(summary = "Get all sales records")
    public ResponseEntity<List<SalesRecordDTO>> getAllSalesRecords() {
        return ResponseEntity.ok(salesRecordService.getAllSalesRecords());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sales record by ID")
    public ResponseEntity<SalesRecordDTO> getSalesRecordById(@PathVariable UUID id) {
        return ResponseEntity.ok(salesRecordService.getSalesRecordById(id));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get sales records by date range")
    public ResponseEntity<List<SalesRecordDTO>> getSalesRecordsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(salesRecordService.getSalesRecordsByDateRange(startDate, endDate));
    }

    @GetMapping("/source/{source}")
    @Operation(summary = "Get sales records by source")
    public ResponseEntity<List<SalesRecordDTO>> getSalesRecordsBySource(@PathVariable String source) {
        return ResponseEntity.ok(salesRecordService.getSalesRecordsBySource(source));
    }

    @PostMapping("/import/csv")
    @Operation(summary = "Import sales records from CSV file")
    public ResponseEntity<List<SalesRecordDTO>> importFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
                return ResponseEntity.badRequest().build();
            }

            List<SalesRecordDTO> records = salesRecordService.importFromCSV(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(records);
        } catch (Exception e) {
            log.error("Error importing CSV file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import/excel")
    @Operation(summary = "Import sales records from Excel file")
    public ResponseEntity<List<SalesRecordDTO>> importFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !(fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
                return ResponseEntity.badRequest().build();
            }

            List<SalesRecordDTO> records = salesRecordService.importFromExcel(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(records);
        } catch (Exception e) {
            log.error("Error importing Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sales record")
    public ResponseEntity<Void> deleteSalesRecord(@PathVariable UUID id) {
        salesRecordService.deleteSalesRecord(id);
        return ResponseEntity.noContent().build();
    }
}
