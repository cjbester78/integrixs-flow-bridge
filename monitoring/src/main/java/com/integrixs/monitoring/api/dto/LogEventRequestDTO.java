package com.integrixs.monitoring.api.dto;


import java.util.Map;

/**
 * DTO for log event request
 */
public class LogEventRequestDTO {
    private String eventType;
    private String level;
    private String source;
    private String message;
    private String userId;
    private String correlationId;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata;
    private String stackTrace;


    // Getters
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
