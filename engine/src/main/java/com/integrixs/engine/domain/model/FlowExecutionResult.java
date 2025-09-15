package com.integrixs.engine.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for flow execution result
 */
@Data
@Builder
public class FlowExecutionResult {
    private String executionId;
    private String flowId;
    private boolean success;
    private Object processedData;
    private String errorMessage;
    private String errorCode;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private Long executionTimeMs;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    @Builder.Default
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
}
