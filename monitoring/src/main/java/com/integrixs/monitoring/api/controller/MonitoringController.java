package com.integrixs.monitoring.api.controller;

import com.integrixs.monitoring.api.dto.*;
import com.integrixs.monitoring.application.service.MonitoringApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for monitoring operations
 */
@RestController
@RequestMapping("/api/monitoring")
@Tag(name = "Monitoring", description = "APIs for system monitoring, metrics, and alerts")
public class MonitoringController {

    private static final Logger log = LoggerFactory.getLogger(MonitoringController.class);
    private final MonitoringApplicationService monitoringApplicationService;

    public MonitoringController(MonitoringApplicationService monitoringApplicationService) {
        this.monitoringApplicationService = monitoringApplicationService;
    }

    /**
     * Log a monitoring event
     * @param request Log event request
     * @return Log event response
     */
    @PostMapping("/events")
    @Operation(summary = "Log a monitoring event", description = "Records a monitoring event in the system")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Event logged successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<LogEventResponseDTO> logEvent(
            @Valid @RequestBody LogEventRequestDTO request) {

        log.debug("Logging event: {} - {}", request.getEventType(), request.getMessage());

        LogEventResponseDTO response = monitoringApplicationService.logEvent(request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Record a metric
     * @param request Record metric request
     * @return Record metric response
     */
    @PostMapping("/metrics")
    @Operation(summary = "Record a metric", description = "Records a metric value in the monitoring system")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Metric recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<RecordMetricResponseDTO> recordMetric(
            @Valid @RequestBody RecordMetricRequestDTO request) {

        log.debug("Recording metric: {} = {}", request.getMetricName(), request.getValue());

        RecordMetricResponseDTO response = monitoringApplicationService.recordMetric(request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Query monitoring events
     * @param request Query request
     * @return List of events
     */
    @PostMapping("/events/query")
    @Operation(summary = "Query monitoring events", description = "Query historical monitoring events")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Query executed successfully")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<List<MonitoringEventDTO>> queryEvents(
            @Valid @RequestBody EventQueryRequestDTO request) {

        log.debug("Querying events with criteria: {}", request);

        List<MonitoringEventDTO> events = monitoringApplicationService.queryEvents(request);
        return ResponseEntity.ok(events);
    }

    /**
     * Query metrics
     * @param request Query request
     * @return List of metrics
     */
    @PostMapping("/metrics/query")
    @Operation(summary = "Query metrics", description = "Query historical metric data")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Query executed successfully")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<List<MetricSnapshotDTO>> queryMetrics(
            @Valid @RequestBody MetricQueryRequestDTO request) {

        log.debug("Querying metrics with criteria: {}", request);

        List<MetricSnapshotDTO> metrics = monitoringApplicationService.queryMetrics(request);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Calculate metric aggregation
     * @param request Aggregation request
     * @return Aggregation result
     */
    @PostMapping("/metrics/aggregate")
    @Operation(summary = "Calculate metric aggregation", description = "Calculate aggregated metric values")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Aggregation calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<MetricAggregationResponseDTO> calculateAggregation(
            @Valid @RequestBody MetricAggregationRequestDTO request) {

        log.debug("Calculating aggregation for metric: {}", request.getMetricName());

        MetricAggregationResponseDTO response = monitoringApplicationService.calculateAggregation(request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get active alerts
     * @return List of active alerts
     */
    @GetMapping("/alerts/active")
    @Operation(summary = "Get active alerts", description = "Retrieve all currently active alerts")
    @ApiResponse(responseCode = "200", description = "Active alerts retrieved successfully")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<List<AlertDTO>> getActiveAlerts() {
        log.debug("Getting active alerts");

        List<AlertDTO> alerts = monitoringApplicationService.getActiveAlerts();
        return ResponseEntity.ok(alerts);
    }

    /**
     * Acknowledge an alert
     * @param alertId Alert ID
     * @param request Acknowledge request
     * @return Alert operation response
     */
    @PostMapping("/alerts/ {alertId}/acknowledge")
    @Operation(summary = "Acknowledge an alert", description = "Acknowledge that an alert has been seen")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Alert acknowledged successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AlertOperationResponseDTO> acknowledgeAlert(
            @Parameter(description = "Alert ID", required = true)
            @PathVariable String alertId,
            @Valid @RequestBody AcknowledgeAlertRequestDTO request) {

        log.info("Acknowledging alert: {} by user: {}", alertId, request.getUserId());

        AlertOperationResponseDTO response = monitoringApplicationService.acknowledgeAlert(alertId, request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Resolve an alert
     * @param alertId Alert ID
     * @param request Resolve request
     * @return Alert operation response
     */
    @PostMapping("/alerts/ {alertId}/resolve")
    @Operation(summary = "Resolve an alert", description = "Mark an alert as resolved")
    @ApiResponses( {
            @ApiResponse(responseCode = "200", description = "Alert resolved successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found")
    })
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<AlertOperationResponseDTO> resolveAlert(
            @Parameter(description = "Alert ID", required = true)
            @PathVariable String alertId,
            @Valid @RequestBody ResolveAlertRequestDTO request) {

        log.info("Resolving alert: {} with resolution: {}", alertId, request.getResolution());

        AlertOperationResponseDTO response = monitoringApplicationService.resolveAlert(alertId, request);

        if(response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Create alert rule
     * @param request Create rule request
     * @return Rule operation response
     */
    @PostMapping("/alerts/rules")
    @Operation(summary = "Create alert rule", description = "Create a new alert rule")
    @ApiResponses( {
            @ApiResponse(responseCode = "201", description = "Alert rule created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<AlertRuleOperationResponseDTO> createAlertRule(
            @Valid @RequestBody CreateAlertRuleRequestDTO request) {

        log.info("Creating alert rule: {}", request.getRuleName());

        AlertRuleOperationResponseDTO response = monitoringApplicationService.createAlertRule(request);

        if(response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Health check endpoint
     * @return Health status
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check monitoring system health")
    @ApiResponse(responseCode = "200", description = "System is healthy")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Monitoring system is healthy");
    }
}
