package com.integrixs.engine.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing a single step in a workflow
 */
@Data
@Builder
public class WorkflowStep {
    private String stepId;
    private String stepName;
    private StepType stepType;
    private StepStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Object inputData;
    private Object outputData;
    @Builder.Default
    private Map<String, Object> stepVariables = new HashMap<>();
    private String errorMessage;
    private Integer retryCount;
    private String nextStepId;

    /**
     * Step types
     */
    public enum StepType {
        SOURCE_ADAPTER,
        TARGET_ADAPTER,
        TRANSFORMATION,
        ROUTING,
        VALIDATION,
        ENRICHMENT,
        SPLIT,
        AGGREGATE,
        CUSTOM
    }

    /**
     * Step execution status
     */
    public enum StepStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        SKIPPED,
        RETRY
    }

    /**
     * Add a step variable
     * @param key Variable key
     * @param value Variable value
     */
    public void addStepVariable(String key, Object value) {
        this.stepVariables.put(key, value);
    }

    /**
     * Get step duration in milliseconds
     * @return Duration or null if not completed
     */
    public Long getDuration() {
        if(startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMillis();
        }
        return null;
    }
}
