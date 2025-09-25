package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.request.ExecutionSearchRequest;
import com.integrixs.backend.api.dto.response.*;
import com.integrixs.backend.domain.model.*;
import com.integrixs.backend.domain.service.ExecutionTraceManager;
import com.integrixs.backend.domain.service.ExecutionMetricsCalculator;
import com.integrixs.backend.infrastructure.websocket.FlowExecutionWebSocketService;
import com.integrixs.backend.infrastructure.notification.AlertNotificationService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for flow execution monitoring
 */
@Service
public class FlowExecutionMonitoringService {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionMonitoringService.class);


    private final ExecutionTraceManager traceManager;
    private final ExecutionMetricsCalculator metricsCalculator;
    private final FlowExecutionWebSocketService webSocketService;
    private final AlertNotificationService alertService;
    private final IntegrationFlowSqlRepository flowRepository;

    // In - memory storage(in production, these would be persisted)
    private final Map<String, ExecutionTrace> activeTraces = new ConcurrentHashMap<>();

    public FlowExecutionMonitoringService(ExecutionTraceManager traceManager,
                                        ExecutionMetricsCalculator metricsCalculator,
                                        FlowExecutionWebSocketService webSocketService,
                                        AlertNotificationService alertService,
                                        IntegrationFlowSqlRepository flowRepository) {
        this.traceManager = traceManager;
        this.metricsCalculator = metricsCalculator;
        this.webSocketService = webSocketService;
        this.alertService = alertService;
        this.flowRepository = flowRepository;
    }
    private final Map<String, List<ExecutionTrace>> executionHistory = new ConcurrentHashMap<>();
    private final Map<String, PerformanceMetrics> flowMetrics = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    // Configuration
    private static final int LONG_RUNNING_THRESHOLD_MINUTES = 30;
    private static final int MAX_HISTORY_PER_FLOW = 1000;
    private static final int CLEANUP_INTERVAL_HOURS = 1;
    private static final int METRICS_UPDATE_INTERVAL_SECONDS = 30;
    private static final int ALERT_CHECK_INTERVAL_MINUTES = 5;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Start background tasks
        scheduler.scheduleAtFixedRate(this::cleanupOldTraces, CLEANUP_INTERVAL_HOURS, CLEANUP_INTERVAL_HOURS, TimeUnit.HOURS);
        scheduler.scheduleAtFixedRate(this::checkAndSendAlerts, ALERT_CHECK_INTERVAL_MINUTES, ALERT_CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Start monitoring a flow execution
     */
    public String startMonitoring(String flowId, String flowType) {
        log.debug("Starting monitoring for flow: {}, type: {}", flowId, flowType);

        ExecutionTrace trace = traceManager.createTrace(flowId, flowType);
        activeTraces.put(trace.getExecutionId(), trace);

        // Notify WebSocket clients
        webSocketService.broadcastExecutionStarted(flowId, trace.getExecutionId());

        return trace.getExecutionId();
    }

    /**
     * Update execution progress
     */
    public void updateProgress(String executionId, String step, String message) {
        log.debug("Updating progress for execution: {}, step: {}", executionId, step);

        ExecutionTrace trace = activeTraces.get(executionId);
        if(trace != null) {
            traceManager.updateProgress(trace, step, message);

            // Notify WebSocket clients
            webSocketService.broadcastExecutionProgress(
                trace.getFlowId(),
                executionId,
                step,
                message
           );
        }
    }

    /**
     * Complete execution
     */
    public void completeExecution(String executionId, boolean success, String message) {
        log.debug("Completing execution: {}, success: {}", executionId, success);

        ExecutionTrace trace = activeTraces.get(executionId);
        if(trace != null) {
            traceManager.completeExecution(trace, success, message);

            // Update metrics
            updateFlowMetrics(trace);

            // Move to history
            moveToHistory(trace);

            // Notify WebSocket clients
            webSocketService.broadcastExecutionCompleted(trace.getFlowId(), executionId, success);

            // Send alert for failures if configured
            if(!success) {
                alertService.sendFailureAlert(trace);
            }
        }
    }

    /**
     * Record execution error
     */
    public void recordError(String executionId, String errorMessage, Throwable exception) {
        log.error("Recording error for execution: {}, error: {}", executionId, errorMessage);

        ExecutionTrace trace = activeTraces.get(executionId);
        if(trace != null) {
            traceManager.recordError(trace, errorMessage, exception);

            // Update metrics
            updateFlowMetrics(trace);

            // Move to history
            moveToHistory(trace);

            // Notify WebSocket clients
            webSocketService.broadcastExecutionError(trace.getFlowId(), executionId, errorMessage);

            // Send alert
            alertService.sendErrorAlert(trace);
        }
    }

    /**
     * Cancel execution
     */
    public boolean cancelExecution(String executionId) {
        log.debug("Cancelling execution: {}", executionId);

        ExecutionTrace trace = activeTraces.get(executionId);
        if(trace != null && trace.getStatus() == ExecutionStatus.RUNNING) {
            traceManager.cancelExecution(trace);

            // Move to history
            moveToHistory(trace);

            // Notify WebSocket clients
            webSocketService.broadcastExecutionCancelled(trace.getFlowId(), executionId);

            return true;
        }
        return false;
    }

    /**
     * Get execution trace
     */
    public Optional<FlowExecutionTraceResponse> getExecutionTrace(String executionId) {
        ExecutionTrace trace = activeTraces.get(executionId);
        if(trace == null) {
            // Check history
            for(List<ExecutionTrace> history : executionHistory.values()) {
                Optional<ExecutionTrace> historicalTrace = history.stream()
                    .filter(t -> t.getExecutionId().equals(executionId))
                    .findFirst();
                if(historicalTrace.isPresent()) {
                    return Optional.of(convertToResponse(historicalTrace.get()));
                }
            }
            return Optional.empty();
        }
        return Optional.of(convertToResponse(trace));
    }

    /**
     * Get active executions
     */
    public List<FlowExecutionTraceResponse> getActiveExecutions() {
        return activeTraces.values().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Search executions
     */
    public List<FlowExecutionTraceResponse> searchExecutions(ExecutionSearchRequest request) {
        return activeTraces.values().stream()
            .filter(trace -> matchesCriteria(trace, request))
            .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
            .limit(request.getLimit())
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get execution statistics
     */
    public FlowExecutionStatisticsResponse getStatistics() {
        Map<ExecutionStatus, Long> statusCounts = activeTraces.values().stream()
            .collect(Collectors.groupingBy(ExecutionTrace::getStatus, Collectors.counting()));

        List<Long> executionTimes = activeTraces.values().stream()
            .filter(trace -> trace.getExecutionDurationMs() > 0)
            .map(ExecutionTrace::getExecutionDurationMs)
            .collect(Collectors.toList());

        long uniqueFlows = activeTraces.values().stream()
            .map(ExecutionTrace::getFlowId)
            .distinct()
            .count();

        return FlowExecutionStatisticsResponse.builder()
            .activeExecutions(activeTraces.size())
            .runningExecutions(statusCounts.getOrDefault(ExecutionStatus.RUNNING, 0L).intValue())
            .completedExecutions(statusCounts.getOrDefault(ExecutionStatus.COMPLETED, 0L).intValue())
            .failedExecutions(statusCounts.getOrDefault(ExecutionStatus.FAILED, 0L).intValue())
            .averageExecutionTimeMs(metricsCalculator.calculateAverageTime(executionTimes))
            .uniqueFlowsMonitored((int) uniqueFlows)
            .build();
    }

    /**
     * Get flow performance metrics
     */
    public Optional<FlowPerformanceMetricsResponse> getFlowMetrics(String flowId) {
        PerformanceMetrics metrics = flowMetrics.get(flowId);
        if(metrics == null) {
            return Optional.empty();
        }

        // Get flow name
        String flowName = flowRepository.findById(UUID.fromString(flowId))
            .map(IntegrationFlow::getName)
            .orElse("Unknown Flow");

        return Optional.of(FlowPerformanceMetricsResponse.builder()
            .flowId(flowId)
            .flowName(flowName)
            .totalExecutions(metrics.getTotalExecutions())
            .successfulExecutions(metrics.getSuccessfulExecutions())
            .failedExecutions(metrics.getFailedExecutions())
            .successRate(metrics.getSuccessRate())
            .averageExecutionTimeMs(metrics.getAverageExecutionTimeMs())
            .minExecutionTimeMs(metrics.getMinExecutionTimeMs())
            .maxExecutionTimeMs(metrics.getMaxExecutionTimeMs())
            .lastUpdate(metrics.getLastUpdate())
            .build());
    }

    /**
     * Get execution alerts
     */
    public List<ExecutionAlertResponse> getAlerts() {
        List<ExecutionAlert> alerts = detectAlerts();

        return alerts.stream()
            .map(this::convertAlertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get execution history for a flow
     */
    public List<FlowExecutionTraceResponse> getFlowExecutionHistory(String flowId, int limit) {
        List<ExecutionTrace> history = executionHistory.getOrDefault(flowId, new ArrayList<>());

        return history.stream()
            .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
            .limit(limit)
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Private helper methods

    private void moveToHistory(ExecutionTrace trace) {
        activeTraces.remove(trace.getExecutionId());
        executionHistory.computeIfAbsent(trace.getFlowId(), k -> new ArrayList<>()).add(trace);
    }

    private void updateFlowMetrics(ExecutionTrace trace) {
        PerformanceMetrics metrics = flowMetrics.computeIfAbsent(trace.getFlowId(), k -> new PerformanceMetrics());
        metrics.setFlowId(trace.getFlowId());
        metrics.setLastUpdate(LocalDateTime.now());
        metrics.setTotalExecutions(metrics.getTotalExecutions() + 1);

        if(trace.getStatus() == ExecutionStatus.COMPLETED) {
            metrics.setSuccessfulExecutions(metrics.getSuccessfulExecutions() + 1);
        } else {
            metrics.setFailedExecutions(metrics.getFailedExecutions() + 1);
        }

        if(trace.getExecutionDurationMs() > 0) {
            // Update min/max
            if(metrics.getMinExecutionTimeMs() == 0 || trace.getExecutionDurationMs() < metrics.getMinExecutionTimeMs()) {
                metrics.setMinExecutionTimeMs(trace.getExecutionDurationMs());
            }
            if(trace.getExecutionDurationMs() > metrics.getMaxExecutionTimeMs()) {
                metrics.setMaxExecutionTimeMs(trace.getExecutionDurationMs());
            }

            // Update average
            double newAverage = metricsCalculator.updateRunningAverage(
                metrics.getAverageExecutionTimeMs(),
                metrics.getTotalExecutions() - 1,
                trace.getExecutionDurationMs()
           );
            metrics.setAverageExecutionTimeMs(newAverage);
        }
    }

    private List<ExecutionAlert> detectAlerts() {
        List<ExecutionAlert> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for(ExecutionTrace trace : activeTraces.values()) {
            // Check for long - running executions
            if(trace.getStatus() == ExecutionStatus.RUNNING &&
                metricsCalculator.isLongRunning(trace.getStartTime(), LONG_RUNNING_THRESHOLD_MINUTES)) {

                alerts.add(ExecutionAlert.builder()
                    .type(AlertType.LONG_RUNNING)
                    .executionId(trace.getExecutionId())
                    .flowId(trace.getFlowId())
                    .message("Execution has been running for over " + LONG_RUNNING_THRESHOLD_MINUTES + " minutes")
                    .timestamp(now)
                    .build());
            }

            // Check for failed executions
            if(trace.getStatus() == ExecutionStatus.FAILED || trace.getStatus() == ExecutionStatus.ERROR) {
                alerts.add(ExecutionAlert.builder()
                    .type(AlertType.EXECUTION_FAILED)
                    .executionId(trace.getExecutionId())
                    .flowId(trace.getFlowId())
                    .message("Execution failed: " + (trace.getErrorMessage() != null ? trace.getErrorMessage() : "Unknown error"))
                    .timestamp(trace.getEndTime() != null ? trace.getEndTime() : now)
                    .build());
            }
        }

        return alerts;
    }

    private void checkAndSendAlerts() {
        try {
            List<ExecutionAlert> alerts = detectAlerts();

            // Group by flow for efficient notification
            Map<String, List<ExecutionAlert>> alertsByFlow = alerts.stream()
                .collect(Collectors.groupingBy(ExecutionAlert::getFlowId));

            for(Map.Entry<String, List<ExecutionAlert>> entry : alertsByFlow.entrySet()) {
                alertService.sendAlerts(entry.getKey(), entry.getValue());
            }
        } catch(Exception e) {
            log.error("Error checking and sending alerts", e);
        }
    }

    private void cleanupOldTraces() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        activeTraces.entrySet().removeIf(entry ->
            entry.getValue().getStartTime().isBefore(cutoff)
       );

        // Cleanup history
        executionHistory.values().forEach(history -> {
            if(history.size() > MAX_HISTORY_PER_FLOW) {
                history.sort((a, b) -> b.getStartTime().compareTo(a.getStartTime()));
                history.subList(MAX_HISTORY_PER_FLOW, history.size()).clear();
            }
        });
    }

    private boolean matchesCriteria(ExecutionTrace trace, ExecutionSearchRequest criteria) {
        if(criteria.getFlowId() != null && !criteria.getFlowId().equals(trace.getFlowId())) {
            return false;
        }
        if(criteria.getStatus() != null && !criteria.getStatus().equals(trace.getStatus().name())) {
            return false;
        }
        if(criteria.getStartTimeAfter() != null && trace.getStartTime().isBefore(criteria.getStartTimeAfter())) {
            return false;
        }
        if(criteria.getStartTimeBefore() != null && trace.getStartTime().isAfter(criteria.getStartTimeBefore())) {
            return false;
        }
        return true;
    }

    private FlowExecutionTraceResponse convertToResponse(ExecutionTrace trace) {
        // Get flow name
        String flowName = flowRepository.findById(UUID.fromString(trace.getFlowId()))
            .map(IntegrationFlow::getName)
            .orElse("Unknown Flow");

        List<FlowExecutionTraceResponse.TraceEventResponse> eventResponses = trace.getEvents().stream()
            .map(event -> FlowExecutionTraceResponse.TraceEventResponse.builder()
                .eventType(event.getEventType())
                .message(event.getMessage())
                .timestamp(event.getTimestamp())
                .build())
            .collect(Collectors.toList());

        return FlowExecutionTraceResponse.builder()
            .executionId(trace.getExecutionId())
            .flowId(trace.getFlowId())
            .flowName(flowName)
            .flowType(trace.getFlowType())
            .status(trace.getStatus().name())
            .currentStep(trace.getCurrentStep())
            .startTime(trace.getStartTime())
            .endTime(trace.getEndTime())
            .lastUpdate(trace.getLastUpdate())
            .executionDurationMs(trace.getExecutionDurationMs())
            .completionMessage(trace.getCompletionMessage())
            .errorMessage(trace.getErrorMessage())
            .events(eventResponses)
            .build();
    }

    private ExecutionAlertResponse convertAlertToResponse(ExecutionAlert alert) {
        // Get flow name
        String flowName = flowRepository.findById(UUID.fromString(alert.getFlowId()))
            .map(IntegrationFlow::getName)
            .orElse("Unknown Flow");

        return ExecutionAlertResponse.builder()
            .type(alert.getType().name())
            .severity(alert.getType().getSeverity())
            .executionId(alert.getExecutionId())
            .flowId(alert.getFlowId())
            .flowName(flowName)
            .message(alert.getMessage())
            .timestamp(alert.getTimestamp())
            .build();
    }
}
