package com.integrixs.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Entity representing a step in a saga transaction
 */
@Entity
@Table(name = "saga_steps")
public class SagaStep extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_transaction_id", nullable = false)
    private SagaTransaction sagaTransaction;

    @Column(name = "step_name", nullable = false)
    private String stepName;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StepStatus status = StepStatus.PENDING;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "action_data", columnDefinition = "TEXT")
    private String actionData;

    @Column(name = "compensation_type")
    private String compensationType;

    @Column(name = "compensation_data", columnDefinition = "TEXT")
    private String compensationData;

    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "is_compensated")
    private boolean compensated = false;

    public enum StepStatus {
        PENDING,
        STARTED,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        COMPENSATING,
        COMPENSATED,
        SKIPPED
    }

    // Getters and setters

    public SagaTransaction getSagaTransaction() {
        return sagaTransaction;
    }

    public void setSagaTransaction(SagaTransaction sagaTransaction) {
        this.sagaTransaction = sagaTransaction;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getActionData() {
        return actionData;
    }

    public void setActionData(String actionData) {
        this.actionData = actionData;
    }

    public String getCompensationType() {
        return compensationType;
    }

    public void setCompensationType(String compensationType) {
        this.compensationType = compensationType;
    }

    public String getCompensationData() {
        return compensationData;
    }

    public void setCompensationData(String compensationData) {
        this.compensationData = compensationData;
    }

    public String getResultData() {
        return resultData;
    }

    public void setResultData(String resultData) {
        this.resultData = resultData;
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

    public boolean isCompensated() {
        return compensated;
    }

    public void setCompensated(boolean compensated) {
        this.compensated = compensated;
    }

    // Additional helper methods for SagaTransactionService
    public void setTransaction(SagaTransaction transaction) {
        this.sagaTransaction = transaction;
    }

    public void setStepType(String stepType) {
        this.actionType = stepType;
    }

    public String getStepType() {
        return this.actionType;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startedAt = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.completedAt = endTime;
    }

    public void setCompensationTime(LocalDateTime compensationTime) {
        // Store in completedAt when compensated
        if(this.compensated) {
            this.completedAt = compensationTime;
        }
    }
    
    // Helper method to get parameters from actionData
    public Map<String, Object> getParameters() {
        if (actionData == null || actionData.isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(actionData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // If parsing fails, return empty map
            return new HashMap<>();
        }
    }
    
    // Helper method to set parameters
    public void setParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            this.actionData = null;
            return;
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.actionData = mapper.writeValueAsString(parameters);
        } catch (Exception e) {
            // If serialization fails, set to null
            this.actionData = null;
        }
    }
}
