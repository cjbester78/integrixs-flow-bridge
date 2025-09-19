package com.integrixs.webclient.api.dto;


import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for message details
 */
public class MessageDetailsDTO {

    private String messageId;
    private String messageType;
    private String source;
    private String adapterId;
    private Object payload;
    private String contentType;
    private Map<String, String> headers;
    private Map<String, String> metadata;
    private String status;
    private String correlationId;
    private String flowId;
    private LocalDateTime receivedAt;

    // Getters
    public String getMessageId() {
        return messageId;
    }
    public String getMessageType() {
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
    public String getStatus() {
        return status;
    }
    public String getCorrelationId() {
        return correlationId;
    }
    public String getFlowId() {
        return flowId;
    }
    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    // Setters
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
    public void setMessageType(String messageType) {
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
    public void setStatus(String status) {
        this.status = status;
    }
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }
    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    // Builder
    public static MessageDetailsDTOBuilder builder() {
        return new MessageDetailsDTOBuilder();
    }

    public static class MessageDetailsDTOBuilder {
        private String messageId;
        private String messageType;
        private String source;
        private String adapterId;
        private Object payload;
        private String contentType;
        private Map<String, String> headers;
        private Map<String, String> metadata;
        private String status;
        private String correlationId;
        private String flowId;
        private LocalDateTime receivedAt;

        public MessageDetailsDTOBuilder messageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public MessageDetailsDTOBuilder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public MessageDetailsDTOBuilder source(String source) {
            this.source = source;
            return this;
        }

        public MessageDetailsDTOBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public MessageDetailsDTOBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public MessageDetailsDTOBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public MessageDetailsDTOBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public MessageDetailsDTOBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public MessageDetailsDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public MessageDetailsDTOBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public MessageDetailsDTOBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public MessageDetailsDTOBuilder receivedAt(LocalDateTime receivedAt) {
            this.receivedAt = receivedAt;
            return this;
        }

        public MessageDetailsDTO build() {
            MessageDetailsDTO result = new MessageDetailsDTO();
            result.messageId = this.messageId;
            result.messageType = this.messageType;
            result.source = this.source;
            result.adapterId = this.adapterId;
            result.payload = this.payload;
            result.contentType = this.contentType;
            result.headers = this.headers;
            result.metadata = this.metadata;
            result.status = this.status;
            result.correlationId = this.correlationId;
            result.flowId = this.flowId;
            result.receivedAt = this.receivedAt;
            return result;
        }
    }
}
