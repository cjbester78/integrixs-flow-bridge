package com.integrixs.backend.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for flow execution trace
 */
public class ExecutionTrace {

    private String executionId;
    private String flowId;
    private String flowType;
    private ExecutionStatus status;
    private String currentStep;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdate;
    private long executionDurationMs;
    private String completionMessage;
    private String errorMessage;
    private String exceptionDetails;
    private List<TraceEvent> events = new ArrayList<>();

    /**
     * Adds a new event to the trace
     */
    public void addEvent(TraceEvent event) {
        events.add(event);
    }

    // Default constructor
    public ExecutionTrace() {
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public String getCompletionMessage() {
        return completionMessage;
    }

    public void setCompletionMessage(String completionMessage) {
        this.completionMessage = completionMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getExceptionDetails() {
        return exceptionDetails;
    }

    public void setExceptionDetails(String exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    public List<TraceEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TraceEvent> events) {
        this.events = events;
    }
}
