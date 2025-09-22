package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.ExecutionSearchRequest;
import com.integrixs.backend.api.dto.response.*;
import com.integrixs.backend.application.service.FlowExecutionMonitoringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for flow execution monitoring
 */
@RestController
@RequestMapping("/api/flow - executions")
@Validated
@Tag(name = "Flow Execution Monitoring", description = "Monitor and manage flow executions")
public class FlowExecutionMonitoringController {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionMonitoringController.class);


    private final FlowExecutionMonitoringService monitoringService;

    public FlowExecutionMonitoringController(FlowExecutionMonitoringService monitoringService) {
        this.monitoringService = monitoringService;
    }

    @PostMapping("/start")
    @Operation(summary = "Start monitoring a flow execution", description = "Begin monitoring a new flow execution")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<String> startMonitoring(
            @Parameter(description = "Flow ID") @RequestParam String flowId,
            @Parameter(description = "Flow type") @RequestParam(defaultValue = "STANDARD") String flowType) {

        log.debug("Starting monitoring for flow: {}, type: {}", flowId, flowType);
        String executionId = monitoringService.startMonitoring(flowId, flowType);
        return ResponseEntity.status(HttpStatus.CREATED).body(executionId);
    }

    @PutMapping("/ {executionId}/progress")
    @Operation(summary = "Update execution progress", description = "Update the progress of a running execution")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<Void> updateProgress(
            @Parameter(description = "Execution ID") @PathVariable String executionId,
            @Parameter(description = "Current step") @RequestParam String step,
            @Parameter(description = "Step message") @RequestParam String message) {

        log.debug("Updating progress for execution: {}, step: {}", executionId, step);
        monitoringService.updateProgress(executionId, step, message);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ {executionId}/complete")
    @Operation(summary = "Complete execution", description = "Mark an execution as completed")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<Void> completeExecution(
            @Parameter(description = "Execution ID") @PathVariable String executionId,
            @Parameter(description = "Success flag") @RequestParam boolean success,
            @Parameter(description = "Completion message") @RequestParam String message) {

        log.debug("Completing execution: {}, success: {}", executionId, success);
        monitoringService.completeExecution(executionId, success, message);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ {executionId}/error")
    @Operation(summary = "Record execution error", description = "Record an error for a running execution")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR')")
    public ResponseEntity<Void> recordError(
            @Parameter(description = "Execution ID") @PathVariable String executionId,
            @Parameter(description = "Error message") @RequestParam String errorMessage,
            @Parameter(description = "Exception details") @RequestParam(required = false) String exceptionDetails) {

        log.debug("Recording error for execution: {}, error: {}", executionId, errorMessage);
        monitoringService.recordError(executionId, errorMessage, null);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/ {executionId}")
    @Operation(summary = "Cancel execution", description = "Cancel a running execution")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER')")
    public ResponseEntity<Boolean> cancelExecution(
            @Parameter(description = "Execution ID") @PathVariable String executionId) {

        log.debug("Cancelling execution: {}", executionId);
        boolean cancelled = monitoringService.cancelExecution(executionId);
        return ResponseEntity.ok(cancelled);
    }

    @GetMapping("/ {executionId}")
    @Operation(summary = "Get execution trace", description = "Get detailed trace information for an execution")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<FlowExecutionTraceResponse> getExecutionTrace(
            @Parameter(description = "Execution ID") @PathVariable String executionId) {

        log.debug("Getting execution trace: {}", executionId);
        return monitoringService.getExecutionTrace(executionId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active executions", description = "Get all currently active executions")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<List<FlowExecutionTraceResponse>> getActiveExecutions() {
        log.debug("Getting active executions");
        List<FlowExecutionTraceResponse> executions = monitoringService.getActiveExecutions();
        return ResponseEntity.ok(executions);
    }

    @PostMapping("/search")
    @Operation(summary = "Search executions", description = "Search for executions based on criteria")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<List<FlowExecutionTraceResponse>> searchExecutions(
            @Valid @RequestBody ExecutionSearchRequest request) {

        log.debug("Searching executions with criteria: {}", request);
        List<FlowExecutionTraceResponse> executions = monitoringService.searchExecutions(request);
        return ResponseEntity.ok(executions);
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get execution statistics", description = "Get overall execution statistics")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<FlowExecutionStatisticsResponse> getStatistics() {
        log.debug("Getting execution statistics");
        FlowExecutionStatisticsResponse statistics = monitoringService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/flows/ {flowId}/metrics")
    @Operation(summary = "Get flow performance metrics", description = "Get performance metrics for a specific flow")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<FlowPerformanceMetricsResponse> getFlowMetrics(
            @Parameter(description = "Flow ID") @PathVariable String flowId) {

        log.debug("Getting performance metrics for flow: {}", flowId);
        return monitoringService.getFlowMetrics(flowId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/flows/ {flowId}/history")
    @Operation(summary = "Get flow execution history", description = "Get execution history for a specific flow")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<List<FlowExecutionTraceResponse>> getFlowExecutionHistory(
            @Parameter(description = "Flow ID") @PathVariable String flowId,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        log.debug("Getting execution history for flow: {}, limit: {}", flowId, limit);
        List<FlowExecutionTraceResponse> history = monitoringService.getFlowExecutionHistory(flowId, limit);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/alerts")
    @Operation(summary = "Get execution alerts", description = "Get current execution alerts")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    public ResponseEntity<List<ExecutionAlertResponse>> getAlerts() {
        log.debug("Getting execution alerts");
        List<ExecutionAlertResponse> alerts = monitoringService.getAlerts();
        return ResponseEntity.ok(alerts);
    }
}
