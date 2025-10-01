package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a saga transaction for distributed transactions
 */
public class SagaTransaction extends BaseEntity {

    private String sagaId;

    private IntegrationFlow flow;

    private String transactionType;

    private SagaStatus status = SagaStatus.STARTED;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Integer currentStep = 0;

    private String correlationId;

    private String errorMessage;

    private Integer retryCount = 0;

    private List<SagaStep> steps = new ArrayList<>();

    private String contextData;

    public enum SagaStatus {
        STARTED,
        IN_PROGRESS,
        COMPENSATING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    // Getters and setters

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setFlowId(UUID flowId) {
        if(this.flow == null) {
            this.flow = new IntegrationFlow();
        }
        this.flow.setId(flowId);
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startedAt = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.completedAt = endTime;
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

    public List<SagaStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SagaStep> steps) {
        this.steps = steps;
    }

    public String getContextData() {
        return contextData;
    }

    public void setContextData(String contextData) {
        this.contextData = contextData;
    }
}
