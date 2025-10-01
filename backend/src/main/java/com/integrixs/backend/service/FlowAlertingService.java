package com.integrixs.backend.service;

import com.integrixs.data.model.*;
import com.integrixs.data.sql.repository.AlertRuleSqlRepository;
import com.integrixs.data.sql.repository.AlertSqlRepository;
import com.integrixs.data.sql.repository.NotificationChannelSqlRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing flow-related alerts and alert rules
 * Implements the FlowAlertingPort interface for the engine module
 */
@Service
public class FlowAlertingService {

    private static final Logger log = LoggerFactory.getLogger(FlowAlertingService.class);


    private final AlertRuleSqlRepository alertRuleRepository;
    private final AlertSqlRepository alertRepository;
    private final NotificationChannelSqlRepository notificationChannelRepository;
    private final NotificationService notificationService;
    private final SystemLogService systemLogService;

    public FlowAlertingService(AlertRuleSqlRepository alertRuleRepository,
                               AlertSqlRepository alertRepository,
                               NotificationChannelSqlRepository notificationChannelRepository,
                               NotificationService notificationService,
                               SystemLogService systemLogService) {
        this.alertRuleRepository = alertRuleRepository;
        this.alertRepository = alertRepository;
        this.notificationChannelRepository = notificationChannelRepository;
        this.notificationService = notificationService;
        this.systemLogService = systemLogService;
    }

    /**
     * Evaluate alert rules for a flow execution
     */
    public void evaluateFlowAlerts(FlowExecution flowExecution) {
        try {
            List<AlertRule> rules = alertRuleRepository.findActiveRulesForFlow(
                flowExecution.getFlow().getId().toString());

            for(AlertRule rule : rules) {
                evaluateRule(rule, flowExecution);
            }
        } catch(Exception e) {
            log.error("Error evaluating flow alerts for execution {}", flowExecution.getId(), e);
        }
    }

    /**
     * Evaluate alert rules for an adapter
     */
    public void evaluateAdapterAlerts(String adapterId, Map<String, Object> metrics) {
        try {
            List<AlertRule> rules = alertRuleRepository.findActiveRulesForAdapter(adapterId);

            for(AlertRule rule : rules) {
                evaluateRule(rule, adapterId, metrics);
            }
        } catch(Exception e) {
            log.error("Error evaluating adapter alerts for {}", adapterId, e);
        }
    }

    /**
     * Evaluate system - wide alert rules
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void evaluateSystemAlerts() {
        try {
            List<AlertRule> rules = alertRuleRepository.findSystemWideRules();

            for(AlertRule rule : rules) {
                evaluateSystemRule(rule);
            }
        } catch(Exception e) {
            log.error("Error evaluating system alerts", e);
        }
    }

    /**
     * Create an alert
     */
    public Alert createAlert(AlertRule rule, String title, String message,
                           Alert.SourceType sourceType, String sourceId,
                           Map<String, String> details) {

        // Check for duplicate alerts
        if(isDuplicateAlert(rule, sourceId)) {
            log.debug("Skipping duplicate alert for rule {} and source {}", rule.getRuleName(), sourceId);
            return null;
        }

        // Create alert
        Alert alert = Alert.builder()
                .alertRule(rule)
                .alertId(generateAlertId())
                .title(title)
                .message(message)
                .severity(rule.getSeverity())
                .status(Alert.AlertStatus.TRIGGERED)
                .triggeredAt(LocalDateTime.now())
                .sourceType(sourceType)
                .sourceId(sourceId)
                .sourceName(getSourceName(sourceType, sourceId))
                .details(details != null ? details : new HashMap<>())
                .build();

        alert = alertRepository.save(alert);

        // Update rule trigger info
        rule.setLastTriggeredAt(LocalDateTime.now());
        rule.setTriggerCount(rule.getTriggerCount() + 1);
        alertRuleRepository.save(rule);

        // Send notifications
        sendAlertNotifications(alert);

        // Log alert
        // TODO: Implement alert logging in SystemLogService
        // systemLogService.logAlert(alert);

        return alert;
    }

    /**
     * Acknowledge an alert
     */
    public Alert acknowledgeAlert(String alertId, String userId, String notes) {
        Alert alert = alertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.acknowledge(userId);
        if(notes != null) {
            alert.addDetail("acknowledgment_notes", notes);
        }

        return alertRepository.save(alert);
    }

    /**
     * Resolve an alert
     */
    public Alert resolveAlert(String alertId, String userId, String resolutionNotes) {
        Alert alert = alertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.resolve(userId, resolutionNotes);

        return alertRepository.save(alert);
    }

