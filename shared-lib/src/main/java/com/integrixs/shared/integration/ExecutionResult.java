package com.integrixs.shared.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Result of flow execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}
