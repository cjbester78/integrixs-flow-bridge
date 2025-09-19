package com.integrixs.monitoring.api.dto;


import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for monitoring event
 */
public class MonitoringEventDTO {
    private String eventId;
    private String eventType;
    private String level;
    private String source;
    private String message;
    private LocalDateTime timestamp;
    private String userId;
    private String correlationId;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata;
    private String stackTrace;

    // Constructors
    public MonitoringEventDTO() {
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MonitoringEventDTO dto = new MonitoringEventDTO();

        public Builder eventId(String eventId) {
            dto.eventId = eventId;
            return this;
        }

        public Builder eventType(String eventType) {
            dto.eventType = eventType;
            return this;
        }

        public Builder level(String level) {
            dto.level = level;
            return this;
        }

        public Builder source(String source) {
            dto.source = source;
            return this;
        }

        public Builder message(String message) {
            dto.message = message;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            dto.timestamp = timestamp;
            return this;
        }

        public Builder userId(String userId) {
            dto.userId = userId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            dto.correlationId = correlationId;
            return this;
        }

        public Builder domainType(String domainType) {
            dto.domainType = domainType;
            return this;
        }

        public Builder domainReferenceId(String domainReferenceId) {
            dto.domainReferenceId = domainReferenceId;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            dto.metadata = metadata;
            return this;
        }

        public Builder stackTrace(String stackTrace) {
            dto.stackTrace = stackTrace;
            return this;
        }

        public MonitoringEventDTO build() {
            return dto;
        }
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getLevel() {
        return level;
    }

    public String getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getDomainType() {
        return domainType;
    }

    public String getDomainReferenceId() {
        return domainReferenceId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    // Setters
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public void setDomainReferenceId(String domainReferenceId) {
        this.domainReferenceId = domainReferenceId;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

}
