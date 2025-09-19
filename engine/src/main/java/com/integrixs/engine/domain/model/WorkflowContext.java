package com.integrixs.engine.domain.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for workflow execution context
 * Represents the overall workflow state and metadata
 */
public class WorkflowContext {
    private String workflowId;
    private String flowId;
    private String executionId;
    private WorkflowState state;
    private List<WorkflowStep> steps = new ArrayList<>();
    private WorkflowStep currentStep;
    private Map<String, Object> globalVariables = new HashMap<>();
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

    // Default constructor
    public WorkflowContext() {
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public WorkflowState getState() {
        return state;
    }

    public void setState(WorkflowState state) {
        this.state = state;
    }

    public List<WorkflowStep> getSteps() {
        return steps;
    }

    public void setSteps(List<WorkflowStep> steps) {
        this.steps = steps;
    }

    public WorkflowStep getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(WorkflowStep currentStep) {
        this.currentStep = currentStep;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public Map<String, Object> getGlobalVariables() {
        return globalVariables;
    }

    public void setGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables = globalVariables;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static WorkflowContextBuilder builder() {
        return new WorkflowContextBuilder();
    }

    public static class WorkflowContextBuilder {
        private String workflowId;
        private String flowId;
        private String executionId;
        private WorkflowState state;
        private List<WorkflowStep> steps;
        private WorkflowStep currentStep;
        private Map<String, Object> globalVariables;
        private Map<String, Object> metadata;
        private String correlationId;
        private Long startTime;
        private Long endTime;
        private String initiatedBy;

        public WorkflowContextBuilder workflowId(String workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public WorkflowContextBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public WorkflowContextBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public WorkflowContextBuilder state(WorkflowState state) {
            this.state = state;
            return this;
        }

        public WorkflowContextBuilder steps(List<WorkflowStep> steps) {
            this.steps = steps;
            return this;
        }

        public WorkflowContextBuilder currentStep(WorkflowStep currentStep) {
            this.currentStep = currentStep;
            return this;
        }

        public WorkflowContextBuilder globalVariables(Map<String, Object> globalVariables) {
            this.globalVariables = globalVariables;
            return this;
        }

        public WorkflowContextBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public WorkflowContextBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public WorkflowContextBuilder startTime(Long startTime) {
            this.startTime = startTime;
            return this;
        }

        public WorkflowContextBuilder endTime(Long endTime) {
            this.endTime = endTime;
            return this;
        }

        public WorkflowContextBuilder initiatedBy(String initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }

        public WorkflowContext build() {
            WorkflowContext instance = new WorkflowContext();
            instance.setWorkflowId(this.workflowId);
            instance.setFlowId(this.flowId);
            instance.setExecutionId(this.executionId);
            instance.setState(this.state);
            instance.setSteps(this.steps != null ? this.steps : new ArrayList<>());
            instance.setCurrentStep(this.currentStep);
            instance.setGlobalVariables(this.globalVariables != null ? this.globalVariables : new HashMap<>());
            instance.setMetadata(this.metadata != null ? this.metadata : new HashMap<>());
            instance.setCorrelationId(this.correlationId);
            instance.setStartTime(this.startTime);
            instance.setEndTime(this.endTime);
            instance.setInitiatedBy(this.initiatedBy);
            return instance;
        }
    }
}
