package com.integrixs.backend.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.User;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audit logger for tracking data changes and sensitive operations.
 * Provides tamper - evident logging for compliance and security.
 */
@Component
public class AuditLogger {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);

    private final ObjectMapper objectMapper;

    public AuditLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Log data creation
     */
    public void logCreate(String entityType, String entityId, Object newData) {
        logDataChange("CREATE", entityType, entityId, null, newData, null);
    }

    /**
     * Log data update
     */
    public void logUpdate(String entityType, String entityId, Object oldData, Object newData) {
        logDataChange("UPDATE", entityType, entityId, oldData, newData, null);
    }

    /**
     * Log data deletion
     */
    public void logDelete(String entityType, String entityId, Object oldData) {
        logDataChange("DELETE", entityType, entityId, oldData, null, null);
    }

    /**
     * Log data access
     */
    public void logAccess(String entityType, String entityId, String accessType, Map<String, Object> metadata) {
        AuditEvent event = createAuditEvent("ACCESS", entityType, entityId);
        event.setAccessType(accessType);
        event.setMetadata(metadata);

        writeAuditLog(event);
    }

    /**
     * Log security event
     */
    public void logSecurityEvent(String eventType, String description, Map<String, Object> details) {
        AuditEvent event = createAuditEvent("SECURITY", "System", eventType);
        event.setDescription(description);
        event.setMetadata(details);
        event.setSeverity("HIGH");

        writeAuditLog(event);
    }

    /**
     * Log configuration change
     */
    public void logConfigChange(String configKey, String oldValue, String newValue) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("key", configKey);
        changes.put("oldValue", oldValue);
        changes.put("newValue", newValue);

        AuditEvent event = createAuditEvent("CONFIG", "Configuration", configKey);
        event.setMetadata(changes);

        writeAuditLog(event);
    }

    /**
     * Log administrative action
     */
    public void logAdminAction(String action, String targetType, String targetId, Map<String, Object> details) {
        AuditEvent event = createAuditEvent("ADMIN", targetType, targetId);
        event.setAction(action);
        event.setMetadata(details);
        event.setSeverity("MEDIUM");

        writeAuditLog(event);
    }

    /**
     * Log flow execution audit
     */
    public void logFlowExecution(String flowId, String flowName, String action, Map<String, Object> context) {
        AuditEvent event = createAuditEvent("FLOW", "IntegrationFlow", flowId);
        event.setAction(action);
        event.setFlowName(flowName);
        event.setMetadata(context);

        writeAuditLog(event);
    }

    /**
     * Core method for logging data changes
     */
    private void logDataChange(String operation, String entityType, String entityId,
                              Object oldData, Object newData, Map<String, Object> metadata) {
        AuditEvent event = createAuditEvent(operation, entityType, entityId);

        // Calculate data fingerprints for integrity
        if(oldData != null) {
            event.setOldDataFingerprint(calculateFingerprint(oldData));
            event.setOldData(sanitizeData(oldData));
        }

        if(newData != null) {
            event.setNewDataFingerprint(calculateFingerprint(newData));
            event.setNewData(sanitizeData(newData));
        }

        if(metadata != null) {
            event.setMetadata(metadata);
        }

        writeAuditLog(event);
    }

    /**
     * Create audit event with common fields
     */
    private AuditEvent createAuditEvent(String eventType, String entityType, String entityId) {
        AuditEvent event = new AuditEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(eventType);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setTimestamp(Instant.now());
        event.setCorrelationId(MDC.get("correlationId"));

        // Get user information
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()) {
            event.setUserId(auth.getName());
            if(auth.getPrincipal() instanceof User) {
                User user = (User) auth.getPrincipal();
                event.setUserEmail(user.getEmail());
                event.setUserRole(user.getRole());
            }
        }

        // Add request context
        event.setSessionId(MDC.get("sessionId"));
        event.setIpAddress(MDC.get("ipAddress"));
        event.setUserAgent(MDC.get("userAgent"));

        return event;
    }

    /**
     * Calculate SHA-256 fingerprint of data for integrity verification
     */
    private String calculateFingerprint(Object data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch(Exception e) {
            log.warn("Failed to calculate data fingerprint", e);
            return null;
        }
    }

    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for(byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Sanitize sensitive data before logging
     */
    private Object sanitizeData(Object data) {
        if(data == null) return null;

        try {
            // Convert to JSON and back to remove sensitive fields
            String json = objectMapper.writeValueAsString(data);
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            // Remove sensitive fields
            sanitizeMap(map);

            return map;
        } catch(Exception e) {
            log.warn("Failed to sanitize data for audit log", e);
            return data.getClass().getSimpleName() + "[sanitization - failed]";
        }
    }

    /**
     * Recursively sanitize map data
     */
    private void sanitizeMap(Map<String, Object> map) {
        String[] sensitiveFields = {"password", "secret", "key", "token", "credential", "ssn", "creditCard"};

        for(String field : sensitiveFields) {
            map.entrySet().removeIf(entry ->
                entry.getKey().toLowerCase().contains(field.toLowerCase())
           );
        }

        // Recursively sanitize nested maps
        map.values().stream()
            .filter(value -> value instanceof Map)
            .forEach(value -> sanitizeMap((Map<String, Object>) value));
    }

    /**
     * Write audit event to log
     */
    private void writeAuditLog(AuditEvent event) {
        try {
            String auditJson = objectMapper.writeValueAsString(event);

            // Use dedicated audit logger with structured format
            log.info("#AUDIT# {}# {}# {}# {}# {}# {}",
                event.getEventType(),
                event.getEntityType(),
                event.getEntityId(),
                event.getUserId() != null ? event.getUserId() : "SYSTEM",
                event.getTimestamp(),
                auditJson
           );

        } catch(Exception e) {
            log.error("Failed to write audit log", e);
        }
    }

    /**
     * Audit event structure
     */
    private static class AuditEvent {
        private String eventId;
        private String eventType;
        private String entityType;
        private String entityId;
        private Instant timestamp;
        private String correlationId;
        private String userId;
        private String userEmail;
        private String userRole;
        private String sessionId;
        private String ipAddress;
        private String userAgent;
        private String action;
        private String description;
        private String flowName;
        private String accessType;
        private String severity = "LOW";
        private Object oldData;
        private Object newData;
        private String oldDataFingerprint;
        private String newDataFingerprint;
        private Map<String, Object> metadata;

        // Getters and setters
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }

        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }

        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }

        public String getEntityId() { return entityId; }
        public void setEntityId(String entityId) { this.entityId = entityId; }

        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public String getUserRole() { return userRole; }
        public void setUserRole(String userRole) { this.userRole = userRole; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getFlowName() { return flowName; }
        public void setFlowName(String flowName) { this.flowName = flowName; }

        public String getAccessType() { return accessType; }
        public void setAccessType(String accessType) { this.accessType = accessType; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public Object getOldData() { return oldData; }
        public void setOldData(Object oldData) { this.oldData = oldData; }

        public Object getNewData() { return newData; }
        public void setNewData(Object newData) { this.newData = newData; }

        public String getOldDataFingerprint() { return oldDataFingerprint; }
        public void setOldDataFingerprint(String oldDataFingerprint) { this.oldDataFingerprint = oldDataFingerprint; }

        public String getNewDataFingerprint() { return newDataFingerprint; }
        public void setNewDataFingerprint(String newDataFingerprint) { this.newDataFingerprint = newDataFingerprint; }

        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }
}
