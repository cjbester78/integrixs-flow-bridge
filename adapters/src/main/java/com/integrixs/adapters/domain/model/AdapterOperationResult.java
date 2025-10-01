package com.integrixs.adapters.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for adapter operation results
 */
public class AdapterOperationResult {
    private String operationId;
    private boolean success;
    private Object data;
    private String message;
    private String errorCode;
    private String errorDetails;
    private LocalDateTime timestamp = LocalDateTime.now();
    private Long executionTimeMs;
    private Map<String, Object> metadata = new HashMap<>();
    private List<String> warnings = new ArrayList<>();
    private Integer recordsProcessed;
    private Integer recordsFailed;
    private String adapterId;
    private String adapterType;

    /**
     * Create a successful result
     * @param data Result data
     * @return Success result
     */
    public static AdapterOperationResult success(Object data) {
        return AdapterOperationResult.builder()
                .success(true)
                .data(data)
                .message("Operation completed successfully")
                .build();
    }

    /**
     * Create an error result
     * @param errorMessage Error message
     * @param errorCode Error code
     * @return Error result
     */
    public static AdapterOperationResult error(String errorMessage, String errorCode) {
        return AdapterOperationResult.builder()
                .success(false)
                .message(errorMessage)
                .errorCode(errorCode)
                .build();
    }

    /**
     * Add metadata
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Add warning
     * @param warning Warning message
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * Create a successful result with message only
     * @param message Success message
     * @return Success result
     */
    public static AdapterOperationResult success(String message) {
        return AdapterOperationResult.builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * Create a successful result with data and message
     * @param data Result data
     * @param message Success message
     * @return Success result
     */
    public static AdapterOperationResult success(Object data, String message) {
        return AdapterOperationResult.builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    /**
     * Create a successful result for test operations
     * @param testName Name of the test
     * @param message Test result message
     * @return Success result
     */

    /**
     * Create a failure result(alias for error)
     * @param errorMessage Error message
     * @return Error result
     */
    public static AdapterOperationResult failure(String errorMessage) {
        return error(errorMessage, "OPERATION_FAILED");
    }

    /**
     * Create a failure result with test name
     * @param testName Name of the test
     * @param errorMessage Error message
     * @return Error result
     */
    public static AdapterOperationResult failure(String testName, String errorMessage) {
        Map<String, String> testData = new HashMap<>();
        testData.put("testName", testName);
        testData.put("error", errorMessage);
        return AdapterOperationResult.builder()
                .success(false)
                .data(testData)
                .message(errorMessage)
                .errorCode("TEST_FAILED")
                .build();
    }

    /**
     * Create a failure result with test name and exception
     * @param testName Name of the test
     * @param errorMessage Error message
     * @param exception The exception that caused the failure
     * @return Error result
     */

    /**
     * Add metadata to result
     * @param metadata Metadata map
     * @return This result
     */
    public AdapterOperationResult withMetadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }

    /**
     * Check if operation was successful
     * @return true if successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Set records processed count
     * @param count Number of records processed
     * @return This result
     */
    public AdapterOperationResult withRecordsProcessed(long count) {
        this.recordsProcessed = (int) count;
        return this;
    }
    // Getters and Setters
    public String getOperationId() {
        return operationId;
    }
    public void setOperationId(String operationId) {
        this.operationId = operationId;
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
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public String getErrorDetails() {
        return errorDetails;
    }
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
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
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    public List<String> getWarnings() {
        return warnings;
    }
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }
    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }
    public Integer getRecordsFailed() {
        return recordsFailed;
    }
    public void setRecordsFailed(Integer recordsFailed) {
        this.recordsFailed = recordsFailed;
    }
    public String getAdapterId() {
        return adapterId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }
    public String getAdapterType() {
        return adapterType;
    }
    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String operationId;
        private boolean success;
        private Object data;
        private String message;
        private String errorCode;
        private String errorDetails;
        private LocalDateTime timestamp;
        private Long executionTimeMs;
        private Map<String, Object> metadata;
        private List<String> warnings;
        private Integer recordsProcessed;
        private Integer recordsFailed;
        private String adapterId;
        private String adapterType;

        public Builder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder data(Object data) {
            this.data = data;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder errorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder executionTimeMs(Long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public Builder recordsProcessed(Integer recordsProcessed) {
            this.recordsProcessed = recordsProcessed;
            return this;
        }

        public Builder recordsFailed(Integer recordsFailed) {
            this.recordsFailed = recordsFailed;
            return this;
        }

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public AdapterOperationResult build() {
            AdapterOperationResult obj = new AdapterOperationResult();
            obj.operationId = this.operationId;
            obj.success = this.success;
            obj.data = this.data;
            obj.message = this.message;
            obj.errorCode = this.errorCode;
            obj.errorDetails = this.errorDetails;
            obj.timestamp = this.timestamp;
            obj.executionTimeMs = this.executionTimeMs;
            obj.metadata = this.metadata;
            obj.warnings = this.warnings;
            obj.recordsProcessed = this.recordsProcessed;
            obj.recordsFailed = this.recordsFailed;
            obj.adapterId = this.adapterId;
            obj.adapterType = this.adapterType;
            return obj;
        }
    }
}
