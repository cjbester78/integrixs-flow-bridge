package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a message payload in the system
 */
public class MessagePayload extends BaseEntity {

    private String messageId;

    private IntegrationFlow flow;

    private FlowExecution flowExecution;

    private MessageStatus status = MessageStatus.RECEIVED;

    private String sourceSystem;

    private String targetSystem;

    private String messageType;

    private String contentType;

    private String messageContent;

    private String headers;

    private String properties;

    private LocalDateTime receivedAt;

    private LocalDateTime processedAt;

    private String errorMessage;

    private Integer retryCount = 0;

    private String correlationId;

    private Integer priority = 5;

    private LocalDateTime completedAt;

    public enum MessageStatus {
        PENDING,
        RECEIVED,
        QUEUED,
        PROCESSING,
        PROCESSED,
        COMPLETED,
        FAILED,
        RETRY,
        CANCELLED,
        DEAD_LETTER
    }

    // Getters and setters

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageId(UUID messageId) {
        this.messageId = messageId.toString();
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public FlowExecution getFlowExecution() {
        return flowExecution;
    }

    public void setFlowExecution(FlowExecution flowExecution) {
        this.flowExecution = flowExecution;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
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

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
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

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    // Additional helper methods
    public String getPayload() {
        return messageContent;
    }

    public void setPayload(String payload) {
        this.messageContent = payload;
    }

    public void setFlowId(UUID flowId) {
        if(this.flow == null) {
            this.flow = new IntegrationFlow();
        }
        this.flow.setId(flowId);
    }

    public void setFlowExecutionId(UUID flowExecutionId) {
        if(this.flowExecution == null) {
            this.flowExecution = new FlowExecution();
        }
        this.flowExecution.setId(flowExecutionId);
    }

    public UUID getFlowId() {
        return flow != null ? flow.getId() : null;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