    /**
     * Suppress an alert
     */
    public Alert suppressAlert(String alertId, LocalDateTime until, String reason) {
        Alert alert = alertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));

        alert.suppress(until, reason);

        return alertRepository.save(alert);
    }

    /**
     * Process alert escalations
     */
    @Scheduled(fixedDelay = 300000) // Every 5 minutes
    public void processEscalations() {
        LocalDateTime escalationTime = LocalDateTime.now().minusMinutes(15); // Default 15 min
        List<Alert> alertsToEscalate = alertRepository.findAlertsNeedingEscalation(escalationTime);

        for(Alert alert : alertsToEscalate) {
            escalateAlert(alert);
        }
    }

    /**
     * Process suppressed alerts
     */
    @Scheduled(fixedDelay = 60000) // Every minute
    public void processSuppressedAlerts() {
        List<Alert> alertsToUnsuppress = alertRepository.findSuppressedAlertsToUnsuppress(LocalDateTime.now());

        for(Alert alert : alertsToUnsuppress) {
            alert.setSuppressed(false);
            alert.setSuppressedUntil(null);
            alert.setStatus(Alert.AlertStatus.TRIGGERED);
            alertRepository.save(alert);

            // Re - send notifications
            sendAlertNotifications(alert);
        }
    }

    /**
     * Evaluate a rule for flow execution
     */
    private void evaluateRule(AlertRule rule, FlowExecution flowExecution) {
        boolean shouldAlert = false;
        String title = "";
        String message = "";
        Map<String, String> details = new HashMap<>();

        switch(rule.getAlertType()) {
            case FLOW_FAILURE:
                if(flowExecution.getStatus() == FlowExecution.ExecutionStatus.FAILED) {
                    shouldAlert = true;
                    title = "Flow Execution Failed";
                    message = String.format("Flow '%s' failed with error: %s",
                            flowExecution.getFlow().getName(),
                            flowExecution.getErrorMessage());
                    details.put("flow_id", flowExecution.getFlow().getId().toString());
                    details.put("execution_id", flowExecution.getId().toString());
                    details.put("error", flowExecution.getErrorMessage());
                }
                break;

            case FLOW_SLA_BREACH:
                // Check if execution time exceeds threshold
                if(rule.getThresholdValue() != null && flowExecution.getCompletedAt() != null) {
                    long executionTime = java.time.Duration.between(
                            flowExecution.getStartedAt(),
                            flowExecution.getCompletedAt()).toMillis();

                    if(evaluateThreshold(executionTime, rule.getThresholdOperator(), rule.getThresholdValue())) {
                        shouldAlert = true;
                        title = "Flow SLA Breach";
                        message = String.format("Flow '%s' execution time(%d ms) exceeded SLA threshold(%d ms)",
                                flowExecution.getFlow().getName(),
                                executionTime,
                                rule.getThresholdValue().longValue());
                        details.put("execution_time_ms", String.valueOf(executionTime));
                        details.put("threshold_ms", String.valueOf(rule.getThresholdValue().longValue()));
                    }
                }
                break;
        }

        if(shouldAlert) {
            createAlert(rule, title, message, Alert.SourceType.FLOW,
                       flowExecution.getFlow().getId().toString(), details);
        }
    }

    /**
     * Evaluate a rule for adapter metrics
     */
    private void evaluateRule(AlertRule rule, String adapterId, Map<String, Object> metrics) {
        boolean shouldAlert = false;
        String title = "";
        String message = "";
        Map<String, String> details = new HashMap<>();

        switch(rule.getAlertType()) {
            case ADAPTER_CONNECTION_FAILURE:
                if(metrics.get("connected") != null && !(Boolean) metrics.get("connected")) {
                    shouldAlert = true;
                    title = "Adapter Connection Failed";
                    message = String.format("Adapter '%s' failed to connect", adapterId);
                    details.put("adapter_id", adapterId);
                    details.put("error", String.valueOf(metrics.get("error")));
                }
                break;

            case ADAPTER_HEALTH_DEGRADED:
                if(metrics.get("health_score") != null && rule.getThresholdValue() != null) {
                    double healthScore = ((Number) metrics.get("health_score")).doubleValue();

                    if(evaluateThreshold(healthScore, rule.getThresholdOperator(), rule.getThresholdValue())) {
                        shouldAlert = true;
                        title = "Adapter Health Degraded";
                        message = String.format("Adapter '%s' health score(%f) below threshold(%f)",
                                adapterId, healthScore, rule.getThresholdValue());
                        details.put("health_score", String.valueOf(healthScore));
                        details.put("threshold", String.valueOf(rule.getThresholdValue()));
                    }
                }
                break;

            case ERROR_RATE_THRESHOLD:
                if(metrics.get("error_rate") != null && rule.getThresholdValue() != null) {
                    double errorRate = ((Number) metrics.get("error_rate")).doubleValue();

                    if(evaluateThreshold(errorRate, rule.getThresholdOperator(), rule.getThresholdValue())) {
                        shouldAlert = true;
                        title = "High Error Rate";
                        message = String.format("Adapter '%s' error rate(%f%%) exceeds threshold(%f%%)",
                                adapterId, errorRate, rule.getThresholdValue());
                        details.put("error_rate", String.valueOf(errorRate));
                        details.put("threshold", String.valueOf(rule.getThresholdValue()));
                    }
                }
                break;
        }

        if(shouldAlert) {
            createAlert(rule, title, message, Alert.SourceType.ADAPTER, adapterId, details);
        }
    }

    /**
     * Evaluate system - wide rules
     */
    private void evaluateSystemRule(AlertRule rule) {
        // Implementation depends on system metrics collection
        // This is a placeholder for system - wide monitoring
        log.debug("Evaluating system rule: {}", rule.getRuleName());
    }

    /**
     * Send alert notifications
     */
    private void sendAlertNotifications(Alert alert) {
        try {
            Set<String> channelIds = alert.getAlertRule().getNotificationChannelIds();
            if(channelIds.isEmpty()) {
                log.warn("No notification channels configured for alert rule: {}",
                        alert.getAlertRule().getRuleName());
                return;
            }

            List<NotificationChannel> channels = notificationChannelRepository
                    .findEnabledChannelsByIds(channelIds.stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList()));

            for(NotificationChannel channel : channels) {
                CompletableFuture.runAsync(() -> {
                    try {
                        String notificationId = notificationService.sendNotification(channel, alert);
                        alert.addNotificationId(notificationId);
                        alertRepository.save(alert);
                    } catch(Exception e) {
                        log.error("Failed to send notification via channel {}", channel.getChannelName(), e);
                    }
                });
            }

            alert.setStatus(Alert.AlertStatus.NOTIFIED);
            alertRepository.save(alert);

        } catch(Exception e) {
            log.error("Error sending alert notifications", e);
        }
    }

    /**
     * Escalate an alert
     */
    private void escalateAlert(Alert alert) {
        alert.escalate();
        alertRepository.save(alert);

        // Send escalation notifications
        Set<String> escalationChannelIds = alert.getAlertRule().getEscalationChannelIds();
        if(!escalationChannelIds.isEmpty()) {
            List<NotificationChannel> channels = notificationChannelRepository
                    .findEnabledChannelsByIds(escalationChannelIds.stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toList()));

            for(NotificationChannel channel : channels) {
                try {
                    String notificationId = notificationService.sendEscalationNotification(channel, alert);
                    alert.getEscalationNotificationIds().add(notificationId);
                } catch(Exception e) {
                    log.error("Failed to send escalation notification via channel {}",
                            channel.getChannelName(), e);
                }
            }

            alertRepository.save(alert);
        }
    }

    /**
     * Check if this is a duplicate alert
     */
    private boolean isDuplicateAlert(AlertRule rule, String sourceId) {
        if(rule.getSuppressionDurationMinutes() == null || rule.getSuppressionDurationMinutes() <= 0) {
            return false;
        }

        LocalDateTime since = LocalDateTime.now().minusMinutes(rule.getSuppressionDurationMinutes());
        List<Alert> recentAlerts = alertRepository.findRecentSimilarAlerts(rule, sourceId, since);

        return !recentAlerts.isEmpty();
    }

    /**
     * Evaluate threshold condition
     */
    private boolean evaluateThreshold(double value, AlertRule.ThresholdOperator operator, double threshold) {
        switch(operator) {
            case GREATER_THAN:
                return value > threshold;
            case GREATER_THAN_OR_EQUAL:
                return value >= threshold;
            case LESS_THAN:
                return value < threshold;
            case LESS_THAN_OR_EQUAL:
                return value <= threshold;
            case EQUAL:
                return value == threshold;
            case NOT_EQUAL:
                return value != threshold;
            default:
                return false;
        }
    }

    /**
     * Generate unique alert ID
     */
    private String generateAlertId() {
        return "ALT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Get source name for display
     */
    private String getSourceName(Alert.SourceType sourceType, String sourceId) {
        // This would lookup the actual name from the database
        // For now, return a formatted version
        return sourceType + ":" + sourceId;
    }
}
