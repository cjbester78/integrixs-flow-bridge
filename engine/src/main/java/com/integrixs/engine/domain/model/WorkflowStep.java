package com.integrixs.engine.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model representing a single step in a workflow
 */
public class WorkflowStep {
    private String stepId;
    private String stepName;
    private StepType stepType;
    private StepStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Object inputData;
    private Object outputData;
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

    // Default constructor
    public WorkflowStep() {
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public StepType getStepType() {
        return stepType;
    }

    public void setStepType(StepType stepType) {
        this.stepType = stepType;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Object getInputData() {
        return inputData;
    }

    public void setInputData(Object inputData) {
        this.inputData = inputData;
    }

    public Object getOutputData() {
        return outputData;
    }

    public void setOutputData(Object outputData) {
        this.outputData = outputData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public String getNextStepId() {
        return nextStepId;
    }

    public void setNextStepId(String nextStepId) {
        this.nextStepId = nextStepId;
    }

    public Map<String, Object> getStepVariables() {
        return stepVariables;
    }

    public void setStepVariables(Map<String, Object> stepVariables) {
        this.stepVariables = stepVariables;
    }

    // Builder
    public static WorkflowStepBuilder builder() {
        return new WorkflowStepBuilder();
    }

    public static class WorkflowStepBuilder {
        private String stepId;
        private String stepName;
        private StepType stepType;
        private String adapterId;
        private String adapterType;
        private Map<String, Object> parameters;
        private StepStatus status;
        private Long startTime;
        private Long endTime;
        private String errorMessage;
        private String errorCode;
        private Integer retryCount;
        private Map<String, Object> metadata;
        private Map<String, Object> inputData;
        private Map<String, Object> outputData;
        private List<String> errors;
        private Map<String, Object> stepVariables;

        public WorkflowStepBuilder stepId(String stepId) {
            this.stepId = stepId;
            return this;
        }

        public WorkflowStepBuilder stepName(String stepName) {
            this.stepName = stepName;
            return this;
        }

        public WorkflowStepBuilder stepType(StepType stepType) {
            this.stepType = stepType;
            return this;
        }

        public WorkflowStepBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public WorkflowStepBuilder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public WorkflowStepBuilder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public WorkflowStepBuilder status(StepStatus status) {
            this.status = status;
            return this;
        }

        public WorkflowStepBuilder startTime(Long startTime) {
            this.startTime = startTime;
            return this;
        }

        public WorkflowStepBuilder endTime(Long endTime) {
            this.endTime = endTime;
            return this;
        }

        public WorkflowStepBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public WorkflowStepBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public WorkflowStepBuilder retryCount(Integer retryCount) {
            this.retryCount = retryCount;
            return this;
        }

        public WorkflowStepBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public WorkflowStepBuilder inputData(Map<String, Object> inputData) {
            this.inputData = inputData;
            return this;
        }

        public WorkflowStepBuilder outputData(Map<String, Object> outputData) {
            this.outputData = outputData;
            return this;
        }

        public WorkflowStepBuilder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public WorkflowStepBuilder stepVariables(Map<String, Object> stepVariables) {
            this.stepVariables = stepVariables;
            return this;
        }

        public WorkflowStep build() {
            WorkflowStep instance = new WorkflowStep();
            instance.setStepId(this.stepId);
            instance.setStepName(this.stepName);
            instance.setStepType(this.stepType);
            instance.setStatus(this.status != null ? this.status : StepStatus.PENDING);
            instance.setStartTime(this.startTime != null ? LocalDateTime.ofEpochSecond(this.startTime, 0, java.time.ZoneOffset.UTC) : null);
            instance.setEndTime(this.endTime != null ? LocalDateTime.ofEpochSecond(this.endTime, 0, java.time.ZoneOffset.UTC) : null);
            instance.setErrorMessage(this.errorMessage);
            instance.setRetryCount(this.retryCount != null ? this.retryCount : 0);
            instance.setInputData(this.inputData);
            instance.setOutputData(this.outputData);
            instance.setStepVariables(this.stepVariables != null ? this.stepVariables : new HashMap<>());
            return instance;
        }
    }
}
