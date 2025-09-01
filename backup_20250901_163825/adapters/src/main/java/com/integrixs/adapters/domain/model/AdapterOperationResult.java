package com.integrixs.adapters.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for adapter operation results
 */
@Data
@Builder
public class AdapterOperationResult {
    private String operationId;
    private boolean success;
    private Object data;
    private String message;
    private String errorCode;
    private String errorDetails;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private Long executionTimeMs;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    @Builder.Default
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
    public static AdapterOperationResult success(String testName, String message) {
        Map<String, String> testData = new HashMap<>();
        testData.put("testName", testName);
        testData.put("result", message);
        return AdapterOperationResult.builder()
                .success(true)
                .data(testData)
                .message(message)
                .build();
    }
    
    /**
     * Create a failure result (alias for error)
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
    public static AdapterOperationResult failure(String testName, String errorMessage, Exception exception) {
        Map<String, Object> testData = new HashMap<>();
        testData.put("testName", testName);
        testData.put("error", errorMessage);
        testData.put("exceptionType", exception.getClass().getName());
        testData.put("exceptionMessage", exception.getMessage());
        return AdapterOperationResult.builder()
                .success(false)
                .data(testData)
                .message(errorMessage)
                .errorCode("TEST_FAILED")
                .errorDetails(exception.toString())
                .build();
    }
    
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
}