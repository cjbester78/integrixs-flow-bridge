package com.integrixs.engine.api.dto;

import com.integrixs.engine.domain.model.WorkflowStep;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for workflow execution responses
 */
public class WorkflowExecutionResponseDTO {
    private String workflowId;
    private String flowId;
    private String executionId;
    private String state;
    private boolean success;
    private List<WorkflowStep> steps = new ArrayList<>();
    private WorkflowStep currentStep;
    private Object outputData;
    private String errorMessage;
    private Map<String, Object> globalVariables = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();
    private String correlationId;
    private Long startTime;
    private Long endTime;
    private Long executionTimeMs;
    private String initiatedBy;

    // Default constructor
    public WorkflowExecutionResponseDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private WorkflowExecutionResponseDTO dto = new WorkflowExecutionResponseDTO();

        public Builder workflowId(String workflowId) {
            dto.workflowId = workflowId;
            return this;
        }

        public Builder flowId(String flowId) {
            dto.flowId = flowId;
            return this;
        }

        public Builder executionId(String executionId) {
            dto.executionId = executionId;
            return this;
        }

        public Builder state(String state) {
            dto.state = state;
            return this;
        }

        public Builder success(boolean success) {
            dto.success = success;
            return this;
        }

        public Builder steps(List<WorkflowStep> steps) {
            dto.steps = steps != null ? steps : new ArrayList<>();
            return this;
        }

        public Builder currentStep(WorkflowStep currentStep) {
            dto.currentStep = currentStep;
            return this;
        }

        public Builder outputData(Object outputData) {
            dto.outputData = outputData;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            dto.errorMessage = errorMessage;
            return this;
        }

        public Builder globalVariables(Map<String, Object> globalVariables) {
            dto.globalVariables = globalVariables != null ? globalVariables : new HashMap<>();
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            dto.metadata = metadata != null ? metadata : new HashMap<>();
            return this;
        }

        public Builder correlationId(String correlationId) {
            dto.correlationId = correlationId;
            return this;
        }

        public Builder startTime(Long startTime) {
            dto.startTime = startTime;
            return this;
        }

        public Builder endTime(Long endTime) {
            dto.endTime = endTime;
            return this;
        }

        public Builder executionTimeMs(Long executionTimeMs) {
            dto.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder initiatedBy(String initiatedBy) {
            dto.initiatedBy = initiatedBy;
            return this;
        }

        public WorkflowExecutionResponseDTO build() {
            return dto;
        }
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }
}
