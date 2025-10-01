package com.integrixs.backend.controller;

import com.integrixs.backend.service.FlowAlertingService;
import com.integrixs.data.model.Alert;
import com.integrixs.data.model.AlertRule;
import com.integrixs.data.sql.repository.AlertSqlRepository;
import com.integrixs.data.sql.repository.AlertRuleSqlRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing alerts
 */
@RestController
@RequestMapping("/api/v2/alerts")
public class AlertController {

    private final AlertSqlRepository alertRepository;
    private final AlertRuleSqlRepository alertRuleRepository;
    private final FlowAlertingService alertingService;

    public AlertController(AlertSqlRepository alertRepository,
                           AlertRuleSqlRepository alertRuleRepository,
                           FlowAlertingService alertingService) {
        this.alertRepository = alertRepository;
        this.alertRuleRepository = alertRuleRepository;
        this.alertingService = alertingService;
    }

    /**
     * Get all alerts with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<Alert> getAlerts(
            @RequestParam(required = false) Alert.AlertStatus status,
            @RequestParam(required = false) AlertRule.AlertSeverity severity,
            @RequestParam(required = false) Alert.SourceType sourceType,
            @RequestParam(required = false) String sourceId,
            Pageable pageable) {

        if(status != null) {
            return alertRepository.findByStatus(status, pageable);
        } else if(severity != null) {
            return alertRepository.findBySeverity(severity, pageable);
        } else if(sourceType != null && sourceId != null) {
            return alertRepository.findBySourceTypeAndSourceId(sourceType, sourceId, pageable);
        } else {
            return alertRepository.findAll(pageable);
        }
    }

    /**
     * Get unresolved alerts
     */
    @GetMapping("/unresolved")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public Page<Alert> getUnresolvedAlerts(Pageable pageable) {
        return alertRepository.findUnresolvedAlerts(pageable);
    }

    /**
     * Get alert by ID
     */
    @GetMapping("/ {alertId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Alert> getAlert(@PathVariable String alertId) {
        return alertRepository.findByAlertId(alertId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Acknowledge an alert
     */
    @PostMapping("/ {alertId}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Alert> acknowledgeAlert(
            @PathVariable String alertId,
            @RequestParam String userId,
            @RequestParam(required = false) String notes) {

        try {
            Alert alert = alertingService.acknowledgeAlert(alertId, userId, notes);
            return ResponseEntity.ok(alert);
        } catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Resolve an alert
     */
    @PostMapping("/ {alertId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Alert> resolveAlert(
            @PathVariable String alertId,
            @RequestParam String userId,
            @RequestParam String resolutionNotes) {

        try {
            Alert alert = alertingService.resolveAlert(alertId, userId, resolutionNotes);
            return ResponseEntity.ok(alert);
        } catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Suppress an alert
     */
    @PostMapping("/ {alertId}/suppress")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Alert> suppressAlert(
            @PathVariable String alertId,
            @RequestParam String until,
            @RequestParam String reason) {

        try {
            LocalDateTime suppressUntil = LocalDateTime.parse(until);
            Alert alert = alertingService.suppressAlert(alertId, suppressUntil, reason);
            return ResponseEntity.ok(alert);
        } catch(RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get alert statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Map<String, Object>> getAlertStatistics(
            @RequestParam(defaultValue = "24") int hours) {

        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        Map<String, Object> stats = new HashMap<>();
        stats.put("since", since);
        stats.put("total", alertRepository.count());
        stats.put("unresolved", alertRepository.findUnresolvedAlerts(Pageable.unpaged()).getTotalElements());
        stats.put("critical", alertRepository.findUnacknowledgedCriticalAlerts().size());
        stats.put("byStatusAndSeverity", alertRepository.countByStatusAndSeveritySince(since));

        return ResponseEntity.ok(stats);
    }

    /**
     * Get alert rules
     */
    @GetMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<AlertRule>> getAlertRules(
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) AlertRule.AlertType type,
            @RequestParam(required = false) AlertRule.AlertSeverity severity,
            Pageable pageable) {

        Page<AlertRule> rules;

        if(enabled != null && enabled) {
            if(type != null) {
                rules = alertRuleRepository.findByAlertTypeAndEnabledTrue(type, pageable);
            } else if(severity != null) {
                rules = alertRuleRepository.findBySeverityAndEnabledTrue(severity, pageable);
            } else {
                rules = alertRuleRepository.findByEnabledTrue(pageable);
            }
        } else {
            rules = alertRuleRepository.findAll(pageable);
        }

        return ResponseEntity.ok(rules);
    }

    /**
     * Get alert rule by ID
     */
    @GetMapping("/rules/ {ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<AlertRule> getAlertRule(@PathVariable UUID ruleId) {
        return alertRuleRepository.findById(ruleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create alert rule
     */
    @PostMapping("/rules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertRule> createAlertRule(@RequestBody AlertRule rule) {
        if(alertRuleRepository.existsByRuleName(rule.getRuleName())) {
            return ResponseEntity.badRequest().build();
        }

        rule.setId(null); // Ensure new entity
        rule.setCreatedAt(LocalDateTime.now());
        rule.setUpdatedAt(LocalDateTime.now());

        AlertRule saved = alertRuleRepository.save(rule);
        return ResponseEntity.ok(saved);
    }

    /**
     * Update alert rule
     */
    @PutMapping("/rules/ {ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertRule> updateAlertRule(
            @PathVariable UUID ruleId,
            @RequestBody AlertRule rule) {

        return alertRuleRepository.findById(ruleId)
                .map(existing -> {
                    // Update fields
                    existing.setRuleName(rule.getRuleName());
                    existing.setDescription(rule.getDescription());
                    existing.setAlertType(rule.getAlertType());
                    existing.setSeverity(rule.getSeverity());
                    existing.setEnabled(rule.isEnabled());
                    existing.setConditionType(rule.getConditionType());
                    existing.setConditionExpression(rule.getConditionExpression());
                    existing.setThresholdValue(rule.getThresholdValue());
                    existing.setThresholdOperator(rule.getThresholdOperator());
                    existing.setTimeWindowMinutes(rule.getTimeWindowMinutes());
                    existing.setOccurrenceCount(rule.getOccurrenceCount());
                    existing.setTargetType(rule.getTargetType());
                    existing.setTargetId(rule.getTargetId());
                    existing.setNotificationChannelIds(rule.getNotificationChannelIds());
                    existing.setSuppressionDurationMinutes(rule.getSuppressionDurationMinutes());
                    existing.setEscalationEnabled(rule.isEscalationEnabled());
                    existing.setEscalationAfterMinutes(rule.getEscalationAfterMinutes());
                    existing.setEscalationChannelIds(rule.getEscalationChannelIds());
                    existing.setTags(rule.getTags());
                    existing.setUpdatedAt(LocalDateTime.now());
                    existing.setModifiedBy(rule.getModifiedBy());

                    return ResponseEntity.ok(alertRuleRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete alert rule
     */
    @DeleteMapping("/rules/ {ruleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable UUID ruleId) {
        if(alertRuleRepository.existsById(ruleId)) {
            alertRuleRepository.deleteById(ruleId);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Enable/disable alert rule
     */
    @PatchMapping("/rules/ {ruleId}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertRule> toggleAlertRule(
            @PathVariable UUID ruleId,
            @RequestParam boolean enabled) {

        return alertRuleRepository.findById(ruleId)
                .map(rule -> {
                    rule.setEnabled(enabled);
                    rule.setUpdatedAt(LocalDateTime.now());
                    return ResponseEntity.ok(alertRuleRepository.save(rule));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
