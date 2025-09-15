package com.integrixs.backend.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Common message format for Integrix messaging
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IntegrixMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String correlationId;
    private String flowId;
    private String adapterId;
    private String messageType;
    private String contentType;
    private Object payload;
    private Map<String, String> headers;
    private Map<String, Object> metadata;
    private LocalDateTime timestamp;
    private String source;
    private String target;
    private Integer priority;
    private Long ttl;

    public IntegrixMessage() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.headers = new HashMap<>();
        this.metadata = new HashMap<>();
        this.contentType = "application/json";
        this.priority = 5;
    }

    public IntegrixMessage(Object payload) {
        this();
        this.payload = payload;
    }

    // Builder pattern for fluent API
    public static class Builder {
        private IntegrixMessage message = new IntegrixMessage();

        public Builder withId(String id) {
            message.id = id;
            return this;
        }

        public Builder withCorrelationId(String correlationId) {
            message.correlationId = correlationId;
            return this;
        }

        public Builder withFlowId(String flowId) {
            message.flowId = flowId;
            return this;
        }

        public Builder withAdapterId(String adapterId) {
            message.adapterId = adapterId;
            return this;
        }

        public Builder withMessageType(String messageType) {
            message.messageType = messageType;
            return this;
        }

        public Builder withContentType(String contentType) {
            message.contentType = contentType;
            return this;
        }

        public Builder withPayload(Object payload) {
            message.payload = payload;
            return this;
        }

        public Builder withHeader(String key, String value) {
            message.headers.put(key, value);
            return this;
        }

        public Builder withHeaders(Map<String, String> headers) {
            message.headers.putAll(headers);
            return this;
        }

        public Builder withMetadata(String key, Object value) {
            message.metadata.put(key, value);
            return this;
        }

        public Builder withMetadata(Map<String, Object> metadata) {
            message.metadata.putAll(metadata);
            return this;
        }

        public Builder withSource(String source) {
            message.source = source;
            return this;
        }

        public Builder withTarget(String target) {
            message.target = target;
            return this;
        }

        public Builder withPriority(Integer priority) {
            message.priority = priority;
            return this;
        }

        public Builder withTtl(Long ttl) {
            message.ttl = ttl;
            return this;
        }

        public IntegrixMessage build() {
            return message;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
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

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    @Override
    public String toString() {
        return "IntegrixMessage {" +
            "id = '" + id + '\'' +
            ", correlationId = '" + correlationId + '\'' +
            ", flowId = '" + flowId + '\'' +
            ", messageType = '" + messageType + '\'' +
            ", source = '" + source + '\'' +
            ", target = '" + target + '\'' +
            ", timestamp = " + timestamp +
            '}';
    }
}
