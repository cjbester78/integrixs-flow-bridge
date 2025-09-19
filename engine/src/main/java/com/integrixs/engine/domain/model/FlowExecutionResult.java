package com.integrixs.engine.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for flow execution result
 */
public class FlowExecutionResult {
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

    /**
     * Create a successful result
     * @param executionId Execution ID
     * @param processedData The processed data
     * @return Success result
     */
    public static FlowExecutionResult success(String executionId, Object processedData) {
        return FlowExecutionResult.builder()
                .executionId(executionId)
                .success(true)
                .processedData(processedData)
                .build();
    }

    /**
     * Create an error result
     * @param executionId Execution ID
     * @param errorMessage The error message
     * @param errorCode The error code
     * @return Error result
     */
    public static FlowExecutionResult error(String executionId, String errorMessage, String errorCode) {
        return FlowExecutionResult.builder()
                .executionId(executionId)
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }

    /**
     * Add a warning to the result
     * @param warning Warning message
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * Add metadata to the result
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    // Default constructor
    public FlowExecutionResult() {
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static FlowExecutionResultBuilder builder() {
        return new FlowExecutionResultBuilder();
    }

    public static class FlowExecutionResultBuilder {
        private String executionId;
        private String flowId;
        private boolean success;
        private Object processedData;
        private String errorMessage;
        private String errorCode;
        private LocalDateTime timestamp;
        private Long executionTimeMs;
        private Map<String, Object> metadata;
        private List<String> warnings;
        private String inboundAdapterId;
        private String outboundAdapterId;
        private Integer recordsProcessed;

        public FlowExecutionResultBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public FlowExecutionResultBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public FlowExecutionResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public FlowExecutionResultBuilder processedData(Object processedData) {
            this.processedData = processedData;
            return this;
        }

        public FlowExecutionResultBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public FlowExecutionResultBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public FlowExecutionResultBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public FlowExecutionResultBuilder executionTimeMs(Long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public FlowExecutionResultBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public FlowExecutionResultBuilder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public FlowExecutionResultBuilder inboundAdapterId(String inboundAdapterId) {
            this.inboundAdapterId = inboundAdapterId;
            return this;
        }

        public FlowExecutionResultBuilder outboundAdapterId(String outboundAdapterId) {
            this.outboundAdapterId = outboundAdapterId;
            return this;
        }

        public FlowExecutionResultBuilder recordsProcessed(Integer recordsProcessed) {
            this.recordsProcessed = recordsProcessed;
            return this;
        }

        public FlowExecutionResult build() {
            FlowExecutionResult instance = new FlowExecutionResult();
            instance.setExecutionId(this.executionId);
            instance.setFlowId(this.flowId);
            instance.setSuccess(this.success);
            instance.setProcessedData(this.processedData);
            instance.setErrorMessage(this.errorMessage);
            instance.setErrorCode(this.errorCode);
            instance.setTimestamp(this.timestamp != null ? this.timestamp : LocalDateTime.now());
            instance.setExecutionTimeMs(this.executionTimeMs);
            instance.setMetadata(this.metadata != null ? this.metadata : new HashMap<>());
            instance.setWarnings(this.warnings != null ? this.warnings : new ArrayList<>());
            instance.setInboundAdapterId(this.inboundAdapterId);
            instance.setOutboundAdapterId(this.outboundAdapterId);
            instance.setRecordsProcessed(this.recordsProcessed);
            return instance;
        }
    }
}
