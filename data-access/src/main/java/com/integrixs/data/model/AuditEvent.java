package com.integrixs.data.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an audit event.
 * More comprehensive than AuditTrail, tracking detailed event information.
 */
public class AuditEvent {

    private UUID id;
    private String username;
    private AuditEventType eventType;
    private AuditCategory category;
    private AuditOutcome outcome;
    private String entityType;
    private String entityId;
    private String details;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
    private String action;
    private String errorMessage;
    private String correlationId;
    private String entityName;
    private UUID userId;
    private String tenantId;
    private String sessionId;
    private String requestId;
    private String apiEndpoint;
    private String httpMethod;

    /**
     * Event types
     */
    public enum AuditEventType {
        LOGIN,
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        CREATE,
        UPDATE,
        DELETE,
        DEPLOY,
        ACTIVATE,
        DEACTIVATE,
        EXECUTE,
        IMPORT,
        EXPORT,
        CONFIGURATION_CHANGE,
        CONFIG_CHANGE,
        PERMISSION_CHANGE,
        PASSWORD_CHANGE,
        FAILED_LOGIN,
        ACCESS_DENIED,
        FLOW_EXECUTED,
        FLOW_ERROR,
        SECURITY_ALERT,
        SUSPICIOUS_ACTIVITY,
        JOB_STARTED,
        JOB_COMPLETED,
        JOB_FAILED,
        SYSTEM_START,
        SYSTEM_STOP,
        MAINTENANCE_PERFORMED
    }

    /**
     * Audit categories
     */
    public enum AuditCategory {
        AUTHENTICATION,
        AUTHORIZATION,
        CONFIGURATION,
        FLOW_MANAGEMENT,
        ADAPTER_MANAGEMENT,
        USER_MANAGEMENT,
        SYSTEM_ADMINISTRATION,
        DATA_ACCESS,
        INTEGRATION_EXECUTION
    }

    /**
     * Audit outcomes
     */
    public enum AuditOutcome {
        SUCCESS,
        FAILURE,
        WARNING
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public void setEventType(AuditEventType eventType) {
        this.eventType = eventType;
    }

    public AuditCategory getCategory() {
        return category;
    }

    public void setCategory(AuditCategory category) {
        this.category = category;
    }

    public AuditOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(AuditOutcome outcome) {
        this.outcome = outcome;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public void setApiEndpoint(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    // Convenience method for AuditReportService
    public LocalDateTime getEventTimestamp() {
        return timestamp;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static Builder baseBuilder(AuditEventType eventType) {
        return new Builder().eventType(eventType);
    }

    public static Builder baseBuilder(AuditOutcome outcome) {
        return new Builder().outcome(outcome);
    }

    public static class Builder {
        private final AuditEvent event = new AuditEvent();

        public Builder username(String username) {
            event.username = username;
            return this;
        }

        public Builder eventType(AuditEventType eventType) {
            event.eventType = eventType;
            return this;
        }

        public Builder category(AuditCategory category) {
            event.category = category;
            return this;
        }

        public Builder outcome(AuditOutcome outcome) {
            event.outcome = outcome;
            return this;
        }

        public Builder entityType(String entityType) {
            event.entityType = entityType;
            return this;
        }

        public Builder entityId(String entityId) {
            event.entityId = entityId;
            return this;
        }

        public Builder details(String details) {
            event.details = details;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            event.ipAddress = ipAddress;
            return this;
        }

        public Builder userAgent(String userAgent) {
            event.userAgent = userAgent;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            event.timestamp = timestamp;
            return this;
        }

        public Builder action(String action) {
            event.action = action;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            event.errorMessage = errorMessage;
            return this;
        }

        public Builder correlationId(String correlationId) {
            event.correlationId = correlationId;
            return this;
        }

        public Builder entityName(String entityName) {
            event.entityName = entityName;
            return this;
        }

        public Builder userId(UUID userId) {
            event.userId = userId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            event.tenantId = tenantId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            event.sessionId = sessionId;
            return this;
        }

        public Builder requestId(String requestId) {
            event.requestId = requestId;
            return this;
        }

        public Builder apiEndpoint(String apiEndpoint) {
            event.apiEndpoint = apiEndpoint;
            return this;
        }

        public Builder httpMethod(String httpMethod) {
            event.httpMethod = httpMethod;
            return this;
        }

        public AuditEvent build() {
            if (event.id == null) {
                event.id = UUID.randomUUID();
            }
            if (event.timestamp == null) {
                event.timestamp = LocalDateTime.now();
            }
            if (event.createdAt == null) {
                event.createdAt = LocalDateTime.now();
            }
            return event;
        }
    }
}