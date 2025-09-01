package com.integrixs.monitoring.infrastructure.service;

import com.integrixs.monitoring.domain.model.Alert;
import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.model.MonitoringEvent;
import com.integrixs.monitoring.domain.repository.AlertRepository;
import com.integrixs.monitoring.domain.service.AlertingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Infrastructure implementation of alerting service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertingServiceImpl implements AlertingService {
    
    private final AlertRepository alertRepository;
    
    @Override
    @Transactional
    public Alert triggerAlert(Alert alert) {
        try {
            if (alert.getAlertId() == null) {
                alert.setAlertId(UUID.randomUUID().toString());
            }
            if (alert.getTriggeredAt() == null) {
                alert.setTriggeredAt(LocalDateTime.now());
            }
            if (alert.getStatus() == null) {
                alert.setStatus(Alert.AlertStatus.TRIGGERED);
            }
            
            Alert savedAlert = alertRepository.save(alert);
            log.info("Alert triggered: {} - {}", alert.getAlertName(), alert.getMessage());
            
            // Execute alert actions
            executeAlertAction(savedAlert);
            
            return savedAlert;
        } catch (Exception e) {
            log.error("Error triggering alert: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to trigger alert", e);
        }
    }
    
    @Override
    @Transactional
    public List<Alert> evaluateMetric(MetricSnapshot metric) {
        List<Alert> triggeredAlerts = new ArrayList<>();
        
        try {
            // Get enabled rules for this metric
            List<AlertRule> rules = alertRepository.findEnabledRules().stream()
                    .filter(rule -> rule.getTargetMetric() != null && 
                                  rule.getTargetMetric().equals(metric.getMetricName()))
                    .collect(java.util.stream.Collectors.toList());
            
            for (AlertRule rule : rules) {
                if (evaluateRule(rule, metric.getValue())) {
                    Alert alert = Alert.builder()
                            .alertName(rule.getRuleName())
                            .alertType(rule.getAlertType())
                            .severity(rule.getSeverity())
                            .source("MetricEvaluation")
                            .message(String.format("Metric %s %s threshold: %.2f %s %.2f",
                                    metric.getMetricName(), rule.getComparison(),
                                    metric.getValue(), rule.getComparison(), rule.getThreshold()))
                            .condition(rule.getCondition())
                            .domainType("Metric")
                            .domainReferenceId(metric.getMetricId())
                            .action(rule.getAction())
                            .build();
                    
                    alert.addMetadata("metricName", metric.getMetricName());
                    alert.addMetadata("metricValue", metric.getValue());
                    alert.addMetadata("threshold", rule.getThreshold());
                    alert.addMetadata("ruleId", rule.getRuleId());
                    
                    triggeredAlerts.add(triggerAlert(alert));
                }
            }
        } catch (Exception e) {
            log.error("Error evaluating metric for alerts: {}", e.getMessage(), e);
        }
        
        return triggeredAlerts;
    }
    
    @Override
    @Transactional
    public List<Alert> evaluateEvent(MonitoringEvent event) {
        List<Alert> triggeredAlerts = new ArrayList<>();
        
        try {
            // Check for critical events
            if (event.isCritical()) {
                Alert alert = Alert.builder()
                        .alertName("Critical Event")
                        .alertType(Alert.AlertType.ERROR_RATE)
                        .severity(Alert.AlertSeverity.CRITICAL)
                        .source("EventEvaluation")
                        .message("Critical event detected: " + event.getMessage())
                        .domainType(event.getDomainType())
                        .domainReferenceId(event.getDomainReferenceId())
                        .build();
                
                alert.addMetadata("eventId", event.getEventId());
                alert.addMetadata("eventType", event.getEventType().name());
                alert.addMetadata("correlationId", event.getCorrelationId());
                
                triggeredAlerts.add(triggerAlert(alert));
            }
            
            // Check for error patterns
            if (event.isError()) {
                // Could implement more sophisticated error pattern detection here
                log.debug("Error event detected, checking for patterns: {}", event.getMessage());
            }
            
        } catch (Exception e) {
            log.error("Error evaluating event for alerts: {}", e.getMessage(), e);
        }
        
        return triggeredAlerts;
    }
    
    @Override
    @Transactional
    public Alert acknowledgeAlert(String alertId, String userId) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        alert.acknowledge(userId);
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert acknowledged: {} by {}", alertId, userId);
        
        return updatedAlert;
    }
    
    @Override
    @Transactional
    public Alert resolveAlert(String alertId, String resolution) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        alert.resolve();
        alert.addMetadata("resolution", resolution);
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert resolved: {} - {}", alertId, resolution);
        
        return updatedAlert;
    }
    
    @Override
    @Transactional
    public Alert suppressAlert(String alertId, String reason, int duration) {
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));
        
        alert.setStatus(Alert.AlertStatus.SUPPRESSED);
        alert.addMetadata("suppressionReason", reason);
        alert.addMetadata("suppressionDuration", duration);
        alert.addMetadata("suppressedUntil", LocalDateTime.now().plusMinutes(duration));
        
        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert suppressed: {} for {} minutes", alertId, duration);
        
        return updatedAlert;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Alert> getActiveAlerts() {
        return alertRepository.findActiveAlerts();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Alert> getAlertsBySeverity(Alert.AlertSeverity severity) {
        return alertRepository.findBySeverity(severity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Alert getAlert(String alertId) {
        return alertRepository.findById(alertId).orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Alert> queryAlerts(AlertQueryCriteria criteria) {
        return alertRepository.query(criteria);
    }
    
    @Override
    @Transactional
    public String createAlertRule(AlertRule rule) {
        if (rule.getRuleId() == null) {
            rule.setRuleId(UUID.randomUUID().toString());
        }
        
        AlertRule savedRule = alertRepository.saveRule(rule);
        log.info("Alert rule created: {}", savedRule.getRuleName());
        
        return savedRule.getRuleId();
    }
    
    @Override
    @Transactional
    public void updateAlertRule(String ruleId, AlertRule rule) {
        rule.setRuleId(ruleId);
        alertRepository.saveRule(rule);
        log.info("Alert rule updated: {}", rule.getRuleName());
    }
    
    @Override
    @Transactional
    public void deleteAlertRule(String ruleId) {
        alertRepository.deleteRule(ruleId);
        log.info("Alert rule deleted: {}", ruleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AlertRule> getAlertRules() {
        return alertRepository.findAllRules();
    }
    
    @Override
    public void executeAlertAction(Alert alert) {
        if (alert.getAction() == null) {
            return;
        }
        
        try {
            switch (alert.getAction().getType()) {
                case EMAIL:
                    // TODO: Implement email notification
                    log.info("Sending email alert for: {}", alert.getAlertName());
                    break;
                    
                case WEBHOOK:
                    // TODO: Implement webhook notification
                    log.info("Sending webhook alert for: {}", alert.getAlertName());
                    break;
                    
                case SMS:
                    // TODO: Implement SMS notification
                    log.info("Sending SMS alert for: {}", alert.getAlertName());
                    break;
                    
                default:
                    log.warn("Unsupported alert action type: {}", alert.getAction().getType());
            }
        } catch (Exception e) {
            log.error("Error executing alert action: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Evaluate rule against metric value
     * @param rule Alert rule
     * @param value Metric value
     * @return true if rule is triggered
     */
    private boolean evaluateRule(AlertRule rule, double value) {
        switch (rule.getComparison()) {
            case "GT":
                return value > rule.getThreshold();
            case "GTE":
                return value >= rule.getThreshold();
            case "LT":
                return value < rule.getThreshold();
            case "LTE":
                return value <= rule.getThreshold();
            case "EQ":
                return Math.abs(value - rule.getThreshold()) < 0.0001;
            case "NEQ":
                return Math.abs(value - rule.getThreshold()) >= 0.0001;
            default:
                log.warn("Unknown comparison operator: {}", rule.getComparison());
                return false;
        }
    }
}