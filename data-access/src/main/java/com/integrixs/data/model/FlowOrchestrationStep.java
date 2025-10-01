package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing an orchestration step in a flow
 */
public class FlowOrchestrationStep {

    private UUID id;

    private IntegrationFlow flow;

    private String stepType;

    private String stepName;

    private String description;

    private Integer executionOrder;

    private Map<String, Object> configuration;

    private String conditionExpression;

    private Boolean isConditional = false;

    private Boolean isActive = true;

    private Integer timeoutSeconds;

    private Integer retryAttempts = 0;

    private Integer retryDelaySeconds = 60;

    // For routing steps
    private UUID targetAdapterId;

    private UUID targetFlowStructureId;

    // For transformation steps
    private UUID transformationId;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

    /**
     * Step types enum
     */
    public enum StepType {
        ROUTE,         // Route to another adapter
        TRANSFORM,     // Apply transformation
        CONDITION,     // Conditional branching
        LOOP,          // Loop over collection
        AGGREGATE,     // Aggregate multiple messages
        SPLIT,         // Split message into multiple
        ENRICH,        // Enrich with additional data
        VALIDATE,      // Validate message
        LOG,           // Log message
        CUSTOM          // Custom step type
    }

    // Default constructor
    public FlowOrchestrationStep() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public Boolean getIsConditional() {
        return isConditional;
    }

    public void setIsConditional(Boolean isConditional) {
        this.isConditional = isConditional;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Integer getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(Integer retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public Integer getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(Integer retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public UUID getTargetAdapterId() {
        return targetAdapterId;
    }

    public void setTargetAdapterId(UUID targetAdapterId) {
        this.targetAdapterId = targetAdapterId;
    }

    public UUID getTargetFlowStructureId() {
        return targetFlowStructureId;
    }

    public void setTargetFlowStructureId(UUID targetFlowStructureId) {
        this.targetFlowStructureId = targetFlowStructureId;
    }

    public UUID getTransformationId() {
        return transformationId;
    }

    public void setTransformationId(UUID transformationId) {
        this.transformationId = transformationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder
    public static FlowOrchestrationStepBuilder builder() {
        return new FlowOrchestrationStepBuilder();
    }

    public static class FlowOrchestrationStepBuilder {
        private UUID id;
        private IntegrationFlow flow;
        private String stepType;
        private String stepName;
        private String description;
        private Integer executionOrder;
        private Map<String, Object> configuration;
        private String conditionExpression;
        private Boolean isConditional;
        private Boolean isActive;
        private Integer timeoutSeconds;
        private Integer retryAttempts;
        private Integer retryDelaySeconds;
        private UUID targetAdapterId;
        private UUID targetFlowStructureId;
        private UUID transformationId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public FlowOrchestrationStepBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FlowOrchestrationStepBuilder flow(IntegrationFlow flow) {
            this.flow = flow;
            return this;
        }

        public FlowOrchestrationStepBuilder stepType(String stepType) {
            this.stepType = stepType;
            return this;
        }

        public FlowOrchestrationStepBuilder stepName(String stepName) {
            this.stepName = stepName;
            return this;
        }

        public FlowOrchestrationStepBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowOrchestrationStepBuilder executionOrder(Integer executionOrder) {
            this.executionOrder = executionOrder;
            return this;
        }

        public FlowOrchestrationStepBuilder configuration(Map<String, Object> configuration) {
            this.configuration = configuration;
            return this;
        }

        public FlowOrchestrationStepBuilder conditionExpression(String conditionExpression) {
            this.conditionExpression = conditionExpression;
            return this;
        }

        public FlowOrchestrationStepBuilder isConditional(Boolean isConditional) {
            this.isConditional = isConditional;
            return this;
        }

        public FlowOrchestrationStepBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FlowOrchestrationStepBuilder timeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public FlowOrchestrationStepBuilder retryAttempts(Integer retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        public FlowOrchestrationStepBuilder retryDelaySeconds(Integer retryDelaySeconds) {
            this.retryDelaySeconds = retryDelaySeconds;
            return this;
        }

        public FlowOrchestrationStepBuilder targetAdapterId(UUID targetAdapterId) {
            this.targetAdapterId = targetAdapterId;
            return this;
        }

        public FlowOrchestrationStepBuilder targetFlowStructureId(UUID targetFlowStructureId) {
            this.targetFlowStructureId = targetFlowStructureId;
            return this;
        }

        public FlowOrchestrationStepBuilder transformationId(UUID transformationId) {
            this.transformationId = transformationId;
            return this;
        }

        public FlowOrchestrationStepBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FlowOrchestrationStepBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FlowOrchestrationStep build() {
            FlowOrchestrationStep instance = new FlowOrchestrationStep();
            instance.setId(this.id);
            instance.setFlow(this.flow);
            instance.setStepType(this.stepType);
            instance.setStepName(this.stepName);
            instance.setDescription(this.description);
            instance.setExecutionOrder(this.executionOrder);
            instance.setConfiguration(this.configuration);
            instance.setConditionExpression(this.conditionExpression);
            instance.setIsConditional(this.isConditional);
            instance.setIsActive(this.isActive);
            instance.setTimeoutSeconds(this.timeoutSeconds);
            instance.setRetryAttempts(this.retryAttempts);
            instance.setRetryDelaySeconds(this.retryDelaySeconds);
            instance.setTargetAdapterId(this.targetAdapterId);
            instance.setTargetFlowStructureId(this.targetFlowStructureId);
            instance.setTransformationId(this.transformationId);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "FlowOrchestrationStep{" +
                "id=" + id + "stepType=" + stepType + "stepName=" + stepName + "description=" + description + "executionOrder=" + executionOrder + "..." +
                '}';
    }
}
