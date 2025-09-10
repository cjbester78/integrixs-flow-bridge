package com.integrixs.backend.audit;

import com.integrixs.backend.config.TenantContext;
import com.integrixs.backend.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Service for audit logging
 */
@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    @Autowired
    private AuditEventRepository auditRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Value("${audit.async.enabled:true}")
    private boolean asyncEnabled;
    
    @Value("${audit.sensitive.data.mask:true}")
    private boolean maskSensitiveData;
    
    /**
     * Log authentication event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logAuthentication(String username, boolean success, 
                                                    String ipAddress, String reason) {
        AuditEvent event = AuditEvent.baseBuilder(
                success ? AuditEvent.AuditEventType.LOGIN_SUCCESS : 
                         AuditEvent.AuditEventType.LOGIN_FAILURE)
            .username(username)
            .ipAddress(ipAddress)
            .outcome(success ? AuditEvent.AuditOutcome.SUCCESS : 
                             AuditEvent.AuditOutcome.FAILURE)
            .action(success ? "User logged in" : "Login attempt failed")
            .errorMessage(success ? null : reason)
            .build();
        
        enrichWithRequestContext(event);
        auditRepository.save(event);
        publishEvent(event);
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Log data access event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logDataAccess(AuditEvent.AuditEventType eventType,
                                                 String entityType, String entityId,
                                                 String entityName) {
        AuditEvent event = AuditEvent.baseBuilder(eventType)
            .entityType(entityType)
            .entityId(entityId)
            .entityName(entityName)
            .action(String.format("%s %s: %s", eventType.name(), entityType, entityName))
            .outcome(AuditEvent.AuditOutcome.SUCCESS)
            .build();
        
        enrichWithSecurityContext(event);
        enrichWithRequestContext(event);
        auditRepository.save(event);
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Log configuration change
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logConfigChange(String configType, String configName,
                               String oldValue, String newValue) {
        AuditEvent event = AuditEvent.baseBuilder(AuditEvent.AuditEventType.CONFIG_CHANGE)
            .entityType(configType)
            .entityName(configName)
            .oldValue(maskSensitiveData ? maskValue(oldValue) : oldValue)
            .newValue(maskSensitiveData ? maskValue(newValue) : newValue)
            .action(String.format("Configuration changed: %s.%s", configType, configName))
            .outcome(AuditEvent.AuditOutcome.SUCCESS)
            .build();
        
        enrichWithSecurityContext(event);
        enrichWithRequestContext(event);
        auditRepository.save(event);
        publishEvent(event);
    }
    
    /**
     * Log security event
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(AuditEvent.AuditEventType eventType, String description,
                                Map<String, String> details) {
        AuditEvent event = AuditEvent.baseBuilder(eventType)
            .action(description)
            .outcome(AuditEvent.AuditOutcome.WARNING)
            .details(details)
            .build();
        
        enrichWithSecurityContext(event);
        enrichWithRequestContext(event);
        auditRepository.save(event);
        publishEvent(event);
        
        // For critical security events, also log to security logger
        if (eventType == AuditEvent.AuditEventType.SECURITY_ALERT ||
            eventType == AuditEvent.AuditEventType.SUSPICIOUS_ACTIVITY) {
            logger.warn("SECURITY EVENT: {} - {}", eventType, description);
        }
    }
    
    /**
     * Log flow execution
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logFlowExecution(String flowId, String flowName,
                                                   boolean success, Long durationMs,
                                                   String errorMessage) {
        AuditEvent event = AuditEvent.baseBuilder(
                success ? AuditEvent.AuditEventType.FLOW_EXECUTED : 
                         AuditEvent.AuditEventType.FLOW_ERROR)
            .entityType("Flow")
            .entityId(flowId)
            .entityName(flowName)
            .action(String.format("Flow %s %s", flowName, 
                   success ? "executed successfully" : "failed"))
            .outcome(success ? AuditEvent.AuditOutcome.SUCCESS : 
                             AuditEvent.AuditOutcome.FAILURE)
            .durationMs(durationMs)
            .errorMessage(errorMessage)
            .build();
        
        enrichWithSecurityContext(event);
        event.setCorrelationId(MDC.get("correlationId"));
        auditRepository.save(event);
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Log API access
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> logApiAccess(String endpoint, String method,
                                               int statusCode, Long durationMs) {
        // Only log non-GET requests or errors
        if ("GET".equals(method) && statusCode >= 200 && statusCode < 300) {
            return CompletableFuture.completedFuture(null);
        }
        
        AuditEvent.AuditEventType eventType = statusCode >= 400 ? 
            AuditEvent.AuditEventType.ACCESS_DENIED : 
            AuditEvent.AuditEventType.ACCESS_GRANTED;
        
        AuditEvent event = AuditEvent.baseBuilder(eventType)
            .apiEndpoint(endpoint)
            .httpMethod(method)
            .httpStatus(statusCode)
            .action(String.format("API %s %s", method, endpoint))
            .outcome(statusCode < 400 ? AuditEvent.AuditOutcome.SUCCESS : 
                                       AuditEvent.AuditOutcome.FAILURE)
            .durationMs(durationMs)
            .build();
        
        enrichWithSecurityContext(event);
        enrichWithRequestContext(event);
        auditRepository.save(event);
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Audit method execution with timing
     */
    public <T> T auditExecution(String action, Supplier<T> supplier) {
        Instant start = Instant.now();
        AuditEvent.AuditOutcome outcome = AuditEvent.AuditOutcome.SUCCESS;
        String errorMessage = null;
        
        try {
            T result = supplier.get();
            return result;
        } catch (Exception e) {
            outcome = AuditEvent.AuditOutcome.ERROR;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            
            AuditEvent event = AuditEvent.baseBuilder(AuditEvent.AuditEventType.SYSTEM_START)
                .action(action)
                .outcome(outcome)
                .durationMs(duration.toMillis())
                .errorMessage(errorMessage)
                .build();
            
            enrichWithSecurityContext(event);
            
            if (asyncEnabled) {
                CompletableFuture.runAsync(() -> {
                    try {
                        auditRepository.save(event);
                    } catch (Exception e) {
                        logger.error("Failed to save audit event", e);
                    }
                });
            } else {
                auditRepository.save(event);
            }
        }
    }
    
    /**
     * Log job execution
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logJobExecution(UUID jobId, String jobType, 
                               AuditEvent.AuditEventType eventType,
                               Map<String, String> details) {
        AuditEvent event = AuditEvent.baseBuilder(eventType)
            .entityType("Job")
            .entityId(jobId.toString())
            .entityName(jobType)
            .action(String.format("Job %s %s", jobType, eventType.name().toLowerCase()))
            .outcome(eventType == AuditEvent.AuditEventType.JOB_FAILED ? 
                    AuditEvent.AuditOutcome.FAILURE : 
                    AuditEvent.AuditOutcome.SUCCESS)
            .details(details)
            .build();
        
        enrichWithSecurityContext(event);
        auditRepository.save(event);
    }
    
    /**
     * Create audit context for method interception
     */
    public AuditContext createContext(String action) {
        return new AuditContext(action);
    }
    
    /**
     * Enrich event with security context
     */
    private void enrichWithSecurityContext(AuditEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            event.setUsername(auth.getName());
            // Extract user ID if available
            if (auth.getPrincipal() instanceof SecurityUtils.UserPrincipal) {
                SecurityUtils.UserPrincipal principal = 
                    (SecurityUtils.UserPrincipal) auth.getPrincipal();
                event.setUserId(principal.getUserId());
            }
        }
        
        // Set tenant context
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId != null) {
            event.setTenantId(tenantId);
        }
    }
    
    /**
     * Enrich event with request context
     */
    private void enrichWithRequestContext(AuditEvent event) {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                
                // Set request details
                event.setIpAddress(getClientIpAddress(request));
                event.setUserAgent(request.getHeader("User-Agent"));
                event.setSessionId(request.getSession(false) != null ? 
                    request.getSession().getId() : null);
                event.setRequestId(request.getHeader("X-Request-ID"));
                
                // Set API endpoint if not already set
                if (event.getApiEndpoint() == null) {
                    event.setApiEndpoint(request.getRequestURI());
                }
                if (event.getHttpMethod() == null) {
                    event.setHttpMethod(request.getMethod());
                }
            }
        } catch (Exception e) {
            logger.debug("Could not enrich audit event with request context", e);
        }
        
        // Set MDC values
        event.setCorrelationId(MDC.get("correlationId"));
        if (event.getRequestId() == null) {
            event.setRequestId(MDC.get("requestId"));
        }
    }
    
    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Mask sensitive data
     */
    private String maskValue(String value) {
        if (value == null || value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }
    
    /**
     * Publish audit event for real-time monitoring
     */
    private void publishEvent(AuditEvent event) {
        try {
            eventPublisher.publishEvent(new AuditEventNotification(event));
        } catch (Exception e) {
            logger.warn("Failed to publish audit event", e);
        }
    }
    
    /**
     * Audit context for method interception
     */
    public class AuditContext implements AutoCloseable {
        private final String action;
        private final Instant startTime;
        private final Map<String, String> details;
        
        public AuditContext(String action) {
            this.action = action;
            this.startTime = Instant.now();
            this.details = new HashMap<>();
        }
        
        public AuditContext withDetail(String key, String value) {
            details.put(key, value);
            return this;
        }
        
        @Override
        public void close() {
            // Log audit event when context closes
            Duration duration = Duration.between(startTime, Instant.now());
            
            AuditEvent event = AuditEvent.baseBuilder(AuditEvent.AuditEventType.SYSTEM_START)
                .action(action)
                .outcome(AuditEvent.AuditOutcome.SUCCESS)
                .durationMs(duration.toMillis())
                .details(details)
                .build();
            
            enrichWithSecurityContext(event);
            enrichWithRequestContext(event);
            
            if (asyncEnabled) {
                CompletableFuture.runAsync(() -> auditRepository.save(event));
            } else {
                auditRepository.save(event);
            }
        }
    }
    
    /**
     * Audit event notification
     */
    public static class AuditEventNotification {
        private final AuditEvent event;
        
        public AuditEventNotification(AuditEvent event) {
            this.event = event;
        }
        
        public AuditEvent getEvent() {
            return event;
        }
    }
}