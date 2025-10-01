package com.integrixs.backend.domain.service;

import com.integrixs.backend.domain.model.ExecutionTrace;
import com.integrixs.backend.domain.model.TraceEvent;
import com.integrixs.backend.domain.model.ExecutionStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Domain service for managing execution traces
 */
@Service
public class ExecutionTraceManager {

    private final ExecutionMetricsCalculator metricsCalculator;

    public ExecutionTraceManager(ExecutionMetricsCalculator metricsCalculator) {
        this.metricsCalculator = metricsCalculator;
    }

    /**
     * Creates a new execution trace
     */
    public ExecutionTrace createTrace(String flowId, String flowType) {
        ExecutionTrace trace = new ExecutionTrace();
        trace.setExecutionId(UUID.randomUUID().toString());
        trace.setFlowId(flowId);
        trace.setFlowType(flowType);
        trace.setStatus(ExecutionStatus.STARTED);
        trace.setStartTime(LocalDateTime.now());

        // Add initial event
        trace.addEvent(createEvent("EXECUTION_STARTED", "Flow execution monitoring started"));

        return trace;
    }

    /**
     * Updates trace progress
     */
    public void updateProgress(ExecutionTrace trace, String step, String message) {
        trace.setCurrentStep(step);
        trace.setLastUpdate(LocalDateTime.now());
        trace.setStatus(ExecutionStatus.RUNNING);
        trace.addEvent(createEvent("STEP_PROGRESS", step + ": " + message));
    }

    /**
     * Completes trace execution
     */
    public void completeExecution(ExecutionTrace trace, boolean success, String message) {
        trace.setStatus(success ? ExecutionStatus.COMPLETED : ExecutionStatus.FAILED);
        trace.setEndTime(LocalDateTime.now());
        trace.setCompletionMessage(message);
        trace.addEvent(createEvent("EXECUTION_COMPLETED", message));

        // Calculate duration
        long duration = metricsCalculator.calculateDuration(trace.getStartTime(), trace.getEndTime());
        trace.setExecutionDurationMs(duration);
    }

    /**
     * Records an error in the trace
     */
    public void recordError(ExecutionTrace trace, String errorMessage, Throwable exception) {
        trace.setStatus(ExecutionStatus.ERROR);
        trace.setEndTime(LocalDateTime.now());
        trace.setErrorMessage(errorMessage);

        if(exception != null) {
            trace.setExceptionDetails(getStackTrace(exception));
        }

        trace.addEvent(createEvent("EXECUTION_ERROR", errorMessage));

        // Calculate duration
        long duration = metricsCalculator.calculateDuration(trace.getStartTime(), trace.getEndTime());
        trace.setExecutionDurationMs(duration);
    }

    /**
     * Cancels a trace
     */
    public void cancelExecution(ExecutionTrace trace) {
        trace.setStatus(ExecutionStatus.CANCELLED);
        trace.setEndTime(LocalDateTime.now());
        trace.addEvent(createEvent("EXECUTION_CANCELLED", "Execution cancelled by user"));

        // Calculate duration
        long duration = metricsCalculator.calculateDuration(trace.getStartTime(), trace.getEndTime());
        trace.setExecutionDurationMs(duration);
    }

    /**
     * Creates a new trace event
     */
    private TraceEvent createEvent(String eventType, String message) {
        TraceEvent event = new TraceEvent();
        event.setEventType(eventType);
        event.setMessage(message);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    /**
     * Gets stack trace as string
     */
    private String getStackTrace(Throwable exception) {
        return Arrays.stream(exception.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n\t", 
                    exception.getClass().getName() + ": " + exception.getMessage() + "\n\t", ""));
    }
}
