package com.integrixs.monitoring.domain.service;

import com.integrixs.monitoring.domain.model.Alert;
import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.model.MonitoringEvent;

import java.util.List;

/**
 * Domain service interface for monitoring-specific alerts
 * Handles alerts based on metrics and monitoring events
 */
public interface MonitoringAlertService {

    /**
     * Create and trigger an alert
     * @param alert Alert to trigger
     * @return Triggered alert with ID
     */
    Alert triggerAlert(Alert alert);

    /**
     * Evaluate metric against alert rules
     * @param metric Metric to evaluate
     * @return List of triggered alerts
     */
    List<Alert> evaluateMetric(MetricSnapshot metric);

    /**
     * Evaluate event against alert rules
     * @param event Event to evaluate
     * @return List of triggered alerts
     */
    List<Alert> evaluateEvent(MonitoringEvent event);

    /**
     * Acknowledge an alert
     * @param alertId Alert ID
     * @param userId User acknowledging the alert
     * @return Updated alert
     */
    Alert acknowledgeAlert(String alertId, String userId);

    /**
     * Resolve an alert
     * @param alertId Alert ID
     * @param resolution Resolution message
     * @return Updated alert
     */
    Alert resolveAlert(String alertId, String resolution);

    /**
     * Suppress an alert
     * @param alertId Alert ID
     * @param reason Suppression reason
     * @param duration Suppression duration in minutes
     * @return Updated alert
     */
    Alert suppressAlert(String alertId, String reason, int duration);

    /**
     * Get active alerts
     * @return List of active alerts
     */
    List<Alert> getActiveAlerts();

    /**
     * Get alerts by severity
     * @param severity Minimum severity
     * @return List of alerts
     */
    List<Alert> getAlertsBySeverity(Alert.AlertSeverity severity);

    /**
     * Get alert by ID
     * @param alertId Alert ID
     * @return Alert if found
     */
    Alert getAlert(String alertId);

    /**
     * Query alerts
     * @param criteria Query criteria
     * @return List of matching alerts
     */
    List<Alert> queryAlerts(AlertQueryCriteria criteria);

    /**
     * Create alert rule
     * @param rule Alert rule definition
     * @return Created rule ID
     */
    String createAlertRule(AlertRule rule);

    /**
     * Update alert rule
     * @param ruleId Rule ID
     * @param rule Updated rule definition
     */
    void updateAlertRule(String ruleId, AlertRule rule);

    /**
     * Delete alert rule
     * @param ruleId Rule ID
     */
    void deleteAlertRule(String ruleId);

    /**
     * Get all alert rules
     * @return List of alert rules
     */
    List<AlertRule> getAlertRules();

    /**
     * Execute alert action
     * @param alert Alert to process
     */
    void executeAlertAction(Alert alert);

    /**
     * Alert rule definition
     */
    class AlertRule {
        private String ruleId;
        private String ruleName;
        private String condition;
        private Alert.AlertType alertType;
        private Alert.AlertSeverity severity;
        private Alert.AlertAction action;
        private boolean enabled;
        private int evaluationInterval;
        private String targetMetric;
        private double threshold;
        private String comparison; // GT, LT, EQ, etc.

        // Getters and setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }

        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public Alert.AlertType getAlertType() { return alertType; }
        public void setAlertType(Alert.AlertType alertType) { this.alertType = alertType; }

        public Alert.AlertSeverity getSeverity() { return severity; }
        public void setSeverity(Alert.AlertSeverity severity) { this.severity = severity; }

        public Alert.AlertAction getAction() { return action; }
        public void setAction(Alert.AlertAction action) { this.action = action; }

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public int getEvaluationInterval() { return evaluationInterval; }
        public void setEvaluationInterval(int evaluationInterval) { this.evaluationInterval = evaluationInterval; }

        public String getTargetMetric() { return targetMetric; }
        public void setTargetMetric(String targetMetric) { this.targetMetric = targetMetric; }

        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }

        public String getComparison() { return comparison; }
        public void setComparison(String comparison) { this.comparison = comparison; }
    }

    /**
     * Alert query criteria
     */
    class AlertQueryCriteria {
        private Alert.AlertType alertType;
        private Alert.AlertSeverity minSeverity;
        private Alert.AlertStatus status;
        private String domainType;
        private String domainReferenceId;
        private Long startTime;
        private Long endTime;
        private Integer limit;

        // Getters and setters
        public Alert.AlertType getAlertType() { return alertType; }
        public void setAlertType(Alert.AlertType alertType) { this.alertType = alertType; }

        public Alert.AlertSeverity getMinSeverity() { return minSeverity; }
        public void setMinSeverity(Alert.AlertSeverity minSeverity) { this.minSeverity = minSeverity; }

        public Alert.AlertStatus getStatus() { return status; }
        public void setStatus(Alert.AlertStatus status) { this.status = status; }

        public String getDomainType() { return domainType; }
        public void setDomainType(String domainType) { this.domainType = domainType; }

        public String getDomainReferenceId() { return domainReferenceId; }
        public void setDomainReferenceId(String domainReferenceId) { this.domainReferenceId = domainReferenceId; }

        public Long getStartTime() { return startTime; }
        public void setStartTime(Long startTime) { this.startTime = startTime; }

        public Long getEndTime() { return endTime; }
        public void setEndTime(Long endTime) { this.endTime = endTime; }

        public Integer getLimit() { return limit; }
        public void setLimit(Integer limit) { this.limit = limit; }
    }
}
