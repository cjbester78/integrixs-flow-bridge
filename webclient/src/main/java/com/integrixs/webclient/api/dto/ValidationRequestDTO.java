package com.integrixs.webclient.api.dto;


import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for message validation request
 */
public class ValidationRequestDTO {

    @NotNull(message = "Message type is required")
    private String messageType;

    @NotNull(message = "Payload is required")
    private Object payload;

    private String contentType;

    private Map<String, String> headers = new HashMap<>();

    private String schemaId;

    // Getters
    public String getMessageType() {
        return messageType;
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
    public String getSchemaId() {
        return schemaId;
    }

    // Setters
    public void setMessageType(String messageType) {
        this.messageType = messageType;
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
    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }

    // Builder
    public static ValidationRequestDTOBuilder builder() {
        return new ValidationRequestDTOBuilder();
    }

    public static class ValidationRequestDTOBuilder {
        private String messageType;
        private Object payload;
        private String contentType;
        private Map<String, String> headers;
        private String schemaId;

        public ValidationRequestDTOBuilder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public ValidationRequestDTOBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public ValidationRequestDTOBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public ValidationRequestDTOBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public ValidationRequestDTOBuilder schemaId(String schemaId) {
            this.schemaId = schemaId;
            return this;
        }

        public ValidationRequestDTO build() {
            ValidationRequestDTO result = new ValidationRequestDTO();
            result.messageType = this.messageType;
            result.payload = this.payload;
            result.contentType = this.contentType;
            result.headers = this.headers;
            result.schemaId = this.schemaId;
            return result;
        }
    }
}
