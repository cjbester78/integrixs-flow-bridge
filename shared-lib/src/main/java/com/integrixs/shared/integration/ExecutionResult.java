package com.integrixs.shared.integration;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Result of flow execution
 */
public class ExecutionResult {
    private String executionId;
    private String flowId;
    private ExecutionStatus status;
    private Map<String, Object> outputData;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMs;
    private Map<String, Object> metadata;

    // Default constructor
    public ExecutionResult() {
    }

    // All args constructor
    public ExecutionResult(String executionId, String flowId, ExecutionStatus status,
                          Map<String, Object> outputData, String errorMessage,
                          LocalDateTime startTime, LocalDateTime endTime,
                          Long durationMs, Map<String, Object> metadata) {
        this.executionId = executionId;
        this.flowId = flowId;
        this.status = status;
        this.outputData = outputData;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.metadata = metadata;
    }

    // Getters
    public String getExecutionId() {
        return executionId;
    }

    public String getFlowId() {
        return flowId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Map<String, Object> getOutputData() {
        return outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    // Setters
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public void setOutputData(Map<String, Object> outputData) {
        this.outputData = outputData;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String executionId;
        private String flowId;
        private ExecutionStatus status;
        private Map<String, Object> outputData;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMs;
        private Map<String, Object> metadata;

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public Builder status(ExecutionStatus status) {
            this.status = status;
            return this;
        }

        public Builder outputData(Map<String, Object> outputData) {
            this.outputData = outputData;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ExecutionResult build() {
            return new ExecutionResult(executionId, flowId, status, outputData,
                                     errorMessage, startTime, endTime, durationMs, metadata);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionResult that = (ExecutionResult) o;
        return Objects.equals(executionId, that.executionId) &&
               Objects.equals(flowId, that.flowId) &&
               status == that.status &&
               Objects.equals(outputData, that.outputData) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime) &&
               Objects.equals(durationMs, that.durationMs) &&
               Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, flowId, status, outputData, errorMessage,
                          startTime, endTime, durationMs, metadata);
    }

    @Override
    public String toString() {
        return "ExecutionResult{" +
                "executionId='" + executionId + '\'' +
                ", flowId='" + flowId + '\'' +
                ", status=" + status +
                ", outputData=" + outputData +
                ", errorMessage='" + errorMessage + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", durationMs=" + durationMs +
                ", metadata=" + metadata +
                '}';
    }
}
