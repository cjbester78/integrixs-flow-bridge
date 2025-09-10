package com.integrixs.backend.audit;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditEvent {
    
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
     * Add detail to audit event
     */
    public void addDetail(String key, String value) {
        if (details == null) {
            details = new HashMap<>();
        }
        details.put(key, value);
    }
    
    /**
     * Create builder with common fields
     */
    public static AuditEventBuilder baseBuilder(AuditEventType eventType) {
        return AuditEvent.builder()
            .eventTimestamp(Instant.now())
            .eventType(eventType)
            .category(determineCategory(eventType))
            .serviceName("integrixs-backend")
            .environment(System.getenv("ENVIRONMENT") != null ? 
                System.getenv("ENVIRONMENT") : "development");
    }
    
    /**
     * Determine category from event type
     */
    private static AuditCategory determineCategory(AuditEventType eventType) {
        String typeName = eventType.name();
        
        if (typeName.startsWith("LOGIN") || typeName.startsWith("LOGOUT") || 
            typeName.startsWith("TOKEN") || typeName.startsWith("PASSWORD")) {
            return AuditCategory.AUTHENTICATION;
        } else if (typeName.startsWith("ACCESS") || typeName.startsWith("PERMISSION") || 
                   typeName.startsWith("ROLE")) {
            return AuditCategory.AUTHORIZATION;
        } else if (typeName.equals("CREATE") || typeName.equals("READ") || 
                   typeName.equals("UPDATE") || typeName.equals("DELETE") ||
                   typeName.equals("EXPORT") || typeName.equals("IMPORT")) {
            return AuditCategory.DATA_ACCESS;
        } else if (typeName.startsWith("FLOW")) {
            return AuditCategory.INTEGRATION;
        } else if (typeName.startsWith("CONFIG") || typeName.startsWith("ADAPTER")) {
            return AuditCategory.CONFIGURATION;
        } else if (typeName.startsWith("SYSTEM") || typeName.startsWith("SERVICE") || 
                   typeName.startsWith("MAINTENANCE")) {
            return AuditCategory.SYSTEM;
        } else if (typeName.startsWith("SECURITY") || typeName.startsWith("SUSPICIOUS") || 
                   typeName.startsWith("API_KEY") || typeName.startsWith("RATE_LIMIT")) {
            return AuditCategory.SECURITY;
        } else if (typeName.startsWith("MESSAGE") || typeName.startsWith("TRANSFORMATION")) {
            return AuditCategory.INTEGRATION;
        } else if (typeName.startsWith("USER") || typeName.startsWith("TENANT")) {
            return AuditCategory.ADMINISTRATION;
        } else if (typeName.startsWith("JOB")) {
            return AuditCategory.JOB_EXECUTION;
        }
        
        return AuditCategory.SYSTEM;
    }
}