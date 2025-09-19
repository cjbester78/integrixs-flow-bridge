package com.integrixs.backend.domain.model;

import java.time.LocalDateTime;

/**
 * Domain model for execution alert
 */
public class ExecutionAlert {
    private AlertType type;
    private String executionId;
    private String flowId;
    private String message;
    private LocalDateTime timestamp;

    // Default constructor
    public ExecutionAlert() {
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public static ExecutionAlertBuilder builder() {
        return new ExecutionAlertBuilder();
    }

    public static class ExecutionAlertBuilder {
        private ExecutionAlert alert = new ExecutionAlert();

        public ExecutionAlertBuilder type(AlertType type) {
            alert.setType(type);
            return this;
        }

        public ExecutionAlertBuilder executionId(String executionId) {
            alert.setExecutionId(executionId);
            return this;
        }

        public ExecutionAlertBuilder flowId(String flowId) {
            alert.setFlowId(flowId);
            return this;
        }

        public ExecutionAlertBuilder message(String message) {
            alert.setMessage(message);
            return this;
        }

        public ExecutionAlertBuilder timestamp(LocalDateTime timestamp) {
            alert.setTimestamp(timestamp);
            return this;
        }

        public ExecutionAlert build() {
            return alert;
        }
    }
}
