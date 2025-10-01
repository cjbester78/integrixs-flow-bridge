package com.integrixs.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.AuditTrail;
import com.integrixs.data.model.SystemLog;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for encrypting sensitive data in audit logs and system logs.
 * Extends FieldEncryptionService to handle audit - specific encryption needs.
 */
@Service
public class AuditLogEncryptionService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuditLogEncryptionService.class);

    private final FieldEncryptionService fieldEncryptionService;
    private final ObjectMapper objectMapper;

    // Fields to encrypt in audit logs
    private static final String[] AUDIT_SENSITIVE_FIELDS = {
        "password",
        "token",
        "secret",
        "apiKey",
        "apiSecret",
        "clientSecret",
        "privateKey",
        "certificate",
        "connectionString",
        "jdbcUrl",
        "username",
        "email",
        "ipAddress",
        "userAgent"
    };

    // Fields to encrypt in system logs
    private static final String[] SYSTEM_LOG_SENSITIVE_FIELDS = {
        "message",
        "details",
        "stackTrace",
        "ipAddress",
        "userAgent",
        "url"
    };

    // Constructor
    public AuditLogEncryptionService(FieldEncryptionService fieldEncryptionService, ObjectMapper objectMapper) {
        this.fieldEncryptionService = fieldEncryptionService;
        this.objectMapper = objectMapper;
    }

    /**
     * Encrypt sensitive data in an AuditTrail entry.
     */
    public AuditTrail encryptAuditTrail(AuditTrail auditTrail) {
        if(auditTrail == null) {
            return null;
        }

        try {
            // Encrypt changes field if it contains sensitive data
            if(auditTrail.getChanges() != null) {
                String encryptedChanges = encryptChangesJson(auditTrail.getChanges());
                auditTrail.setChanges(encryptedChanges);
            }

            // Encrypt user IP
            if(auditTrail.getUserIp() != null) {
                String encryptedIp = fieldEncryptionService.encryptField("audit.userIp", auditTrail.getUserIp());
                auditTrail.setUserIp(encryptedIp);
            }

            // Encrypt user agent
            if(auditTrail.getUserAgent() != null) {
                String encryptedAgent = fieldEncryptionService.encryptField("audit.userAgent", auditTrail.getUserAgent());
                auditTrail.setUserAgent(encryptedAgent);
            }

        } catch(Exception e) {
            log.error("Failed to encrypt audit trail", e);
        }

        return auditTrail;
    }

    /**
     * Decrypt sensitive data in an AuditTrail entry.
     */
    public AuditTrail decryptAuditTrail(AuditTrail auditTrail) {
        if(auditTrail == null) {
            return null;
        }

        try {
            // Decrypt changes field
            if(auditTrail.getChanges() != null && fieldEncryptionService.isEncrypted(auditTrail.getChanges())) {
                String decryptedChanges = decryptChangesJson(auditTrail.getChanges());
                auditTrail.setChanges(decryptedChanges);
            }

            // Decrypt user IP
            if(auditTrail.getUserIp() != null && fieldEncryptionService.isEncrypted(auditTrail.getUserIp())) {
                String decryptedIp = fieldEncryptionService.decryptField("audit.userIp", auditTrail.getUserIp());
                auditTrail.setUserIp(decryptedIp);
            }

            // Decrypt user agent
            if(auditTrail.getUserAgent() != null && fieldEncryptionService.isEncrypted(auditTrail.getUserAgent())) {
                String decryptedAgent = fieldEncryptionService.decryptField("audit.userAgent", auditTrail.getUserAgent());
                auditTrail.setUserAgent(decryptedAgent);
            }

        } catch(Exception e) {
            log.error("Failed to decrypt audit trail", e);
        }

        return auditTrail;
    }

    /**
     * Encrypt sensitive data in a SystemLog entry.
     */
    public SystemLog encryptSystemLog(SystemLog systemLog) {
        if(systemLog == null) {
            return null;
        }

        try {
            // Encrypt message if it contains sensitive patterns
            if(systemLog.getMessage() != null && containsSensitiveData(systemLog.getMessage())) {
                String encrypted = fieldEncryptionService.encryptField("systemlog.message", systemLog.getMessage());
                systemLog.setMessage(encrypted);
            }

            // Encrypt details
            if(systemLog.getDetails() != null && containsSensitiveData(systemLog.getDetails())) {
                String encrypted = fieldEncryptionService.encryptField("systemlog.details", systemLog.getDetails());
                systemLog.setDetails(encrypted);
            }

            // Encrypt stack trace
            if(systemLog.getStackTrace() != null) {
                String encrypted = fieldEncryptionService.encryptField("systemlog.stackTrace", systemLog.getStackTrace());
                systemLog.setStackTrace(encrypted);
            }

            // Encrypt IP address
            if(systemLog.getIpAddress() != null) {
                String encrypted = fieldEncryptionService.encryptField("systemlog.ipAddress", systemLog.getIpAddress());
                systemLog.setIpAddress(encrypted);
            }

            // Encrypt user agent
            if(systemLog.getUserAgent() != null) {
                String encrypted = fieldEncryptionService.encryptField("systemlog.userAgent", systemLog.getUserAgent());
                systemLog.setUserAgent(encrypted);
            }

            // Encrypt URL if it contains query parameters
            if(systemLog.getUrl() != null && systemLog.getUrl().contains("?")) {
                String encrypted = fieldEncryptionService.encryptField("systemlog.url", systemLog.getUrl());
                systemLog.setUrl(encrypted);
            }

        } catch(Exception e) {
            log.error("Failed to encrypt system log", e);
        }

        return systemLog;
    }

    /**
     * Decrypt sensitive data in a SystemLog entry.
     */
    public SystemLog decryptSystemLog(SystemLog systemLog) {
        if(systemLog == null) {
            return null;
        }

        try {
            // Decrypt message
            if(systemLog.getMessage() != null && fieldEncryptionService.isEncrypted(systemLog.getMessage())) {
                String decrypted = fieldEncryptionService.decryptField("systemlog.message", systemLog.getMessage());
                systemLog.setMessage(decrypted);
            }

            // Decrypt details
            if(systemLog.getDetails() != null && fieldEncryptionService.isEncrypted(systemLog.getDetails())) {
                String decrypted = fieldEncryptionService.decryptField("systemlog.details", systemLog.getDetails());
                systemLog.setDetails(decrypted);
            }

            // Decrypt stack trace
            if(systemLog.getStackTrace() != null && fieldEncryptionService.isEncrypted(systemLog.getStackTrace())) {
                String decrypted = fieldEncryptionService.decryptField("systemlog.stackTrace", systemLog.getStackTrace());
                systemLog.setStackTrace(decrypted);
            }

            // Decrypt IP address
            if(systemLog.getIpAddress() != null && fieldEncryptionService.isEncrypted(systemLog.getIpAddress())) {
                String decrypted = fieldEncryptionService.decryptField("systemlog.ipAddress", systemLog.getIpAddress());
                systemLog.setIpAddress(decrypted);
            }

            // Decrypt user agent
            if(systemLog.getUserAgent() != null && fieldEncryptionService.isEncrypted(systemLog.getUserAgent())) {
                String decrypted = fieldEncryptionService.decryptField("systemlog.userAgent", systemLog.getUserAgent());
                systemLog.setUserAgent(decrypted);
            }

            // Decrypt URL
            if(systemLog.getUrl() != null && fieldEncryptionService.isEncrypted(systemLog.getUrl())) {
                String decrypted = fieldEncryptionService.decryptField("systemlog.url", systemLog.getUrl());
                systemLog.setUrl(decrypted);
            }

        } catch(Exception e) {
            log.error("Failed to decrypt system log", e);
        }

        return systemLog;
    }

    /**
     * Encrypt sensitive fields in a JSON changes string.
     */
    private String encryptChangesJson(String changesJson) {
        try {
            // Parse JSON
            Map<String, Object> changes = objectMapper.readValue(changesJson, Map.class);

            // Encrypt sensitive fields in before/after objects
            if(changes.containsKey("before")) {
                Map<String, Object> before = (Map<String, Object>) changes.get("before");
                encryptSensitiveFields(before);
            }

            if(changes.containsKey("after")) {
                Map<String, Object> after = (Map<String, Object>) changes.get("after");
                encryptSensitiveFields(after);
            }

            // Convert back to JSON
            return objectMapper.writeValueAsString(changes);

        } catch(Exception e) {
            log.warn("Failed to parse changes JSON for encryption, encrypting whole string", e);
            return fieldEncryptionService.encryptField("audit.changes", changesJson);
        }
    }

    /**
     * Decrypt sensitive fields in a JSON changes string.
     */
    private String decryptChangesJson(String encryptedJson) {
        // If the whole string is encrypted
        if(fieldEncryptionService.isEncrypted(encryptedJson)) {
            return fieldEncryptionService.decryptField("audit.changes", encryptedJson);
        }

        try {
            // Parse JSON and decrypt individual fields
            Map<String, Object> changes = objectMapper.readValue(encryptedJson, Map.class);

            if(changes.containsKey("before")) {
                Map<String, Object> before = (Map<String, Object>) changes.get("before");
                decryptSensitiveFields(before);
            }

            if(changes.containsKey("after")) {
                Map<String, Object> after = (Map<String, Object>) changes.get("after");
                decryptSensitiveFields(after);
            }

            return objectMapper.writeValueAsString(changes);

        } catch(Exception e) {
            log.warn("Failed to parse changes JSON for decryption", e);
            return encryptedJson;
        }
    }

    /**
     * Encrypt sensitive fields in a map.
     */
    private void encryptSensitiveFields(Map<String, Object> data) {
        for(String field : AUDIT_SENSITIVE_FIELDS) {
            if(data.containsKey(field)) {
                Object value = data.get(field);
                if(value instanceof String) {
                    String encrypted = fieldEncryptionService.encryptField("audit." + field, (String) value);
                    data.put(field, encrypted);
                }
            }
        }
    }

    /**
     * Decrypt sensitive fields in a map.
     */
    private void decryptSensitiveFields(Map<String, Object> data) {
        for(Map.Entry<String, Object> entry : data.entrySet()) {
            if(entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                if(fieldEncryptionService.isEncrypted(value)) {
                    String decrypted = fieldEncryptionService.decryptField("audit." + entry.getKey(), value);
                    data.put(entry.getKey(), decrypted);
                }
            }
        }
    }

    /**
     * Check if a string contains sensitive data patterns.
     */
    private boolean containsSensitiveData(String text) {
        if(text == null) {
            return false;
        }

        String lowerText = text.toLowerCase();

        // Check for sensitive keywords
        return lowerText.contains("password") ||
               lowerText.contains("token") ||
               lowerText.contains("secret") ||
               lowerText.contains("api_key") ||
               lowerText.contains("apikey") ||
               lowerText.contains("authorization") ||
               lowerText.contains("bearer ") ||
               lowerText.contains("basic ") ||
               lowerText.contains("private") ||
               lowerText.contains("credential");
    }
}
