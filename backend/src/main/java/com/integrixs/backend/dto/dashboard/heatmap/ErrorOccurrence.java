package com.integrixs.backend.dto.dashboard.heatmap;

import java.time.LocalDateTime;

/**
 * Single error occurrence.
 */
public class ErrorOccurrence {
    private LocalDateTime timestamp;
    private String flowId;
    private String component;
    private String errorMessage;

    // Default constructor
    public ErrorOccurrence() {
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
