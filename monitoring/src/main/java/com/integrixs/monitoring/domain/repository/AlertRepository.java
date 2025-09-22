package com.integrixs.monitoring.domain.repository;

import com.integrixs.monitoring.domain.model.Alert;
import com.integrixs.monitoring.domain.service.MonitoringAlertService.AlertQueryCriteria;
import com.integrixs.monitoring.domain.service.MonitoringAlertService.AlertRule;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for alerts
 */
public interface AlertRepository {

    /**
     * Save an alert
     * @param alert Alert to save
     * @return Saved alert
     */
    Alert save(Alert alert);

    /**
     * Find alert by ID
     * @param alertId Alert ID
     * @return Alert if found
     */
    Optional<Alert> findById(String alertId);

    /**
     * Find active alerts
     * @return List of active alerts
     */
    List<Alert> findActiveAlerts();

    /**
     * Find alerts by severity
     * @param minSeverity Minimum severity
     * @return List of alerts
     */
    List<Alert> findBySeverity(Alert.AlertSeverity minSeverity);

    /**
     * Find alerts by domain reference
     * @param domainType Domain type
     * @param domainReferenceId Domain reference ID
     * @return List of alerts
     */
    List<Alert> findByDomainReference(String domainType, String domainReferenceId);

    /**
     * Query alerts based on criteria
     * @param criteria Query criteria
     * @return List of matching alerts
     */
    List<Alert> query(AlertQueryCriteria criteria);

    /**
     * Count alerts matching criteria
     * @param criteria Query criteria
     * @return Alert count
     */
    long count(AlertQueryCriteria criteria);

    /**
     * Delete resolved alerts older than specified days
     * @param retentionDays Number of days to retain
     * @return Number of deleted alerts
     */
    long deleteResolvedOlderThan(int retentionDays);

    // Alert Rule operations

    /**
     * Save alert rule
     * @param rule Alert rule
     * @return Saved rule
     */
    AlertRule saveRule(AlertRule rule);

    /**
     * Find alert rule by ID
     * @param ruleId Rule ID
     * @return Rule if found
     */
    Optional<AlertRule> findRuleById(String ruleId);

    /**
     * Find all alert rules
     * @return List of all rules
     */
    List<AlertRule> findAllRules();

    /**
     * Find enabled alert rules
     * @return List of enabled rules
     */
    List<AlertRule> findEnabledRules();

    /**
     * Delete alert rule
     * @param ruleId Rule ID
     */
    void deleteRule(String ruleId);

    /**
     * Get alert statistics
     * @param startTime Start time
     * @param endTime End time
     * @return Statistics
     */
    AlertStatistics getStatistics(long startTime, long endTime);

    /**
     * Alert statistics
     */
    class AlertStatistics {
        private long totalAlerts;
        private long activeAlerts;
        private long acknowledgedAlerts;
        private long resolvedAlerts;
        private long criticalAlerts;
        private long majorAlerts;
        private long minorAlerts;
        private long warningAlerts;
        private double averageResolutionTime;
        private double averageAcknowledgmentTime;

        // Getters and setters
        public long getTotalAlerts() { return totalAlerts; }
        public void setTotalAlerts(long totalAlerts) { this.totalAlerts = totalAlerts; }

        public long getActiveAlerts() { return activeAlerts; }
        public void setActiveAlerts(long activeAlerts) { this.activeAlerts = activeAlerts; }

        public long getAcknowledgedAlerts() { return acknowledgedAlerts; }
        public void setAcknowledgedAlerts(long acknowledgedAlerts) { this.acknowledgedAlerts = acknowledgedAlerts; }

        public long getResolvedAlerts() { return resolvedAlerts; }
        public void setResolvedAlerts(long resolvedAlerts) { this.resolvedAlerts = resolvedAlerts; }

        public long getCriticalAlerts() { return criticalAlerts; }
        public void setCriticalAlerts(long criticalAlerts) { this.criticalAlerts = criticalAlerts; }

        public long getMajorAlerts() { return majorAlerts; }
        public void setMajorAlerts(long majorAlerts) { this.majorAlerts = majorAlerts; }

        public long getMinorAlerts() { return minorAlerts; }
        public void setMinorAlerts(long minorAlerts) { this.minorAlerts = minorAlerts; }

        public long getWarningAlerts() { return warningAlerts; }
        public void setWarningAlerts(long warningAlerts) { this.warningAlerts = warningAlerts; }

        public double getAverageResolutionTime() { return averageResolutionTime; }
        public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }

        public double getAverageAcknowledgmentTime() { return averageAcknowledgmentTime; }
        public void setAverageAcknowledgmentTime(double averageAcknowledgmentTime) { this.averageAcknowledgmentTime = averageAcknowledgmentTime; }
    }
}
