package com.integrixs.engine.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for adapter execution result
 */
@Data
@Builder
public class AdapterExecutionResult {
    private String executionId;
    private boolean success;
    private Object data;
    private String errorMessage;
    private String errorCode;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private Long executionTimeMs;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    @Builder.Default
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
}