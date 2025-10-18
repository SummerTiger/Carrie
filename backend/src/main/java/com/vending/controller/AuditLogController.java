package com.vending.controller;

import com.vending.entity.AuditLog;
import com.vending.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    // Security: ADMIN-only access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AuditLog> auditLogs = auditLogService.findAll(pageable);
        return ResponseEntity.ok(auditLogs);
    }

    // Security: ADMIN-only access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/{username}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByUsername(
            @PathVariable String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogService.findByUsername(username, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    // Security: ADMIN-only access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/action/{action}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByAction(
            @PathVariable String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogService.findByAction(action, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    // Security: ADMIN-only access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/resource/{resourceType}")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByResourceType(
            @PathVariable String resourceType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogService.findByResourceType(resourceType, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    // Security: ADMIN-only access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/date-range")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogService.findByTimestampBetween(startDate, endDate, pageable);
        return ResponseEntity.ok(auditLogs);
    }

    // Security: ADMIN-only access
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recent")
    public ResponseEntity<List<AuditLog>> getRecentAuditLogs() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditLogService.findAll(pageable);
        return ResponseEntity.ok(auditLogs.getContent());
    }

    // Security: ADMIN-only access
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/cleanup")
    public ResponseEntity<String> cleanupOldLogs(@RequestParam(defaultValue = "90") int daysToKeep) {
        int deletedCount = auditLogService.cleanupOldLogs(daysToKeep);
        return ResponseEntity.ok("Cleaned up " + deletedCount + " old audit log entries");
    }
}
