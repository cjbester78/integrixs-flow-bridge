package com.integrixs.backend.service;

import com.integrixs.backend.security.AuditLogEncryptionService;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.data.model.AuditTrail;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.AuditTrailSqlRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing audit trail entries.
 *
 * <p>Provides methods to log and retrieve audit information for all CRUD operations.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Service
public class AuditTrailService {

    private static final Logger log = LoggerFactory.getLogger(AuditTrailService.class);

    @Autowired
    private AuditTrailSqlRepository auditTrailRepository;

    @Autowired
    private UserSqlRepository userRepository;

    @Autowired(required = false)
    private AuditLogEncryptionService auditLogEncryptionService;

    @Value("${audit.encryption.enabled:false}")
    private boolean encryptionEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Log a CREATE operation
     */
    @Async
    public void logCreate(String entityType, String entityId, Object newEntity) {
        try {
            AuditTrail audit = buildAuditEntry(entityType, entityId, AuditTrail.AuditAction.CREATE);

            // Store the new entity state
            ObjectNode changes = objectMapper.createObjectNode();
            changes.set("new", objectMapper.valueToTree(newEntity));
            audit.setChanges(changes.toString());

            // Encrypt sensitive data if enabled
            if(encryptionEnabled && auditLogEncryptionService != null) {
                audit = auditLogEncryptionService.encryptAuditTrail(audit);
            }

            auditTrailRepository.save(audit);
            log.debug("Created audit entry for CREATE operation on {} with ID {}", entityType, entityId);
        } catch(Exception e) {
            log.error("Failed to create audit entry for CREATE operation", e);
        }
    }

    /**
     * Log an UPDATE operation
     */
    @Async
    public void logUpdate(String entityType, String entityId, Object oldEntity, Object newEntity) {
        try {
            AuditTrail audit = buildAuditEntry(entityType, entityId, AuditTrail.AuditAction.UPDATE);

            // Store both old and new states
            ObjectNode changes = objectMapper.createObjectNode();
            changes.set("old", objectMapper.valueToTree(oldEntity));
            changes.set("new", objectMapper.valueToTree(newEntity));
            audit.setChanges(changes.toString());

            // Encrypt sensitive data if enabled
            if(encryptionEnabled && auditLogEncryptionService != null) {
                audit = auditLogEncryptionService.encryptAuditTrail(audit);
            }

            auditTrailRepository.save(audit);
            log.debug("Created audit entry for UPDATE operation on {} with ID {}", entityType, entityId);
        } catch(Exception e) {
            log.error("Failed to create audit entry for UPDATE operation", e);
        }
    }

    /**
     * Log a DELETE operation
     */
    @Async
    public void logDelete(String entityType, String entityId, Object oldEntity) {
        try {
            AuditTrail audit = buildAuditEntry(entityType, entityId, AuditTrail.AuditAction.DELETE);

            // Store the deleted entity state
            ObjectNode changes = objectMapper.createObjectNode();
            changes.set("old", objectMapper.valueToTree(oldEntity));
            audit.setChanges(changes.toString());

            // Encrypt sensitive data if enabled
            if(encryptionEnabled && auditLogEncryptionService != null) {
                audit = auditLogEncryptionService.encryptAuditTrail(audit);
            }

            auditTrailRepository.save(audit);
            log.debug("Created audit entry for DELETE operation on {} with ID {}", entityType, entityId);
        } catch(Exception e) {
            log.error("Failed to create audit entry for DELETE operation", e);
        }
    }

    /**
     * Log a custom action
     */
    @Async
    public void logAction(String entityType, String entityId, AuditTrail.AuditAction action, Map<String, Object> details) {
        try {
            AuditTrail audit = buildAuditEntry(entityType, entityId, action);

            if(details != null && !details.isEmpty()) {
                audit.setChanges(objectMapper.writeValueAsString(details));
            }

            // Encrypt sensitive data if enabled
            if(encryptionEnabled && auditLogEncryptionService != null) {
                audit = auditLogEncryptionService.encryptAuditTrail(audit);
            }

            auditTrailRepository.save(audit);
            log.debug("Created audit entry for {} operation on {} with ID {}", action, entityType, entityId);
        } catch(Exception e) {
            log.error("Failed to create audit entry for {} operation", action, e);
        }
    }

