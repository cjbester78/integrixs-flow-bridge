package com.integrixs.backend.audit;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing an audit event
 */
@Entity
@Table(name = "audit_events", indexes = {
    @Index(name = "idx_audit_timestamp", columnList = "event_timestamp"),
    @Index(name = "idx_audit_user", columnList = "username"),
    @Index(name = "idx_audit_type", columnList = "event_type"),
    @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_tenant", columnList = "tenant_id")
})
public class AuditEvent {
    
    public static AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "event_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditEventType eventType;

    @Column(name = "event_category", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AuditCategory category;

    @Column(name = "username", length = 100)
    private String username;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "entity_name", length = 255)
    private String entityName;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "outcome", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditOutcome outcome;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "duration_ms")
    private Long durationMs;

    @ElementCollection
    @CollectionTable(
        name = "audit_event_details",
        joinColumns = @JoinColumn(name = "audit_event_id")
   )
    @MapKeyColumn(name = "detail_key", length = 100)
    @Column(name = "detail_value", length = 1000)
    private Map<String, String> details = new HashMap<>();

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "service_name", length = 50)
    private String serviceName;

    @Column(name = "environment", length = 20)
    private String environment;

    @Column(name = "api_endpoint", length = 255)
    private String apiEndpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "http_status")
    private Integer httpStatus;

    /**
     * Audit event types
     */
    public enum AuditEventType {
        // Authentication events
        LOGIN_SUCCESS,
        LOGIN_FAILURE,
        LOGOUT,
        TOKEN_REFRESH,
        TOKEN_REVOCATION,
        PASSWORD_CHANGE,
        PASSWORD_RESET_REQUEST,
        PASSWORD_RESET_COMPLETE,

        // Authorization events
        ACCESS_GRANTED,
        ACCESS_DENIED,
        PERMISSION_CHANGE,
        ROLE_ASSIGNMENT,
        ROLE_REVOCATION,

        // Data events
        CREATE,
        READ,
        UPDATE,
        DELETE,
        EXPORT,
        IMPORT,

        // Flow events
        FLOW_CREATED,
        FLOW_UPDATED,
        FLOW_DELETED,
        FLOW_EXECUTED,
        FLOW_DEPLOYED,
        FLOW_STOPPED,
        FLOW_ERROR,

        // Configuration events
        CONFIG_CHANGE,
        ADAPTER_CONFIGURED,
        CONNECTION_TEST,

        // System events
        SYSTEM_START,
        SYSTEM_STOP,
        SERVICE_HEALTH_CHECK,
        MAINTENANCE_MODE_ENABLED,
        MAINTENANCE_MODE_DISABLED,
        MAINTENANCE_PERFORMED,

        // Security events
        SECURITY_ALERT,
        SUSPICIOUS_ACTIVITY,
        RATE_LIMIT_EXCEEDED,
        API_KEY_CREATED,
        API_KEY_REVOKED,

        // Integration events
        MESSAGE_SENT,
        MESSAGE_RECEIVED,
        MESSAGE_PROCESSED,
        MESSAGE_FAILED,
        TRANSFORMATION_EXECUTED,

        // Administrative events
        USER_CREATED,
        USER_UPDATED,
        USER_DELETED,
        USER_ACTIVATED,
        USER_DEACTIVATED,
        TENANT_CREATED,
        TENANT_UPDATED,

        // Job events
        JOB_STARTED,
        JOB_COMPLETED,
        JOB_FAILED,
        JOB_CANCELLED
    }

    /**
     * Audit categories
     */
    public enum AuditCategory {
        AUTHENTICATION,
        AUTHORIZATION,
        DATA_ACCESS,
        CONFIGURATION,
        SYSTEM,
        SECURITY,
        INTEGRATION,
        ADMINISTRATION,
        JOB_EXECUTION
    }

    /**
     * Audit outcome
     */
    public enum AuditOutcome {
        SUCCESS,
        FAILURE,
        ERROR,
        WARNING
    }

    /**
     * Default constructor
     */
    public AuditEvent() {
        this.details = new HashMap<>();
    }
    
    /**
     * Add detail to audit event
     */
    public void addDetail(String key, String value) {
        if(details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
    }
    
    // Getters
    public UUID getId() { return id; }
    public Instant getEventTimestamp() { return eventTimestamp; }
    public AuditEventType getEventType() { return eventType; }
    public AuditCategory getCategory() { return category; }
    public String getUsername() { return username; }
    public UUID getUserId() { return userId; }
    public String getTenantId() { return tenantId; }
    public String getSessionId() { return sessionId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getRequestId() { return requestId; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getEntityName() { return entityName; }
    public String getAction() { return action; }
    public AuditOutcome getOutcome() { return outcome; }
    public String getErrorMessage() { return errorMessage; }
    public Long getDurationMs() { return durationMs; }
    public Map<String, String> getDetails() { return details; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getCorrelationId() { return correlationId; }
    public String getServiceName() { return serviceName; }
    public String getEnvironment() { return environment; }
    public String getApiEndpoint() { return apiEndpoint; }
    public String getHttpMethod() { return httpMethod; }
    public Integer getHttpStatus() { return httpStatus; }
    
    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }
    public void setEventType(AuditEventType eventType) { this.eventType = eventType; }
    public void setCategory(AuditCategory category) { this.category = category; }
    public void setUsername(String username) { this.username = username; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public void setEntityId(String entityId) { this.entityId = entityId; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public void setAction(String action) { this.action = action; }
    public void setOutcome(AuditOutcome outcome) { this.outcome = outcome; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public void setDetails(Map<String, String> details) { this.details = details; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setEnvironment(String environment) { this.environment = environment; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    /**
     * Create builder with common fields
     */
    public static AuditEventBuilder baseBuilder(AuditEventType eventType) {
        return AuditEvent.builder()
            .eventTimestamp(Instant.now())
            .eventType(eventType)
            .category(determineCategory(eventType))
            .serviceName("integrixs - backend")
            .environment(System.getenv("ENVIRONMENT") != null ?
                System.getenv("ENVIRONMENT") : "development");
    }

    /**
     * Determine category from event type
     */
    private static AuditCategory determineCategory(AuditEventType eventType) {
        String typeName = eventType.name();

        if(typeName.startsWith("LOGIN") || typeName.startsWith("LOGOUT") ||
            typeName.startsWith("TOKEN") || typeName.startsWith("PASSWORD")) {
            return AuditCategory.AUTHENTICATION;
        } else if(typeName.startsWith("ACCESS") || typeName.startsWith("PERMISSION") ||
                   typeName.startsWith("ROLE")) {
            return AuditCategory.AUTHORIZATION;
        } else if(typeName.equals("CREATE") || typeName.equals("READ") ||
                   typeName.equals("UPDATE") || typeName.equals("DELETE") ||
                   typeName.equals("EXPORT") || typeName.equals("IMPORT")) {
            return AuditCategory.DATA_ACCESS;
        } else if(typeName.startsWith("FLOW")) {
            return AuditCategory.INTEGRATION;
        } else if(typeName.startsWith("CONFIG") || typeName.startsWith("ADAPTER")) {
            return AuditCategory.CONFIGURATION;
        } else if(typeName.startsWith("SYSTEM") || typeName.startsWith("SERVICE") ||
                   typeName.startsWith("MAINTENANCE")) {
            return AuditCategory.SYSTEM;
        } else if(typeName.startsWith("SECURITY") || typeName.startsWith("SUSPICIOUS") ||
                   typeName.startsWith("API_KEY") || typeName.startsWith("RATE_LIMIT")) {
            return AuditCategory.SECURITY;
        } else if(typeName.startsWith("MESSAGE") || typeName.startsWith("TRANSFORMATION")) {
            return AuditCategory.INTEGRATION;
        } else if(typeName.startsWith("USER") || typeName.startsWith("TENANT")) {
            return AuditCategory.ADMINISTRATION;
        } else if(typeName.startsWith("JOB")) {
            return AuditCategory.JOB_EXECUTION;
        }

        return AuditCategory.SYSTEM;
    }
    
    public static class AuditEventBuilder {
        private UUID id;
        private Instant eventTimestamp;
        private AuditEventType eventType;
        private AuditCategory category;
        private String username;
        private String ipAddress;
        private String sessionId;
        private String tenantId;
        private String entityType;
        private String entityId;
                private Map<String, String> details;
        private String serviceName;
        private String environment;
        private String action;
        private AuditOutcome outcome;
        private String entityName;
        private UUID userId;
        private String correlationId;
        private String apiEndpoint;
        private String httpMethod;
        private Integer httpStatus;
        private Long durationMs;
        private String oldValue;
        private String newValue;
        private String errorMessage;
        private String userAgent;
        private String requestId;
        
        public AuditEventBuilder id(UUID id) {
            this.id = id;
            return this;
        }
        
        public AuditEventBuilder eventTimestamp(Instant eventTimestamp) {
            this.eventTimestamp = eventTimestamp;
            return this;
        }
        
        public AuditEventBuilder eventType(AuditEventType eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public AuditEventBuilder category(AuditCategory category) {
            this.category = category;
            return this;
        }
        
        public AuditEventBuilder username(String username) {
            this.username = username;
            return this;
        }
        
        public AuditEventBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public AuditEventBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public AuditEventBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public AuditEventBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }
        
        public AuditEventBuilder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }
        
        public AuditEventBuilder action(String action) {
            this.action = action;
            return this;
        }
        
        public AuditEventBuilder outcome(AuditOutcome outcome) {
            this.outcome = outcome;
            return this;
        }
        
        public AuditEventBuilder entityName(String entityName) {
            this.entityName = entityName;
            return this;
        }
        
        public AuditEventBuilder userId(UUID userId) {
            this.userId = userId;
            return this;
        }
        
        public AuditEventBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }
        
        public AuditEventBuilder apiEndpoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
            return this;
        }
        
        public AuditEventBuilder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }
        
        public AuditEventBuilder httpStatus(Integer httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }
        
        public AuditEventBuilder durationMs(Long durationMs) {
            this.durationMs = durationMs;
            return this;
        }
        
        public AuditEventBuilder oldValue(String oldValue) {
            this.oldValue = oldValue;
            return this;
        }
        
        public AuditEventBuilder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }
        
        public AuditEventBuilder details(Map<String, String> details) {
            this.details = details;
            return this;
        }
        
        public AuditEventBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }
        
        public AuditEventBuilder environment(String environment) {
            this.environment = environment;
            return this;
        }
        
        
        public AuditEventBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public AuditEventBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public AuditEventBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public AuditEvent build() {
            AuditEvent event = new AuditEvent();
            event.id = this.id;
            event.eventTimestamp = this.eventTimestamp;
            event.eventType = this.eventType;
            event.category = this.category;
            event.username = this.username;
            event.userId = this.userId;
            event.tenantId = this.tenantId;
            event.sessionId = this.sessionId;
            event.ipAddress = this.ipAddress;
            event.userAgent = this.userAgent;
            event.requestId = this.requestId;
            event.entityType = this.entityType;
            event.entityId = this.entityId;
            event.entityName = this.entityName;
            event.action = this.action;
            event.outcome = this.outcome;
            event.errorMessage = this.errorMessage;
            event.durationMs = this.durationMs;
            event.details = this.details;
            event.oldValue = this.oldValue;
            event.newValue = this.newValue;
            event.correlationId = this.correlationId;
            event.serviceName = this.serviceName;
            event.environment = this.environment;
            event.apiEndpoint = this.apiEndpoint;
            event.httpMethod = this.httpMethod;
            event.httpStatus = this.httpStatus;
            return event;
        }
    }
}
