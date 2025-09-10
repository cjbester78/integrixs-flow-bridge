package com.integrixs.backend.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for audit events
 */
@Repository
public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    
    /**
     * Find events by user
     */
    Page<AuditEvent> findByUsernameOrderByEventTimestampDesc(String username, Pageable pageable);
    
    /**
     * Find events by entity
     */
    Page<AuditEvent> findByEntityTypeAndEntityIdOrderByEventTimestampDesc(
        String entityType, String entityId, Pageable pageable);
    
    /**
     * Find events by type
     */
    Page<AuditEvent> findByEventTypeOrderByEventTimestampDesc(
        AuditEvent.AuditEventType eventType, Pageable pageable);
    
    /**
     * Find events by category
     */
    Page<AuditEvent> findByCategoryOrderByEventTimestampDesc(
        AuditEvent.AuditCategory category, Pageable pageable);
    
    /**
     * Find events within time range
     */
    Page<AuditEvent> findByEventTimestampBetweenOrderByEventTimestampDesc(
        Instant start, Instant end, Pageable pageable);
    
    /**
     * Find failed events
     */
    Page<AuditEvent> findByOutcomeInOrderByEventTimestampDesc(
        List<AuditEvent.AuditOutcome> outcomes, Pageable pageable);
    
    /**
     * Find events by tenant
     */
    Page<AuditEvent> findByTenantIdOrderByEventTimestampDesc(String tenantId, Pageable pageable);
    
    /**
     * Complex search query
     */
    @Query("SELECT a FROM AuditEvent a WHERE " +
           "(:username IS NULL OR a.username = :username) AND " +
           "(:eventType IS NULL OR a.eventType = :eventType) AND " +
           "(:category IS NULL OR a.category = :category) AND " +
           "(:outcome IS NULL OR a.outcome = :outcome) AND " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:tenantId IS NULL OR a.tenantId = :tenantId) AND " +
           "(:startTime IS NULL OR a.eventTimestamp >= :startTime) AND " +
           "(:endTime IS NULL OR a.eventTimestamp <= :endTime) " +
           "ORDER BY a.eventTimestamp DESC")
    Page<AuditEvent> searchAuditEvents(
        @Param("username") String username,
        @Param("eventType") AuditEvent.AuditEventType eventType,
        @Param("category") AuditEvent.AuditCategory category,
        @Param("outcome") AuditEvent.AuditOutcome outcome,
        @Param("entityType") String entityType,
        @Param("tenantId") String tenantId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        Pageable pageable
    );
    
    /**
     * Find security events
     */
    @Query("SELECT a FROM AuditEvent a WHERE " +
           "a.category = 'SECURITY' OR " +
           "a.eventType IN ('LOGIN_FAILURE', 'ACCESS_DENIED', 'RATE_LIMIT_EXCEEDED', " +
           "'SUSPICIOUS_ACTIVITY', 'SECURITY_ALERT') " +
           "ORDER BY a.eventTimestamp DESC")
    Page<AuditEvent> findSecurityEvents(Pageable pageable);
    
    /**
     * Count events by type in time range
     */
    @Query("SELECT a.eventType, COUNT(a) FROM AuditEvent a " +
           "WHERE a.eventTimestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY a.eventType")
    List<Object[]> countEventsByType(
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );
    
    /**
     * Find recent failed login attempts
     */
    @Query("SELECT a FROM AuditEvent a WHERE " +
           "a.eventType = 'LOGIN_FAILURE' AND " +
           "a.ipAddress = :ipAddress AND " +
           "a.eventTimestamp >= :since")
    List<AuditEvent> findRecentFailedLogins(
        @Param("ipAddress") String ipAddress,
        @Param("since") Instant since
    );
    
    /**
     * Delete old audit events
     */
    void deleteByEventTimestampBefore(Instant cutoffTime);
    
    /**
     * Find events by correlation ID
     */
    List<AuditEvent> findByCorrelationIdOrderByEventTimestamp(String correlationId);
}