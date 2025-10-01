package com.integrixs.monitoring.application.service;

import com.integrixs.monitoring.api.dto.*;
import com.integrixs.monitoring.domain.model.Alert;
import com.integrixs.monitoring.domain.model.MetricSnapshot;
import com.integrixs.monitoring.domain.model.MonitoringEvent;
import com.integrixs.monitoring.domain.service.MonitoringAlertService;
import com.integrixs.monitoring.domain.service.EventLoggingService;
import com.integrixs.monitoring.domain.service.MetricsCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Application service for monitoring operations
 * Orchestrates domain services and handles use cases
 */
@Service
public class MonitoringApplicationService {

    private static final Logger log = LoggerFactory.getLogger(MonitoringApplicationService.class);
    private final EventLoggingService eventLoggingService;
    private final MetricsCollectorService metricsCollectorService;
    private final MonitoringAlertService alertingService;

    public MonitoringApplicationService(EventLoggingService eventLoggingService,
                                     MetricsCollectorService metricsCollectorService,
                                     MonitoringAlertService alertingService) {
        this.eventLoggingService = eventLoggingService;
        this.metricsCollectorService = metricsCollectorService;
        this.alertingService = alertingService;
    }

    /**
     * Log a monitoring event
     * @param request Log event request
     * @return Log event response
     */
    public LogEventResponseDTO logEvent(LogEventRequestDTO request) {
        try {
            // Convert to domain model
            MonitoringEvent event = MonitoringEvent.builder()
                    .eventType(MonitoringEvent.EventType.valueOf(request.getEventType()))
                    .level(MonitoringEvent.EventLevel.valueOf(request.getLevel()))
                    .source(request.getSource())
                    .message(request.getMessage())
                    .userId(request.getUserId())
                    .correlationId(request.getCorrelationId())
                    .domainType(request.getDomainType())
                    .domainReferenceId(request.getDomainReferenceId())
                    .metadata(request.getMetadata() != null ? request.getMetadata() : new HashMap<>())
                    .stackTrace(request.getStackTrace())
                    .build();

            // Log the event
            eventLoggingService.logEvent(event);

            // Evaluate for alerts
            List<Alert> triggeredAlerts = alertingService.evaluateEvent(event);

            return LogEventResponseDTO.builder()
                    .success(true)
                    .eventId(event.getEventId())
                    .timestamp(event.getTimestamp())
                    .alertsTriggered(triggeredAlerts.size())
                    .build();

        } catch(Exception e) {
            log.error("Error logging event: {}", e.getMessage(), e);
            return LogEventResponseDTO.builder()
                    .success(false)
                    .errorMessage("Failed to log event: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Record a metric
     * @param request Record metric request
     * @return Record metric response
     */
    public RecordMetricResponseDTO recordMetric(RecordMetricRequestDTO request) {
        try {
            // Convert to domain model
            MetricSnapshot metric = MetricSnapshot.builder()
                    .metricName(request.getMetricName())
                    .metricType(MetricSnapshot.MetricType.valueOf(request.getMetricType()))
                    .value(request.getValue())
                    .unit(request.getUnit())
                    .timestamp(LocalDateTime.now())
                    .source(request.getSource())
                    .domainType(request.getDomainType())
                    .domainReferenceId(request.getDomainReferenceId())
                    .tags(request.getTags() != null ? request.getTags() : new HashMap<>())
                    .dimensions(request.getDimensions() != null ? request.getDimensions() : new HashMap<>())
                    .build();

            // Record the metric
            metricsCollectorService.recordMetric(metric);

            // Evaluate for alerts
            List<Alert> triggeredAlerts = alertingService.evaluateMetric(metric);

            return RecordMetricResponseDTO.builder()
                    .success(true)
                    .metricId(metric.getMetricId())
                    .timestamp(metric.getTimestamp())
                    .alertsTriggered(triggeredAlerts.size())
                    .build();

        } catch(Exception e) {
            log.error("Error recording metric: {}", e.getMessage(), e);
            return RecordMetricResponseDTO.builder()
                    .success(false)
                    .errorMessage("Failed to record metric: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Query monitoring events
     * @param request Query request
     * @return List of events
     */
    public List<MonitoringEventDTO> queryEvents(EventQueryRequestDTO request) {
        try {
            // Build query criteria
            EventLoggingService.EventQueryCriteria criteria = new EventLoggingService.EventQueryCriteria();
            if(request.getEventType() != null) {
                criteria.setEventType(MonitoringEvent.EventType.valueOf(request.getEventType()));
            }
            if(request.getMinLevel() != null) {
                criteria.setMinLevel(MonitoringEvent.EventLevel.valueOf(request.getMinLevel()));
            }
            criteria.setSource(request.getSource());
            criteria.setUserId(request.getUserId());
            criteria.setDomainType(request.getDomainType());
            criteria.setDomainReferenceId(request.getDomainReferenceId());
            criteria.setCorrelationId(request.getCorrelationId());
            criteria.setStartTime(request.getStartTime());
            criteria.setEndTime(request.getEndTime());
            criteria.setLimit(request.getLimit());

            // Query events
            List<MonitoringEvent> events = eventLoggingService.queryEvents(criteria);

            // Convert to DTOs
            return events.stream()
                    .map(this::convertToEventDTO)
                    .collect(Collectors.toList());

        } catch(Exception e) {
            log.error("Error querying events: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Query metrics
     * @param request Query request
     * @return List of metrics
     */
    public List<MetricSnapshotDTO> queryMetrics(MetricQueryRequestDTO request) {
        try {
            // Build query criteria
            MetricsCollectorService.MetricQueryCriteria criteria = new MetricsCollectorService.MetricQueryCriteria();
            criteria.setMetricName(request.getMetricName());
            if(request.getMetricType() != null) {
                criteria.setMetricType(MetricSnapshot.MetricType.valueOf(request.getMetricType()));
            }
            criteria.setTags(request.getTags());
            criteria.setStartTime(request.getStartTime());
            criteria.setEndTime(request.getEndTime());
            criteria.setLimit(request.getLimit());
            criteria.setOrderBy(request.getOrderBy());

            // Query metrics
            List<MetricSnapshot> metrics = metricsCollectorService.queryMetrics(criteria);

            // Convert to DTOs
            return metrics.stream()
                    .map(this::convertToMetricDTO)
                    .collect(Collectors.toList());

        } catch(Exception e) {
            log.error("Error querying metrics: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get active alerts
     * @return List of active alerts
     */
    public List<AlertDTO> getActiveAlerts() {
        try {
            List<Alert> alerts = alertingService.getActiveAlerts();
            return alerts.stream()
                    .map(this::convertToAlertDTO)
                    .collect(Collectors.toList());
        } catch(Exception e) {
            log.error("Error getting active alerts: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Acknowledge an alert
     * @param alertId Alert ID
     * @param request Acknowledge request
     * @return Alert operation response
     */
    public AlertOperationResponseDTO acknowledgeAlert(String alertId, AcknowledgeAlertRequestDTO request) {
        try {
            Alert alert = alertingService.acknowledgeAlert(alertId, request.getUserId());

            return AlertOperationResponseDTO.builder()
                    .success(true)
                    .alertId(alert.getAlertId())
                    .status(alert.getStatus().name())
                    .message("Alert acknowledged successfully")
                    .build();

        } catch(Exception e) {
            log.error("Error acknowledging alert: {}", e.getMessage(), e);
            return AlertOperationResponseDTO.builder()
                    .success(false)
                    .alertId(alertId)
                    .errorMessage("Failed to acknowledge alert: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Resolve an alert
     * @param alertId Alert ID
     * @param request Resolve request
     * @return Alert operation response
     */
    public AlertOperationResponseDTO resolveAlert(String alertId, ResolveAlertRequestDTO request) {
        try {
            Alert alert = alertingService.resolveAlert(alertId, request.getResolution());

            return AlertOperationResponseDTO.builder()
                    .success(true)
                    .alertId(alert.getAlertId())
                    .status(alert.getStatus().name())
                    .message("Alert resolved successfully")
                    .build();

        } catch(Exception e) {
            log.error("Error resolving alert: {}", e.getMessage(), e);
            return AlertOperationResponseDTO.builder()
                    .success(false)
                    .alertId(alertId)
                    .errorMessage("Failed to resolve alert: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Calculate metric aggregation
     * @param request Aggregation request
     * @return Aggregation result
     */
    public MetricAggregationResponseDTO calculateAggregation(MetricAggregationRequestDTO request) {
        try {
            double value = metricsCollectorService.calculateAggregation(
                    request.getMetricName(),
                    MetricsCollectorService.AggregationType.valueOf(request.getAggregationType()),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getTags()
           );

            return MetricAggregationResponseDTO.builder()
                    .success(true)
                    .metricName(request.getMetricName())
                    .aggregationType(request.getAggregationType())
                    .value(value)
                    .startTime(request.getStartTime())
                    .endTime(request.getEndTime())
                    .build();

        } catch(Exception e) {
            log.error("Error calculating aggregation: {}", e.getMessage(), e);
            return MetricAggregationResponseDTO.builder()
                    .success(false)
                    .errorMessage("Failed to calculate aggregation: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Create alert rule
     * @param request Create rule request
     * @return Rule operation response
     */
    public AlertRuleOperationResponseDTO createAlertRule(CreateAlertRuleRequestDTO request) {
        try {
            // Convert to domain model
            MonitoringAlertService.AlertRule rule = new MonitoringAlertService.AlertRule();
            rule.setRuleName(request.getRuleName());
            rule.setCondition(request.getCondition());
            rule.setAlertType(Alert.AlertType.valueOf(request.getAlertType()));
            rule.setSeverity(Alert.AlertSeverity.valueOf(request.getSeverity()));
            rule.setEnabled(request.isEnabled());
            rule.setEvaluationInterval(request.getEvaluationInterval());
            rule.setTargetMetric(request.getTargetMetric());
            rule.setThreshold(request.getThreshold());
            rule.setComparison(request.getComparison());

            if(request.getAction() != null) {
                rule.setAction(convertToAlertAction(request.getAction()));
            }

            // Create rule
            String ruleId = alertingService.createAlertRule(rule);

            return AlertRuleOperationResponseDTO.builder()
                    .success(true)
                    .ruleId(ruleId)
                    .message("Alert rule created successfully")
                    .build();

        } catch(Exception e) {
            log.error("Error creating alert rule: {}", e.getMessage(), e);
            return AlertRuleOperationResponseDTO.builder()
                    .success(false)
                    .errorMessage("Failed to create alert rule: " + e.getMessage())
                    .build();
        }
    }

    // Conversion methods

    private MonitoringEventDTO convertToEventDTO(MonitoringEvent event) {
        return MonitoringEventDTO.builder()
                .eventId(event.getEventId())
                .eventType(event.getEventType().name())
                .level(event.getLevel().name())
                .source(event.getSource())
                .message(event.getMessage())
                .timestamp(event.getTimestamp())
                .userId(event.getUserId())
                .correlationId(event.getCorrelationId())
                .domainType(event.getDomainType())
                .domainReferenceId(event.getDomainReferenceId())
                .metadata(event.getMetadata())
                .stackTrace(event.getStackTrace())
                .build();
    }

    private MetricSnapshotDTO convertToMetricDTO(MetricSnapshot metric) {
        return MetricSnapshotDTO.builder()
                .metricId(metric.getMetricId())
                .metricName(metric.getMetricName())
                .metricType(metric.getMetricType().name())
                .value(metric.getValue())
                .unit(metric.getUnit())
                .timestamp(metric.getTimestamp())
                .source(metric.getSource())
                .domainType(metric.getDomainType())
                .domainReferenceId(metric.getDomainReferenceId())
                .tags(metric.getTags())
                .dimensions(metric.getDimensions())
                .build();
    }

    private AlertDTO convertToAlertDTO(Alert alert) {
        return AlertDTO.builder()
                .alertId(alert.getAlertId())
                .alertName(alert.getAlertName())
                .alertType(alert.getAlertType().name())
                .severity(alert.getSeverity().name())
                .status(alert.getStatus().name())
                .source(alert.getSource())
                .message(alert.getMessage())
                .condition(alert.getCondition())
                .triggeredAt(alert.getTriggeredAt())
                .resolvedAt(alert.getResolvedAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .acknowledgedBy(alert.getAcknowledgedBy())
                .domainType(alert.getDomainType())
                .domainReferenceId(alert.getDomainReferenceId())
                .metadata(alert.getMetadata())
                .build();
    }

    private Alert.AlertAction convertToAlertAction(AlertActionDTO dto) {
        return Alert.AlertAction.builder()
                .type(Alert.AlertAction.ActionType.valueOf(dto.getType()))
                .parameters(dto.getParameters())
                .build();
    }
}
