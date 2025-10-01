package com.integrixs.shared.dto.system;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * DTO for system log entries.
 *
 * <p>Represents audit and monitoring log entries for tracking
 * system activities, errors, and user actions.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class SystemLogDTO {

    private String id;
    private LocalDateTime timestamp;
    private String level;
    private String message;
    private String details;
    private String source;
    private String sourceId;
    private String sourceName;
    private String component;
    private String componentId;
    private String domainType;
    private String domainReferenceId;
    private String userId;
    private LocalDateTime createdAt;
    private Map<String, Object> context;
    private String correlationId;
    private String clientIp;

    // Default constructor
    public SystemLogDTO() {
        this.context = new HashMap<>();
    }

    // All args constructor
    public SystemLogDTO(String id, LocalDateTime timestamp, String level, String message, String details, String source, String sourceId, String sourceName, String component, String componentId, String domainType, String domainReferenceId, String userId, LocalDateTime createdAt, Map<String, Object> context, String correlationId, String clientIp) {
        this.id = id;
        this.timestamp = timestamp;
        this.level = level;
        this.message = message;
        this.details = details;
        this.source = source;
        this.sourceId = sourceId;
        this.sourceName = sourceName;
        this.component = component;
        this.componentId = componentId;
        this.domainType = domainType;
        this.domainReferenceId = domainReferenceId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.context = context != null ? context : new HashMap<>();
        this.correlationId = correlationId;
        this.clientIp = clientIp;
    }

    // Getters
    public String getId() { return id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getLevel() { return level; }
    public String getMessage() { return message; }
    public String getDetails() { return details; }
    public String getSource() { return source; }
    public String getSourceId() { return sourceId; }
    public String getSourceName() { return sourceName; }
    public String getComponent() { return component; }
    public String getComponentId() { return componentId; }
    public String getDomainType() { return domainType; }
    public String getDomainReferenceId() { return domainReferenceId; }
    public String getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Map<String, Object> getContext() { return context; }
    public String getCorrelationId() { return correlationId; }
    public String getClientIp() { return clientIp; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setLevel(String level) { this.level = level; }
    public void setMessage(String message) { this.message = message; }
    public void setDetails(String details) { this.details = details; }
    public void setSource(String source) { this.source = source; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public void setComponent(String component) { this.component = component; }
    public void setComponentId(String componentId) { this.componentId = componentId; }
    public void setDomainType(String domainType) { this.domainType = domainType; }
    public void setDomainReferenceId(String domainReferenceId) { this.domainReferenceId = domainReferenceId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setContext(Map<String, Object> context) { this.context = context; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }

    // Builder
    public static SystemLogDTOBuilder builder() {
        return new SystemLogDTOBuilder();
    }

    public static class SystemLogDTOBuilder {
        private String id;
        private LocalDateTime timestamp;
        private String level;
        private String message;
        private String details;
        private String source;
        private String sourceId;
        private String sourceName;
        private String component;
        private String componentId;
        private String domainType;
        private String domainReferenceId;
        private String userId;
        private LocalDateTime createdAt;
        private Map<String, Object> context = new HashMap<>();
        private String correlationId;
        private String clientIp;

        public SystemLogDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public SystemLogDTOBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public SystemLogDTOBuilder level(String level) {
            this.level = level;
            return this;
        }

        public SystemLogDTOBuilder message(String message) {
            this.message = message;
            return this;
        }

        public SystemLogDTOBuilder details(String details) {
            this.details = details;
            return this;
        }

        public SystemLogDTOBuilder source(String source) {
            this.source = source;
            return this;
        }

        public SystemLogDTOBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public SystemLogDTOBuilder sourceName(String sourceName) {
            this.sourceName = sourceName;
            return this;
        }

        public SystemLogDTOBuilder component(String component) {
            this.component = component;
            return this;
        }

        public SystemLogDTOBuilder componentId(String componentId) {
            this.componentId = componentId;
            return this;
        }

        public SystemLogDTOBuilder domainType(String domainType) {
            this.domainType = domainType;
            return this;
        }

        public SystemLogDTOBuilder domainReferenceId(String domainReferenceId) {
            this.domainReferenceId = domainReferenceId;
            return this;
        }

        public SystemLogDTOBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public SystemLogDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public SystemLogDTOBuilder context(Map<String, Object> context) {
            this.context = context;
            return this;
        }

        public SystemLogDTOBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public SystemLogDTOBuilder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public SystemLogDTO build() {
            return new SystemLogDTO(id, timestamp, level, message, details, source, sourceId, sourceName, component, componentId, domainType, domainReferenceId, userId, createdAt, context, correlationId, clientIp);
        }
    }
}
