package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a flow execution instance
 */
public class FlowExecution extends BaseEntity {

    private String executionId;

    private IntegrationFlow flow;

    private ExecutionStatus status = ExecutionStatus.STARTED;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private TriggerType triggerType;

    private String triggeredBy;

    private String executionContext;

    private String errorMessage;

    private String errorDetails;

    private Integer messagesProcessed = 0;

    private Integer messagesFailed = 0;

    private Long executionTimeMs;

    private String currentStep;

    private Integer retryCount = 0;

    public enum ExecutionStatus {
        STARTED,
        QUEUED,
        RUNNING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED,
        RETRY
    }

    public enum TriggerType {
        MANUAL,
        SCHEDULED,
        EVENT,
        API,
        WEBHOOK,
        FILE_WATCHER,
        DATABASE_TRIGGER
    }

    // Getters and setters

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
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

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(String executionContext) {
        this.executionContext = executionContext;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public Integer getMessagesProcessed() {
        return messagesProcessed;
    }

    public void setMessagesProcessed(Integer messagesProcessed) {
        this.messagesProcessed = messagesProcessed;
    }

    public Integer getMessagesFailed() {
        return messagesFailed;
    }

    public void setMessagesFailed(Integer messagesFailed) {
        this.messagesFailed = messagesFailed;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    // Additional helper methods
    public void setMessageId(UUID messageId) {
        // Flow executions don't have message ID directly
        // This might be used for correlation
        if(this.executionContext == null) {
            this.executionContext = " {\"messageId\":\"" + messageId.toString() + "\"}";
        }
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startedAt = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.completedAt = endTime;
    }
}
