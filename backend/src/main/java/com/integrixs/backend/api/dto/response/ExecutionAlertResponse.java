package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for execution alert
 */
public class ExecutionAlertResponse {

    private String type;
    private String severity;
    private String executionId;
    private String flowId;
    private String flowName;
    private String message;
    private LocalDateTime timestamp;

    // Default constructor
    public ExecutionAlertResponse() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
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

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
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

    public static ExecutionAlertResponseBuilder builder() {
        return new ExecutionAlertResponseBuilder();
    }

    public static class ExecutionAlertResponseBuilder {
        private ExecutionAlertResponse response = new ExecutionAlertResponse();

        public ExecutionAlertResponseBuilder type(String type) {
            response.setType(type);
            return this;
        }

        public ExecutionAlertResponseBuilder severity(String severity) {
            response.setSeverity(severity);
            return this;
        }

        public ExecutionAlertResponseBuilder executionId(String executionId) {
            response.setExecutionId(executionId);
            return this;
        }

        public ExecutionAlertResponseBuilder flowId(String flowId) {
            response.setFlowId(flowId);
            return this;
        }

        public ExecutionAlertResponseBuilder flowName(String flowName) {
            response.setFlowName(flowName);
            return this;
        }

        public ExecutionAlertResponseBuilder message(String message) {
            response.setMessage(message);
            return this;
        }

        public ExecutionAlertResponseBuilder timestamp(LocalDateTime timestamp) {
            response.setTimestamp(timestamp);
            return this;
        }

        public ExecutionAlertResponse build() {
            return response;
        }
    }
}
