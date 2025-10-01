package com.integrixs.engine.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for adapter execution result
 */
public class AdapterExecutionResult {
    private String executionId;
    private boolean success;
    private Object data;
    private String errorMessage;
    private String errorCode;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Long executionTimeMs;
    private Map<String, Object> metadata = new HashMap<>();
    private List<String> warnings = new ArrayList<>();
    private String adapterType;
    private String adapterId;

    /**
     * Create a successful result
     * @param data The result data
     * @return Success result
     */
    public static AdapterExecutionResult success(Object data) {
        return AdapterExecutionResult.builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Create an error result
     * @param errorMessage The error message
     * @param errorCode The error code
     * @return Error result
     */
    public static AdapterExecutionResult error(String errorMessage, String errorCode) {
        return AdapterExecutionResult.builder()
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
    public AdapterExecutionResult() {
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static AdapterExecutionResultBuilder builder() {
        return new AdapterExecutionResultBuilder();
    }

    public static class AdapterExecutionResultBuilder {
        private String executionId;
        private boolean success;
        private Object data;
        private String errorMessage;
        private String errorCode;
        private LocalDateTime timestamp;
        private Long executionTimeMs;
        private Map<String, Object> metadata;
        private List<String> warnings;
        private String adapterType;
        private String adapterId;

        public AdapterExecutionResultBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public AdapterExecutionResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public AdapterExecutionResultBuilder data(Object data) {
            this.data = data;
            return this;
        }

        public AdapterExecutionResultBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public AdapterExecutionResultBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public AdapterExecutionResultBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public AdapterExecutionResultBuilder executionTimeMs(Long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public AdapterExecutionResultBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public AdapterExecutionResultBuilder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public AdapterExecutionResultBuilder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public AdapterExecutionResultBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public AdapterExecutionResult build() {
            AdapterExecutionResult instance = new AdapterExecutionResult();
            instance.setExecutionId(this.executionId);
            instance.setSuccess(this.success);
            instance.setData(this.data);
            instance.setErrorMessage(this.errorMessage);
            instance.setErrorCode(this.errorCode);
            instance.setTimestamp(this.timestamp != null ? this.timestamp : LocalDateTime.now());
            instance.setExecutionTimeMs(this.executionTimeMs);
            instance.setMetadata(this.metadata != null ? this.metadata : new HashMap<>());
            instance.setWarnings(this.warnings != null ? this.warnings : new ArrayList<>());
            instance.setAdapterType(this.adapterType);
            instance.setAdapterId(this.adapterId);
            return instance;
        }
    }
}
