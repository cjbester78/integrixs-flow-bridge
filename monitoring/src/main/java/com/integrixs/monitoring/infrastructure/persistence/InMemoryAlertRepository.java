package com.integrixs.monitoring.infrastructure.persistence;

import com.integrixs.monitoring.domain.model.Alert;
import com.integrixs.monitoring.domain.repository.AlertRepository;
import com.integrixs.monitoring.domain.service.MonitoringAlertService.AlertQueryCriteria;
import com.integrixs.monitoring.domain.service.MonitoringAlertService.AlertRule;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In - memory implementation of alert repository
 */
@Repository
public class InMemoryAlertRepository implements AlertRepository {

    private final Map<String, Alert> alertStorage = new ConcurrentHashMap<>();
    private final Map<String, AlertRule> ruleStorage = new ConcurrentHashMap<>();

    @Override
    public Alert save(Alert alert) {
        alertStorage.put(alert.getAlertId(), alert);
        return alert;
    }

    @Override
    public Optional<Alert> findById(String alertId) {
        return Optional.ofNullable(alertStorage.get(alertId));
    }

    @Override
    public List<Alert> findActiveAlerts() {
        return alertStorage.values().stream()
                .filter(Alert::isActive)
                .sorted(Comparator.comparing(Alert::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Alert> findBySeverity(Alert.AlertSeverity minSeverity) {
        return alertStorage.values().stream()
                .filter(alert -> alert.getSeverity().ordinal() >= minSeverity.ordinal())
                .sorted(Comparator.comparing(Alert::getSeverity).reversed()
                        .thenComparing(Alert::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Alert> findByDomainReference(String domainType, String domainReferenceId) {
        return alertStorage.values().stream()
                .filter(alert -> domainType.equals(alert.getDomainType()) &&
                               domainReferenceId.equals(alert.getDomainReferenceId()))
                .sorted(Comparator.comparing(Alert::getTriggeredAt).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Alert> query(AlertQueryCriteria criteria) {
        return alertStorage.values().stream()
                .filter(alert -> matchesCriteria(alert, criteria))
                .sorted(Comparator.comparing(Alert::getTriggeredAt).reversed())
                .limit(criteria.getLimit() != null ? criteria.getLimit() : 1000)
                .collect(Collectors.toList());
    }

    @Override
    public long count(AlertQueryCriteria criteria) {
        return alertStorage.values().stream()
                .filter(alert -> matchesCriteria(alert, criteria))
                .count();
    }

    @Override
    public long deleteResolvedOlderThan(int retentionDays) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        List<String> toDelete = alertStorage.entrySet().stream()
                .filter(entry -> entry.getValue().getStatus() == Alert.AlertStatus.RESOLVED)
                .filter(entry -> entry.getValue().getResolvedAt() != null &&
                               entry.getValue().getResolvedAt().isBefore(cutoff))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        toDelete.forEach(alertStorage::remove);
        return toDelete.size();
    }

    @Override
    public AlertRule saveRule(AlertRule rule) {
        ruleStorage.put(rule.getRuleId(), rule);
        return rule;
    }

    @Override
    public Optional<AlertRule> findRuleById(String ruleId) {
        return Optional.ofNullable(ruleStorage.get(ruleId));
    }

    @Override
    public List<AlertRule> findAllRules() {
        return new ArrayList<>(ruleStorage.values());
    }

    @Override
    public List<AlertRule> findEnabledRules() {
        return ruleStorage.values().stream()
                .filter(AlertRule::isEnabled)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteRule(String ruleId) {
        ruleStorage.remove(ruleId);
    }

    @Override
    public AlertStatistics getStatistics(long startTime, long endTime) {
        LocalDateTime start = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

        List<Alert> alertsInRange = alertStorage.values().stream()
                .filter(alert -> !alert.getTriggeredAt().isBefore(start) &&
                               !alert.getTriggeredAt().isAfter(end))
                .collect(Collectors.toList());

        AlertStatistics stats = new AlertStatistics();
        stats.setTotalAlerts(alertsInRange.size());

        alertsInRange.forEach(alert -> {
            // Status counts
            switch(alert.getStatus()) {
                case TRIGGERED:
                    stats.setActiveAlerts(stats.getActiveAlerts() + 1);
                    break;
                case ACKNOWLEDGED:
                    stats.setAcknowledgedAlerts(stats.getAcknowledgedAlerts() + 1);
                    break;
                case RESOLVED:
                    stats.setResolvedAlerts(stats.getResolvedAlerts() + 1);
                    break;
            }

            // Severity counts
            switch(alert.getSeverity()) {
                case CRITICAL:
                    stats.setCriticalAlerts(stats.getCriticalAlerts() + 1);
                    break;
                case MAJOR:
                    stats.setMajorAlerts(stats.getMajorAlerts() + 1);
                    break;
                case MINOR:
                    stats.setMinorAlerts(stats.getMinorAlerts() + 1);
                    break;
                case WARNING:
                    stats.setWarningAlerts(stats.getWarningAlerts() + 1);
                    break;
            }
        });

        // Calculate average times
        double totalResolutionTime = 0;
        int resolvedCount = 0;
        double totalAcknowledgmentTime = 0;
        int acknowledgedCount = 0;

        for(Alert alert : alertsInRange) {
            if(alert.getResolvedAt() != null) {
                long resolutionTime = java.time.Duration.between(
                        alert.getTriggeredAt(), alert.getResolvedAt()).toMinutes();
                totalResolutionTime += resolutionTime;
                resolvedCount++;
            }

            if(alert.getAcknowledgedAt() != null) {
                long acknowledgmentTime = java.time.Duration.between(
                        alert.getTriggeredAt(), alert.getAcknowledgedAt()).toMinutes();
                totalAcknowledgmentTime += acknowledgmentTime;
                acknowledgedCount++;
            }
        }

        if(resolvedCount > 0) {
            stats.setAverageResolutionTime(totalResolutionTime / resolvedCount);
        }

        if(acknowledgedCount > 0) {
            stats.setAverageAcknowledgmentTime(totalAcknowledgmentTime / acknowledgedCount);
        }

        return stats;
    }

    private boolean matchesCriteria(Alert alert, AlertQueryCriteria criteria) {
        if(criteria.getAlertType() != null && alert.getAlertType() != criteria.getAlertType()) {
            return false;
        }

        if(criteria.getMinSeverity() != null &&
            alert.getSeverity().ordinal() < criteria.getMinSeverity().ordinal()) {
            return false;
        }

        if(criteria.getStatus() != null && alert.getStatus() != criteria.getStatus()) {
            return false;
        }

        if(criteria.getDomainType() != null && !criteria.getDomainType().equals(alert.getDomainType())) {
            return false;
        }

        if(criteria.getDomainReferenceId() != null &&
            !criteria.getDomainReferenceId().equals(alert.getDomainReferenceId())) {
            return false;
        }

        if(criteria.getStartTime() != null) {
            LocalDateTime start = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(criteria.getStartTime()), ZoneId.systemDefault());
            if(alert.getTriggeredAt().isBefore(start)) {
                return false;
            }
        }

        if(criteria.getEndTime() != null) {
            LocalDateTime end = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(criteria.getEndTime()), ZoneId.systemDefault());
            if(alert.getTriggeredAt().isAfter(end)) {
                return false;
            }
        }

        return true;
    }
}
