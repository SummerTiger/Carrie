package com.vending.repository;

import com.vending.entity.AuditLog;
import com.vending.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    Page<AuditLog> findByUser(User user, Pageable pageable);

    Page<AuditLog> findByUsername(String username, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByResourceType(String resourceType, Pageable pageable);

    Page<AuditLog> findByResourceTypeAndResourceId(String resourceType, String resourceId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    Page<AuditLog> findByTimestampBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT al FROM AuditLog al WHERE al.username = :username AND al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    Page<AuditLog> findByUsernameAndTimestampBetween(
            @Param("username") String username,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT al FROM AuditLog al WHERE al.action = :action AND al.timestamp BETWEEN :startDate AND :endDate ORDER BY al.timestamp DESC")
    Page<AuditLog> findByActionAndTimestampBetween(
            @Param("action") String action,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT al FROM AuditLog al WHERE al.status = :status ORDER BY al.timestamp DESC")
    Page<AuditLog> findByStatus(@Param("status") String status, Pageable pageable);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.action = :action AND al.timestamp > :since")
    long countByActionSince(@Param("action") String action, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(al) FROM AuditLog al WHERE al.username = :username AND al.action = :action AND al.timestamp > :since")
    long countByUsernameAndActionSince(
            @Param("username") String username,
            @Param("action") String action,
            @Param("since") LocalDateTime since
    );

    @Modifying
    @Query("DELETE FROM AuditLog al WHERE al.timestamp < :cutoffDate")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);

    List<AuditLog> findTop10ByOrderByTimestampDesc();
}
