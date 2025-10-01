package com.integrixs.webclient.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing an inbound message
 */
public class InboundMessage {
    private String messageId;
    private MessageType messageType;
    private String source;
    private String adapterId;
    private Object payload;
    private String contentType;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> metadata = new HashMap<>();
    private LocalDateTime receivedAt;
    private MessageStatus status;
    private String correlationId;
    private String flowId;

    /**
     * Message types
     */
    public enum MessageType {
        HTTP_REQUEST,
        SOAP_REQUEST,
        WEBHOOK,
        API_CALL,
        FILE_UPLOAD,
        STREAM
    }

    /**
     * Message status
     */
    public enum MessageStatus {
        RECEIVED,
        VALIDATED,
        PROCESSING,
        PROCESSED,
        FAILED,
        REJECTED
    }

    // Getters
    public String getMessageId() {
        return messageId;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSource() {
        return source;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public Object getPayload() {
        return payload;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getFlowId() {
        return flowId;
    }

    // Setters
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    /**
     * Builder pattern implementation
     */
    public static InboundMessageBuilder builder() {
        return new InboundMessageBuilder();
    }

    public static class InboundMessageBuilder {
        private String messageId;
        private MessageType messageType;
        private String source;
        private String adapterId;
        private Object payload;
        private String contentType;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> metadata = new HashMap<>();
        private LocalDateTime receivedAt;
        private MessageStatus status;
        private String correlationId;
        private String flowId;

        public InboundMessageBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public InboundMessageBuilder messageType(MessageType messageType) {
            this.messageType = messageType;
            return this;
        }

        public InboundMessageBuilder source(String source) {
            this.source = source;
            return this;
        }

        public InboundMessageBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public InboundMessageBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public InboundMessageBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public InboundMessageBuilder headers(Map<String, String> headers) {
            this.headers = headers != null ? headers : new HashMap<>();
            return this;
        }

        public InboundMessageBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata != null ? metadata : new HashMap<>();
            return this;
        }

        public InboundMessageBuilder receivedAt(LocalDateTime receivedAt) {
            this.receivedAt = receivedAt;
            return this;
        }

        public InboundMessageBuilder status(MessageStatus status) {
            this.status = status;
            return this;
        }

        public InboundMessageBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public InboundMessageBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public InboundMessage build() {
            InboundMessage message = new InboundMessage();
            message.setMessageId(messageId != null ? messageId : UUID.randomUUID().toString());
            message.setMessageType(messageType);
            message.setSource(source);
            message.setAdapterId(adapterId);
            message.setPayload(payload);
            message.setContentType(contentType);
            message.setHeaders(headers);
            message.setMetadata(metadata);
            message.setReceivedAt(receivedAt != null ? receivedAt : LocalDateTime.now());
            message.setStatus(status != null ? status : MessageStatus.RECEIVED);
            message.setCorrelationId(correlationId);
            message.setFlowId(flowId);
            return message;
        }
    }

    /**
     * Add header
     * @param key Header key
     * @param value Header value
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Add metadata
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    /**
     * Check if message is processed
     * @return true if processed
     */
    public boolean isProcessed() {
        return status == MessageStatus.PROCESSED;
    }

    /**
     * Check if message failed
     * @return true if failed
     */
    public boolean isFailed() {
        return status == MessageStatus.FAILED || status == MessageStatus.REJECTED;
    }

    /**
     * Get payload as specific type
     * @param type Target type
     * @return Payload cast to type
     */
    public <T> T getPayloadAs(Class<T> type) {
        return type.cast(payload);
    }
}
