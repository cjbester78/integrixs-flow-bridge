package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a dead letter message
 */
public class DeadLetterMessage extends BaseEntity {

    private Message originalMessage;

    private IntegrationFlow flow;

    private String messageId;

    private String messageContent;

    private String headers;

    private String properties;

    private String errorReason;

    private String errorDetails;

    private ErrorRecord.ErrorType lastErrorType;

    private Integer retryCount = 0;

    private LocalDateTime queuedAt;

    private LocalDateTime lastRetryAt;

    private boolean reprocessed = false;

    private LocalDateTime reprocessedAt;

    private String reprocessedBy;

    private String reprocessResult;

    private String correlationId;

    private String sourceSystem;

    private String targetSystem;

    private String retryMessageId;

    private LocalDateTime failedAt;

    // Getters and setters

    public Message getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(Message originalMessage) {
        this.originalMessage = originalMessage;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public ErrorRecord.ErrorType getLastErrorType() {
        return lastErrorType;
    }

    public void setLastErrorType(ErrorRecord.ErrorType lastErrorType) {
        this.lastErrorType = lastErrorType;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(LocalDateTime queuedAt) {
        this.queuedAt = queuedAt;
    }

    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }

    public void setLastRetryAt(LocalDateTime lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }

    public boolean isReprocessed() {
        return reprocessed;
    }

    public void setReprocessed(boolean reprocessed) {
        this.reprocessed = reprocessed;
    }

    public LocalDateTime getReprocessedAt() {
        return reprocessedAt;
    }

    public void setReprocessedAt(LocalDateTime reprocessedAt) {
        this.reprocessedAt = reprocessedAt;
    }

    public String getReprocessedBy() {
        return reprocessedBy;
    }

    public void setReprocessedBy(String reprocessedBy) {
        this.reprocessedBy = reprocessedBy;
    }

    public String getReprocessResult() {
        return reprocessResult;
    }

    public void setReprocessResult(String reprocessResult) {
        this.reprocessResult = reprocessResult;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getTargetSystem() {
        return targetSystem;
    }

    public void setTargetSystem(String targetSystem) {
        this.targetSystem = targetSystem;
    }

    // Additional helper methods
    public String getPayload() {
        return messageContent;
    }

    public void setPayload(String payload) {
        this.messageContent = payload;
    }

    public UUID getFlowId() {
        return flow != null ? flow.getId() : null;
    }

    public void setFlowId(UUID flowId) {
        if(this.flow == null) {
            this.flow = new IntegrationFlow();
        }
        this.flow.setId(flowId);
    }

    public String getReason() {
        return errorReason;
    }

    public void setReason(String reason) {
        this.errorReason = reason;
    }

    public enum Status {
        PENDING,
        QUEUED,
        PROCESSING,
        REPROCESSED,
        RETRIED,
        FAILED,
        EXPIRED,
        RESOLVED
    }

    private Status status = Status.QUEUED;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Additional methods for DeadLetterQueueService compatibility
    public void setErrorMessage(String errorMessage) {
        this.errorDetails = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorDetails;
    }

    public void setErrorStackTrace(String errorStackTrace) {
        // Store stack trace as part of error details
        if (this.errorDetails == null || this.errorDetails.isEmpty()) {
            this.errorDetails = errorStackTrace;
        } else {
            this.errorDetails = this.errorDetails + "\n\nStack Trace:\n" + errorStackTrace;
        }
    }

    public String getErrorStackTrace() {
        // Extract stack trace from error details if present
        if (this.errorDetails != null && this.errorDetails.contains("Stack Trace:")) {
            int index = this.errorDetails.indexOf("Stack Trace:");
            return this.errorDetails.substring(index);
        }
        return null;
    }

    public void setOriginalReceivedAt(LocalDateTime originalReceivedAt) {
        // Store as part of queuedAt if not already set
        if (this.queuedAt == null) {
            this.queuedAt = originalReceivedAt;
        }
    }

    public LocalDateTime getOriginalReceivedAt() {
        return this.queuedAt;
    }

    public void setErrorType(ErrorRecord.ErrorType errorType) {
        this.lastErrorType = errorType;
    }

    public ErrorRecord.ErrorType getErrorType() {
        return this.lastErrorType;
    }

    public String getRetryMessageId() {
        return retryMessageId;
    }

    public void setRetryMessageId(String retryMessageId) {
        this.retryMessageId = retryMessageId;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
}
