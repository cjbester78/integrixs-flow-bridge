package com.integrixs.data.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a dead letter message
 */
@Entity
@Table(name = "dead_letter_messages")
public class DeadLetterMessage extends BaseEntity {
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_message_id")
    private Message originalMessage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id")
    private IntegrationFlow flow;
    
    @Column(name = "message_id", nullable = false)
    private String messageId;
    
    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;
    
    @Column(name = "headers", columnDefinition = "TEXT")
    private String headers;
    
    @Column(name = "properties", columnDefinition = "TEXT")
    private String properties;
    
    @Column(name = "error_reason", columnDefinition = "TEXT", nullable = false)
    private String errorReason;
    
    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;
    
    @Column(name = "last_error_type")
    @Enumerated(EnumType.STRING)
    private ErrorRecord.ErrorType lastErrorType;
    
    @Column(name = "retry_count")
    private Integer retryCount = 0;
    
    @Column(name = "queued_at", nullable = false)
    private LocalDateTime queuedAt;
    
    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;
    
    @Column(name = "is_reprocessed")
    private boolean reprocessed = false;
    
    @Column(name = "reprocessed_at")
    private LocalDateTime reprocessedAt;
    
    @Column(name = "reprocessed_by")
    private String reprocessedBy;
    
    @Column(name = "reprocess_result")
    private String reprocessResult;
    
    @Column(name = "correlation_id")
    private String correlationId;
    
    @Column(name = "source_system")
    private String sourceSystem;
    
    @Column(name = "target_system")
    private String targetSystem;
    
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
        if (this.flow == null) {
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
        EXPIRED
    }
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.QUEUED;
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
}