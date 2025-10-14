package com.vending.service;

import com.vending.entity.AuditLog;
import com.vending.entity.User;
import com.vending.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Async
    @Transactional
    public void log(String action, String resourceType, String resourceId, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details)
                    .status(AuditLog.STATUS_SUCCESS)
                    .build();

            enrichWithUserInfo(auditLog);
            enrichWithRequestInfo(auditLog);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log silently to not disrupt the main operation
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    @Async
    @Transactional
    public void logSuccess(String action, String resourceType, String resourceId, String details) {
        log(action, resourceType, resourceId, details, AuditLog.STATUS_SUCCESS, null);
    }

    @Async
    @Transactional
    public void logFailure(String action, String resourceType, String resourceId, String details, String errorMessage) {
        log(action, resourceType, resourceId, details, AuditLog.STATUS_FAILURE, errorMessage);
    }

    @Async
    @Transactional
    public void log(String action, String resourceType, String resourceId, String details, String status, String errorMessage) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .details(details)
                    .status(status)
                    .errorMessage(errorMessage)
                    .build();

            enrichWithUserInfo(auditLog);
            enrichWithRequestInfo(auditLog);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log silently to not disrupt the main operation
            System.err.println("Failed to create audit log: " + e.getMessage());
        }
    }

    @Transactional
    public void logSync(String action, String resourceType, String resourceId, String details) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .status(AuditLog.STATUS_SUCCESS)
                .build();

        enrichWithUserInfo(auditLog);
        enrichWithRequestInfo(auditLog);

        auditLogRepository.save(auditLog);
    }

    private void enrichWithUserInfo(AuditLog auditLog) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof User) {
                    User user = (User) principal;
                    auditLog.setUser(user);
                    auditLog.setUsername(user.getUsername());
                } else {
                    auditLog.setUsername(authentication.getName());
                }
            } else {
                auditLog.setUsername("anonymous");
            }
        } catch (Exception e) {
            auditLog.setUsername("system");
        }
    }

    private void enrichWithRequestInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIP(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // Request context not available (async call, etc.)
        }
    }

    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    // Query methods
    public Page<AuditLog> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    public Page<AuditLog> findByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }

    public Page<AuditLog> findByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    public Page<AuditLog> findByResourceType(String resourceType, Pageable pageable) {
        return auditLogRepository.findByResourceType(resourceType, pageable);
    }

    public Page<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startDate, endDate, pageable);
    }

    @Transactional
    public int cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        return auditLogRepository.deleteOldLogs(cutoffDate);
    }
}
