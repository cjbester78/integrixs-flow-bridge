package com.integrixs.webclient.api.dto;


import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for inbound message request
 */
public class InboundMessageRequestDTO {

    @NotNull(message = "Message type is required")
    private String messageType;

    @NotNull(message = "Source is required")
    private String source;

    @NotNull(message = "Adapter ID is required")
    private String adapterId;

    @NotNull(message = "Payload is required")
    private Object payload;

    private String contentType;

    private Map<String, String> headers = new HashMap<>();

    private Map<String, String> metadata = new HashMap<>();

    private String correlationId;

    // Getters
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
    public String getCorrelationId() {
        return correlationId;
    }

    // Setters
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
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    // Builder
    public static InboundMessageRequestDTOBuilder builder() {
        return new InboundMessageRequestDTOBuilder();
    }

    public static class InboundMessageRequestDTOBuilder {
        private String messageType;
        private String source;
        private String adapterId;
        private Object payload;
        private String contentType;
        private Map<String, String> headers;
        private Map<String, String> metadata;
        private String correlationId;

        public InboundMessageRequestDTOBuilder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public InboundMessageRequestDTOBuilder source(String source) {
            this.source = source;
            return this;
        }

        public InboundMessageRequestDTOBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public InboundMessageRequestDTOBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public InboundMessageRequestDTOBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public InboundMessageRequestDTOBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public InboundMessageRequestDTOBuilder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public InboundMessageRequestDTOBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public InboundMessageRequestDTO build() {
            InboundMessageRequestDTO result = new InboundMessageRequestDTO();
            result.messageType = this.messageType;
            result.source = this.source;
            result.adapterId = this.adapterId;
            result.payload = this.payload;
            result.contentType = this.contentType;
            result.headers = this.headers;
            result.metadata = this.metadata;
            result.correlationId = this.correlationId;
            return result;
        }
    }
}
