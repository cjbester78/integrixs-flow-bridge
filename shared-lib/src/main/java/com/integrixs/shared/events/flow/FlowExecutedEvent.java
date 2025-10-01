package com.integrixs.shared.events.flow;

import com.integrixs.shared.events.AbstractDomainEvent;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Event raised when an integration flow is executed.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class FlowExecutedEvent extends AbstractDomainEvent {

    private String flowId;
    private String executionId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean success;
    private String errorMessage;
    private Long recordsProcessed;
    private String triggeredBy;

    // Default constructor
    public FlowExecutedEvent() {
        super();
    }

    // All args constructor
    public FlowExecutedEvent(String flowId, String executionId, LocalDateTime startTime,
                            LocalDateTime endTime, boolean success, String triggeredBy) {
        super(flowId, triggeredBy);
        this.flowId = flowId;
        this.executionId = executionId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.success = success;
        this.triggeredBy = triggeredBy;
    }

    /**
     * Gets the execution duration.
     *
     * @return duration between start and end time
     */
    public Duration getExecutionDuration() {
        if(startTime != null && endTime != null) {
            return Duration.between(startTime, endTime);
        }
        return Duration.ZERO;
    }

    // Getters
    public String getFlowId() {
        return flowId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getRecordsProcessed() {
        return recordsProcessed;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    // Setters
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setRecordsProcessed(Long recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
}