    /**
     * Log a user action
     */
    @Async
    public void logUserAction(User performedByUser, String entityType, String entityId, String action) {
        try {
            AuditTrail audit = new AuditTrail();
            audit.setEntityType(entityType);
            audit.setEntityId(entityId);
            audit.setAction(AuditTrail.AuditAction.valueOf(action.toUpperCase()));
            audit.setUser(performedByUser);

            // Get request details
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if(attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                audit.setUserIp(extractClientIp(request));
                audit.setUserAgent(request.getHeader("User-Agent"));
            }

            // Encrypt sensitive data if enabled
            if(encryptionEnabled && auditLogEncryptionService != null) {
                audit = auditLogEncryptionService.encryptAuditTrail(audit);
            }

            auditTrailRepository.save(audit);
            log.debug("Created audit entry for user action {} on {} with ID {}", action, entityType, entityId);
        } catch(Exception e) {
            log.error("Failed to create audit entry for user action", e);
        }
    }

    /**
     * Extract client IP address from request
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if(xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if(xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Build a basic audit entry with common fields
     */
    private AuditTrail buildAuditEntry(String entityType, String entityId, AuditTrail.AuditAction action) {
        AuditTrail audit = new AuditTrail();
        audit.setEntityType(entityType);
        audit.setEntityId(entityId);
        audit.setAction(action);

        // Get current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                String username = auth.getName();
                Optional<User> userOpt = userRepository.findByUsername(username);
                if(userOpt.isPresent()) {
                    audit.setUser(userOpt.get());
                } else {
                    log.warn("Could not find user with username: {}", username);
                }
            } catch(Exception e) {
                log.warn("Could not retrieve user for audit entry", e);
            }
        }

        // Get request details
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        if(attr != null) {
            HttpServletRequest request = attr.getRequest();
            audit.setUserIp(getClientIpAddress(request));
            audit.setUserAgent(request.getHeader("User-Agent"));
        }

        return audit;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for(String header : headerNames) {
            String ip = request.getHeader(header);
            if(ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle multiple IPs in X-Forwarded-For
                if(ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Retrieve audit history for an entity
     */
    public Page<AuditTrail> getEntityAuditHistory(String entityType, String entityId, Pageable pageable) {
        // Convert list to page
        List<AuditTrail> audits = auditTrailRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);

        // Decrypt sensitive data if enabled
        if(encryptionEnabled && auditLogEncryptionService != null) {
            audits = audits.stream()
                .map(audit -> auditLogEncryptionService.decryptAuditTrail(audit))
                .collect(Collectors.toList());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), audits.size());
        return new PageImpl<>(audits.subList(start, end), pageable, audits.size());
    }

    /**
     * Retrieve audit history by user
     */
    public Page<AuditTrail> getUserAuditHistory(String userId, Pageable pageable) {
        Page<AuditTrail> audits = auditTrailRepository.findByUserIdOrderByCreatedAtDesc(UUID.fromString(userId), pageable);

        // Decrypt sensitive data if enabled
        if(encryptionEnabled && auditLogEncryptionService != null) {
            List<AuditTrail> decryptedList = audits.getContent().stream()
                .map(audit -> auditLogEncryptionService.decryptAuditTrail(audit))
                .collect(Collectors.toList());
            return new PageImpl<>(decryptedList, pageable, audits.getTotalElements());
        }

        return audits;
    }

    /**
     * Search audit trail with filters
     */
    public Page<AuditTrail> searchAuditTrail(String entityType, AuditTrail.AuditAction action,
                                            String userId, LocalDateTime startDate,
                                            LocalDateTime endDate, Pageable pageable) {
        Page<AuditTrail> audits = auditTrailRepository.searchAuditTrail(entityType, action, userId != null ? UUID.fromString(userId) : null, startDate, endDate, pageable);

        // Decrypt sensitive data if enabled
        if(encryptionEnabled && auditLogEncryptionService != null) {
            List<AuditTrail> decryptedList = audits.getContent().stream()
                .map(audit -> auditLogEncryptionService.decryptAuditTrail(audit))
                .collect(Collectors.toList());
            return new PageImpl<>(decryptedList, pageable, audits.getTotalElements());
        }

        return audits;
    }
}
