package com.integrixs.data.repository;

import com.integrixs.data.model.Alert;
import com.integrixs.data.model.AlertRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Alert entities
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Find alert by alert ID
     */
    Optional<Alert> findByAlertId(String alertId);

    /**
     * Find alerts by status
     */
    Page<Alert> findByStatus(Alert.AlertStatus status, Pageable pageable);

    /**
     * Find unresolved alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.status NOT IN('RESOLVED', 'EXPIRED')")
    Page<Alert> findUnresolvedAlerts(Pageable pageable);

    /**
     * Find alerts by rule
     */
    Page<Alert> findByAlertRule(AlertRule alertRule, Pageable pageable);

    /**
     * Find alerts by source
     */
    Page<Alert> findBySourceTypeAndSourceId(Alert.SourceType sourceType, String sourceId, Pageable pageable);

    /**
     * Find alerts triggered in time range
     */
    Page<Alert> findByTriggeredAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Find alerts by severity
     */
    Page<Alert> findBySeverity(AlertRule.AlertSeverity severity, Pageable pageable);

    /**
     * Find unacknowledged critical alerts
     */
    @Query("SELECT a FROM Alert a WHERE a.severity = 'CRITICAL' AND a.status = 'TRIGGERED'")
    List<Alert> findUnacknowledgedCriticalAlerts();

    /**
     * Find alerts needing escalation
     */
    @Query("SELECT a FROM Alert a JOIN a.alertRule ar WHERE " +
           "a.escalated = false AND ar.escalationEnabled = true AND " +
           "a.status IN('TRIGGERED', 'NOTIFIED') AND " +
           "a.triggeredAt < :escalationTime")
    List<Alert> findAlertsNeedingEscalation(@Param("escalationTime") LocalDateTime escalationTime);

    /**
     * Find suppressed alerts ready to unsuppress
     */
    @Query("SELECT a FROM Alert a WHERE a.suppressed = true AND a.suppressedUntil < :now")
    List<Alert> findSuppressedAlertsToUnsuppress(@Param("now") LocalDateTime now);

    /**
     * Count alerts by status and severity
     */
    @Query("SELECT a.status, a.severity, COUNT(a) FROM Alert a " +
           "WHERE a.triggeredAt >= :since " +
           "GROUP BY a.status, a.severity")
    List<Object[]> countByStatusAndSeveritySince(@Param("since") LocalDateTime since);

    /**
     * Find recent alerts for deduplication
     */
    @Query("SELECT a FROM Alert a WHERE a.alertRule = :rule AND " +
           "a.sourceId = :sourceId AND a.status != 'RESOLVED' AND " +
           "a.triggeredAt > :since")
    List<Alert> findRecentSimilarAlerts(@Param("rule") AlertRule rule,
                                       @Param("sourceId") String sourceId,
                                       @Param("since") LocalDateTime since);

    /**
     * Check if alert ID exists
     */
    boolean existsByAlertId(String alertId);
}
