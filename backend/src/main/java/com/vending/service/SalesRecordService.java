package com.vending.service;

import com.vending.dto.SalesRecordDTO;
import com.vending.entity.SalesRecord;
import com.vending.exception.ResourceNotFoundException;
import com.vending.repository.SalesRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesRecordService {

    private final SalesRecordRepository salesRecordRepository;

    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d/yyyy");
    private static final String[] CSV_EXPECTED_HEADERS = {"Settlement Date", "# of Batches", "# Completed", "# Sur", "# Incomplete", "Approved Amount", "Fee Amount"};

    public List<SalesRecordDTO> getAllSalesRecords() {
        return salesRecordRepository.findAllByOrderBySettlementDateDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SalesRecordDTO> getSalesRecordsByDateRange(LocalDate startDate, LocalDate endDate) {
        return salesRecordRepository.findBySettlementDateBetweenOrderBySettlementDateDesc(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<SalesRecordDTO> getSalesRecordsBySource(String source) {
        return salesRecordRepository.findBySourceOrderBySettlementDateDesc(source).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public SalesRecordDTO getSalesRecordById(UUID id) {
        SalesRecord record = salesRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales record not found with id: " + id));
        return convertToDTO(record);
    }

    @Transactional
    public List<SalesRecordDTO> importFromCSV(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        log.info("Starting CSV import from file: {}", fileName);

        List<SalesRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file is empty");
            }

            String line;
            int lineNumber = 2; // Start at 2 (header is line 1)

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    lineNumber++;
                    continue;
                }

                try {
                    SalesRecord record = parseCSVLine(line, fileName);
                    records.add(record);
                } catch (Exception e) {
                    log.warn("Skipping line {} due to error: {}", lineNumber, e.getMessage());
                }
                lineNumber++;
            }
        }

        List<SalesRecord> savedRecords = salesRecordRepository.saveAll(records);
        log.info("Successfully imported {} records from CSV file: {}", savedRecords.size(), fileName);

        return savedRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<SalesRecordDTO> importFromExcel(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        log.info("Starting Excel import from file: {}", fileName);

        List<SalesRecord> records = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();

            // Skip header row (row 0)
            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                try {
                    SalesRecord record = parseExcelRow(row, fileName);
                    if (record != null) {
                        records.add(record);
                    }
                } catch (Exception e) {
                    log.warn("Skipping row {} due to error: {}", rowIndex + 1, e.getMessage());
                }
            }
        }

        List<SalesRecord> savedRecords = salesRecordRepository.saveAll(records);
        log.info("Successfully imported {} records from Excel file: {}", savedRecords.size(), fileName);

        return savedRecords.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSalesRecord(UUID id) {
        SalesRecord record = salesRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales record not found with id: " + id));
        salesRecordRepository.delete(record);
        log.info("Deleted sales record with id: {}", id);
    }

    private SalesRecord parseCSVLine(String line, String fileName) {
        // Remove BOM if present
        if (line.startsWith("\uFEFF")) {
            line = line.substring(1);
        }

        // Parse CSV line (handle quoted values)
        List<String> values = parseCSVValues(line);

        if (values.size() < 7) {
            throw new IllegalArgumentException("Invalid CSV line format");
        }

        SalesRecord record = new SalesRecord();
        record.setSource("CSV");
        record.setFileName(fileName);

        // Parse date (e.g., "10/1/2025")
        try {
            LocalDate date = LocalDate.parse(values.get(0).replace("\"", ""), CSV_DATE_FORMATTER);
            record.setSettlementDate(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + values.get(0));
        }

        // Parse numbers
        record.setNumberOfBatches(parseInteger(values.get(1)));
        record.setNumberCompleted(parseInteger(values.get(2)));
        record.setNumberSur(parseInteger(values.get(3)));
        record.setNumberIncomplete(parseInteger(values.get(4)));

        // Parse amounts (e.g., "$6.75")
        record.setApprovedAmount(parseAmount(values.get(5)));
        record.setFeeAmount(parseAmount(values.get(6)));

        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return record;
    }

    private SalesRecord parseExcelRow(Row row, String fileName) {
        // Check if row is empty
        if (isRowEmpty(row)) {
            return null;
        }

        SalesRecord record = new SalesRecord();
        record.setSource("EXCEL");
        record.setFileName(fileName);

        // Parse date from first cell
        Cell dateCell = row.getCell(0);
        if (dateCell != null) {
            if (dateCell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(dateCell)) {
                record.setSettlementDate(dateCell.getLocalDateTimeCellValue().toLocalDate());
            } else if (dateCell.getCellType() == CellType.STRING) {
                try {
                    LocalDate date = LocalDate.parse(dateCell.getStringCellValue(), CSV_DATE_FORMATTER);
                    record.setSettlementDate(date);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid date format in Excel");
                }
            }
        }

        // Parse numeric columns
        record.setNumberOfBatches(getIntegerCellValue(row.getCell(1)));
        record.setNumberCompleted(getIntegerCellValue(row.getCell(2)));
        record.setNumberSur(getIntegerCellValue(row.getCell(3)));
        record.setNumberIncomplete(getIntegerCellValue(row.getCell(4)));

        // Parse amount columns
        record.setApprovedAmount(getAmountCellValue(row.getCell(5)));
        record.setFeeAmount(getAmountCellValue(row.getCell(6)));

        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());

        return record;
    }

    private List<String> parseCSVValues(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                insideQuotes = !insideQuotes;
                currentValue.append(c);
            } else if (c == ',' && !insideQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString().trim());

        return values;
    }

    private Integer parseInteger(String value) {
        try {
            value = value.replace("\"", "").trim();
            return value.isEmpty() ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private BigDecimal parseAmount(String value) {
        try {
            value = value.replace("\"", "").replace("$", "").replace(",", "").trim();
            return value.isEmpty() ? BigDecimal.ZERO : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Integer getIntegerCellValue(Cell cell) {
        if (cell == null) {
            return 0;
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> (int) cell.getNumericCellValue();
            case STRING -> parseInteger(cell.getStringCellValue());
            default -> 0;
        };
    }

    private BigDecimal getAmountCellValue(Cell cell) {
        if (cell == null) {
            return BigDecimal.ZERO;
        }

        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
            case STRING -> parseAmount(cell.getStringCellValue());
            default -> BigDecimal.ZERO;
        };
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private SalesRecordDTO convertToDTO(SalesRecord record) {
        return SalesRecordDTO.builder()
                .id(record.getId())
                .settlementDate(record.getSettlementDate())
                .source(record.getSource())
                .numberOfBatches(record.getNumberOfBatches())
                .numberCompleted(record.getNumberCompleted())
                .numberSur(record.getNumberSur())
                .numberIncomplete(record.getNumberIncomplete())
                .approvedAmount(record.getApprovedAmount())
                .feeAmount(record.getFeeAmount())
                .fileName(record.getFileName())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
