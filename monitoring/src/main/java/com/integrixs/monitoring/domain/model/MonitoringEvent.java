package com.integrixs.monitoring.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing a monitoring event
 */
public class MonitoringEvent {
    private String eventId;
    private EventType eventType;
    private EventLevel level;
    private String source;
    private String message;
    private LocalDateTime timestamp;
    private String userId;
    private String correlationId;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata = new HashMap<>();
    private String stackTrace;

    // Constructors
    public MonitoringEvent() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MonitoringEvent event = new MonitoringEvent();

        public Builder eventId(String eventId) {
            event.eventId = eventId;
            return this;
        }

        public Builder eventType(EventType eventType) {
            event.eventType = eventType;
            return this;
        }

        public Builder level(EventLevel level) {
            event.level = level;
            return this;
        }

        public Builder source(String source) {
            event.source = source;
            return this;
        }

        public Builder message(String message) {
            event.message = message;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            event.timestamp = timestamp;
            return this;
        }

        public Builder userId(String userId) {
            event.userId = userId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            event.correlationId = correlationId;
            return this;
        }

        public Builder domainType(String domainType) {
            event.domainType = domainType;
            return this;
        }

        public Builder domainReferenceId(String domainReferenceId) {
            event.domainReferenceId = domainReferenceId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            event.metadata = metadata != null ? metadata : new HashMap<>();
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            event.stackTrace = stackTrace;
            return this;
        }

        public MonitoringEvent build() {
            return event;
        }
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public EventLevel getLevel() {
        return level;
    }

    public void setLevel(EventLevel level) {
        this.level = level;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getDomainType() {
        return domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    /**
     * Event types
     */
    public enum EventType {
        SYSTEM_LOG,
        FLOW_EXECUTION,
        ADAPTER_OPERATION,
        USER_ACTIVITY,
        ERROR,
        PERFORMANCE,
        SECURITY,
        AUDIT
    }

    /**
     * Event levels
     */
    public enum EventLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }


    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Check if event is error level or higher
     */
    public boolean isError() {
        return level == EventLevel.ERROR || level == EventLevel.CRITICAL;
    }

    /**
     * Check if event requires immediate attention
     */
    public boolean isCritical() {
        return level == EventLevel.CRITICAL;
    }
}
