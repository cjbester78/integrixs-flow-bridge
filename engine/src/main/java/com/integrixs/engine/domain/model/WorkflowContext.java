package com.integrixs.engine.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for workflow execution context
 * Represents the overall workflow state and metadata
 */
@Data
@Builder
public class WorkflowContext {
    private String workflowId;
    private String flowId;
    private String executionId;
    private WorkflowState state;
    @Builder.Default
    private List<WorkflowStep> steps = new ArrayList<>();
    private WorkflowStep currentStep;
    @Builder.Default
    private Map<String, Object> globalVariables = new HashMap<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private String correlationId;
    private Long startTime;
    private Long endTime;
    private String initiatedBy;

    /**
     * Workflow execution states
     */
    public enum WorkflowState {
        INITIATED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED,
        SUSPENDED
    }

    /**
     * Add a workflow step
     * @param step The step to add
     */
    public void addStep(WorkflowStep step) {
        this.steps.add(step);
    }

    /**
     * Add a global variable
     * @param key Variable key
     * @param value Variable value
     */
    public void addGlobalVariable(String key, Object value) {
        this.globalVariables.put(key, value);
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
     * Get execution duration
     * @return Duration in milliseconds
     */
    public Long getDuration() {
        if(startTime != null && endTime != null) {
            return endTime - startTime;
        }
        return null;
    }
}
