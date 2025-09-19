package com.integrixs.engine.api.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for flow execution responses
 */
public class FlowExecutionResponseDTO {
    private String executionId;
    private String flowId;
    private boolean success;
    private Object processedData;
    private String errorMessage;
    private String errorCode;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Long executionTimeMs;
    private Map<String, Object> metadata = new HashMap<>();
    private List<String> warnings = new ArrayList<>();
    private String inboundAdapterId;
    private String outboundAdapterId;
    private Integer recordsProcessed;

    // Default constructor
    public FlowExecutionResponseDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private FlowExecutionResponseDTO dto = new FlowExecutionResponseDTO();

        public Builder executionId(String executionId) {
            dto.executionId = executionId;
            return this;
        }

        public Builder flowId(String flowId) {
            dto.flowId = flowId;
            return this;
        }

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder processedData(Object processedData) {
            dto.processedData = processedData;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            dto.errorMessage = errorMessage;
            return this;
        }

        public Builder errorCode(String errorCode) {
            dto.errorCode = errorCode;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            dto.timestamp = timestamp;
            return this;
        }

        public Builder executionTimeMs(Long executionTimeMs) {
            dto.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            dto.metadata = metadata != null ? metadata : new HashMap<>();
            return this;
        }

        public Builder warnings(List<String> warnings) {
            dto.warnings = warnings != null ? warnings : new ArrayList<>();
            return this;
        }

        public Builder inboundAdapterId(String inboundAdapterId) {
            dto.inboundAdapterId = inboundAdapterId;
            return this;
        }

        public Builder outboundAdapterId(String outboundAdapterId) {
            dto.outboundAdapterId = outboundAdapterId;
            return this;
        }

        public Builder recordsProcessed(Integer recordsProcessed) {
            dto.recordsProcessed = recordsProcessed;
            return this;
        }

        public FlowExecutionResponseDTO build() {
            return dto;
        }
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getProcessedData() {
        return processedData;
    }

    public void setProcessedData(Object processedData) {
        this.processedData = processedData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getInboundAdapterId() {
        return inboundAdapterId;
    }

    public void setInboundAdapterId(String inboundAdapterId) {
        this.inboundAdapterId = inboundAdapterId;
    }

    public String getOutboundAdapterId() {
        return outboundAdapterId;
    }

    public void setOutboundAdapterId(String outboundAdapterId) {
        this.outboundAdapterId = outboundAdapterId;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
}
