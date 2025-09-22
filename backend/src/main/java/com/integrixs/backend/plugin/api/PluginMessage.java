package com.integrixs.backend.plugin.api;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Message structure for plugin communication
 */
public class PluginMessage {

    /**
     * Unique message ID
     */
    private String id = UUID.randomUUID().toString();

    /**
     * Message headers/metadata
     */
    private Map<String, String> headers;

    /**
     * Message body/payload
     */
    private Object body;

    /**
     * Content type of the body
     */
    private String contentType;

    /**
     * Encoding of the body(for text content)
     */
    private String encoding = "UTF-8";

    /**
     * Message timestamp
     */
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Correlation ID for request/response matching
     */
    private String correlationId;

    /**
     * Reply - to address(if applicable)
     */
    private String replyTo;

    /**
     * Message priority
     */
    private Priority priority = Priority.NORMAL;

    /**
     * Time - to - live in milliseconds
     */
    private Long ttl;

    /**
     * Custom properties
     */
    private Map<String, Object> properties;

    /**
     * Message priority levels
     */
    public enum Priority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    // Builder pattern
    public static PluginMessageBuilder builder() {
        return new PluginMessageBuilder();
    }

    public static class PluginMessageBuilder {
        private String id = UUID.randomUUID().toString();
        private Map<String, String> headers;
        private Object body;
        private String contentType;
        private String encoding = "UTF-8";
        private LocalDateTime timestamp = LocalDateTime.now();
        private String correlationId;
        private String replyTo;
        private Priority priority = Priority.NORMAL;
        private Long ttl;
        private Map<String, Object> properties;

        public PluginMessageBuilder id(String id) {
            this.id = id;
            return this;
        }

        public PluginMessageBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public PluginMessageBuilder body(Object body) {
            this.body = body;
            return this;
        }

        public PluginMessageBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public PluginMessageBuilder encoding(String encoding) {
            this.encoding = encoding;
            return this;
        }

        public PluginMessageBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public PluginMessageBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public PluginMessageBuilder replyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public PluginMessageBuilder priority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public PluginMessageBuilder ttl(Long ttl) {
            this.ttl = ttl;
            return this;
        }

        public PluginMessageBuilder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public PluginMessage build() {
            PluginMessage message = new PluginMessage();
            message.id = this.id;
            message.headers = this.headers;
            message.body = this.body;
            message.contentType = this.contentType;
            message.encoding = this.encoding;
            message.timestamp = this.timestamp;
            message.correlationId = this.correlationId;
            message.replyTo = this.replyTo;
            message.priority = this.priority;
            message.ttl = this.ttl;
            message.properties = this.properties;
            return message;
        }
    }
}
