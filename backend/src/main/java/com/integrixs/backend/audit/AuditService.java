package com.integrixs.backend.audit;

import com.integrixs.backend.config.TenantContext;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.AuditEvent;
import com.integrixs.data.sql.repository.AuditEventRepository;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
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

    @Autowired
    private com.integrixs.data.sql.repository.SystemLogSqlRepository systemLogRepository;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${audit.async.enabled:true}")
    private boolean asyncEnabled;

    @Value("${audit.sensitive.data.mask:true}")
    private boolean maskSensitiveData;

    /**
     * Log authentication event
     */
    @Async
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
    public void logConfigChange(String configType, String configName,
                               String oldValue, String newValue) {
        Map<String, Object> details = new HashMap<>();
        details.put("oldValue", oldValue);
        details.put("newValue", newValue);

        AuditEvent event = AuditEvent.baseBuilder(AuditEvent.AuditEventType.CONFIG_CHANGE)
            .entityType(configType)
            .entityName(configName)
            .details(convertMapToJson(details))
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
    public void logSecurityEvent(AuditEvent.AuditEventType eventType, String description,
                                Map<String, String> details) {
        AuditEvent event = AuditEvent.baseBuilder(eventType)
            .action(description)
            .outcome(AuditEvent.AuditOutcome.WARNING)
            .details(convertMapToJson(details))
            .build();

        enrichWithSecurityContext(event);
        enrichWithRequestContext(event);
        auditRepository.save(event);
        publishEvent(event);

        // For critical security events, also log to security logger
        if(eventType == AuditEvent.AuditEventType.SECURITY_ALERT ||
            eventType == AuditEvent.AuditEventType.SUSPICIOUS_ACTIVITY) {
            logger.warn("SECURITY EVENT: {} - {}", eventType, description);
        }
    }

    /**
     * Log flow execution
     */
    @Async
    public CompletableFuture<Void> logFlowExecution(String flowId, String flowName,
                                                   boolean success, Long durationMs,
                                                   String errorMessage) {
        Map<String, Object> details = new HashMap<>();
        if (durationMs != null) {
            details.put("durationMs", durationMs);
        }

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
            .details(details.isEmpty() ? null : convertMapToJson(details))
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
    public CompletableFuture<Void> logApiAccess(String endpoint, String method,
                                               int statusCode, Long durationMs) {
        // Only log non - GET requests or errors
        if("GET".equals(method) && statusCode >= 200 && statusCode < 300) {
            return CompletableFuture.completedFuture(null);
        }

        AuditEvent.AuditEventType eventType = statusCode >= 400 ?
            AuditEvent.AuditEventType.ACCESS_DENIED :
            AuditEvent.AuditEventType.CREATE; // Using CREATE as a substitute for ACCESS_GRANTED

        Map<String, Object> details = new HashMap<>();
        details.put("statusCode", statusCode);
        if (durationMs != null) {
            details.put("durationMs", durationMs);
        }

        AuditEvent event = AuditEvent.baseBuilder(eventType)
            .apiEndpoint(endpoint)
            .httpMethod(method)
            .details(convertMapToJson(details))
            .action(String.format("API %s %s", method, endpoint))
            .outcome(statusCode < 400 ? AuditEvent.AuditOutcome.SUCCESS :
                                       AuditEvent.AuditOutcome.FAILURE)
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
        } catch(Exception e) {
            outcome = AuditEvent.AuditOutcome.FAILURE;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            Duration duration = Duration.between(start, Instant.now());

            Map<String, Object> details = new HashMap<>();
            details.put("durationMs", duration.toMillis());

            AuditEvent event = AuditEvent.baseBuilder(AuditEvent.AuditEventType.SYSTEM_START)
                .action(action)
                .outcome(outcome)
                .details(convertMapToJson(details))
                .errorMessage(errorMessage)
                .build();

            enrichWithSecurityContext(event);

            if(asyncEnabled) {
                CompletableFuture.runAsync(() -> {
                    try {
                        auditRepository.save(event);
                    } catch(Exception e) {
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
            .details(details != null ? convertMapToJson(details) : null)
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
     * Log package creation audit event (from service version)
     */
    @Async
    public void logPackageCreation(UUID correlationId, String status, String message, Map<String, Object> resources) {
        try {
            com.integrixs.data.model.SystemLog log = new com.integrixs.data.model.SystemLog();
            log.setComponent("PACKAGE_CREATION");
            log.setAction("CREATE_PACKAGE");
            // Map status to LogLevel
            if ("SUCCESS".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            } else if ("ERROR".equals(status) || "FAILURE".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.ERROR);
            } else {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            }
            log.setMessage(message);
            log.setCorrelationId(correlationId.toString());
            log.setTimestamp(java.time.LocalDateTime.now());

            // Add details
            Map<String, Object> details = new HashMap<>();
            details.put("correlationId", correlationId);
            details.put("status", status);
            details.put("resourceCount", resources.size());
            details.put("resourceTypes", resources.keySet());

            // Add resource IDs
            Map<String, String> resourceIds = new HashMap<>();
            resources.forEach((key, value) -> {
                if(value != null) {
                    try {
                        // Extract ID if possible
                        var idField = value.getClass().getDeclaredField("id");
                        idField.setAccessible(true);
                        Object id = idField.get(value);
                        if(id != null) {
                            resourceIds.put(key, id.toString());
                        }
                    } catch(Exception e) {
                        // Ignore if can't get ID
                    }
                }
            });
            details.put("resourceIds", resourceIds);

            log.setDetails(objectMapper.writeValueAsString(details));
            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }

    /**
     * Log adapter execution audit event (from service version)
     */
    @Async
    public void logAdapterExecution(UUID adapterId, String adapterName, String direction,
                                  String status, String message, long duration) {
        try {
            com.integrixs.data.model.SystemLog log = new com.integrixs.data.model.SystemLog();
            log.setComponent("ADAPTER");
            log.setAction("EXECUTE_ADAPTER");
            if ("SUCCESS".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            } else if ("ERROR".equals(status) || "FAILURE".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.ERROR);
            } else {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            }
            log.setMessage(message);
            log.setTimestamp(java.time.LocalDateTime.now());

            Map<String, Object> details = new HashMap<>();
            details.put("adapterId", adapterId);
            details.put("adapterName", adapterName);
            details.put("direction", direction);
            details.put("duration", duration);

            log.setDetails(objectMapper.writeValueAsString(details));
            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create adapter execution audit log", e);
        }
    }

    /**
     * Log transformation execution audit event (from service version)
     */
    @Async
    public void logTransformationExecution(String transformationId, String status,
                                         String message, Map<String, Object> metrics) {
        try {
            com.integrixs.data.model.SystemLog log = new com.integrixs.data.model.SystemLog();
            log.setComponent("TRANSFORMATION");
            log.setAction("EXECUTE_TRANSFORMATION");
            if ("SUCCESS".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            } else if ("ERROR".equals(status) || "FAILURE".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.ERROR);
            } else {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            }
            log.setMessage(message);
            log.setTimestamp(java.time.LocalDateTime.now());

            Map<String, Object> details = new HashMap<>();
            details.put("transformationId", transformationId);
            if(metrics != null) {
                details.putAll(metrics);
            }

            log.setDetails(objectMapper.writeValueAsString(details));
            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create transformation execution audit log", e);
        }
    }

    /**
     * Log error event (from service version)
     */
    @Async
    public void logError(String module, String action, String errorMessage,
                       String stackTrace, Map<String, Object> context) {
        try {
            com.integrixs.data.model.SystemLog log = new com.integrixs.data.model.SystemLog();
            log.setComponent(module);
            log.setAction(action);
            log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.ERROR);
            log.setMessage(errorMessage);
            log.setTimestamp(java.time.LocalDateTime.now());

            Map<String, Object> details = new HashMap<>();
            details.put("errorMessage", errorMessage);
            if(stackTrace != null) {
                details.put("stackTrace", stackTrace);
            }
            if(context != null) {
                details.put("context", context);
            }

            log.setDetails(objectMapper.writeValueAsString(details));
            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create error audit log", e);
        }
    }

    /**
     * Log security event (compatibility method for service version)
     */
    @Async
    public void logSecurityEvent(String eventType, String userId, String status,
                                String message, Map<String, Object> context) {
        try {
            com.integrixs.data.model.SystemLog log = new com.integrixs.data.model.SystemLog();
            log.setComponent("SECURITY");
            log.setAction(eventType);
            if ("SUCCESS".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            } else if ("ERROR".equals(status) || "FAILURE".equals(status)) {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.ERROR);
            } else {
                log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            }
            log.setMessage(message);
            if (userId != null && !"SYSTEM".equals(userId)) {
                try {
                    log.setUserId(UUID.fromString(userId));
                } catch (IllegalArgumentException e) {
                    // Skip if not a valid UUID
                }
            }
            log.setTimestamp(java.time.LocalDateTime.now());

            if(context != null) {
                log.setDetails(objectMapper.writeValueAsString(context));
            }

            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create security audit log", e);
        }
    }

    /**
     * Log performance metrics (from service version)
     */
    @Async
    public void logPerformanceMetrics(String operation, long duration,
                                    Map<String, Object> metrics) {
        try {
            com.integrixs.data.model.SystemLog log = new com.integrixs.data.model.SystemLog();
            log.setComponent("PERFORMANCE");
            log.setAction(operation);
            log.setLevel(com.integrixs.data.model.SystemLog.LogLevel.INFO);
            log.setMessage(String.format("Operation completed in %d ms", duration));
            log.setTimestamp(java.time.LocalDateTime.now());

            Map<String, Object> details = new HashMap<>();
            details.put("duration", duration);
            if(metrics != null) {
                details.putAll(metrics);
            }

            log.setDetails(objectMapper.writeValueAsString(details));
            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create performance audit log", e);
        }
    }

    /**
     * Enrich event with security context
     */
    private void enrichWithSecurityContext(AuditEvent event) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated()) {
            event.setUsername(auth.getName());
            // Extract user ID if available
            if(auth.getPrincipal() instanceof SecurityUtils.UserPrincipal) {
                SecurityUtils.UserPrincipal principal =
                    (SecurityUtils.UserPrincipal) auth.getPrincipal();
                event.setUserId(principal.getUserId());
            }
        }

        // Set tenant context
        String tenantId = TenantContext.getCurrentTenant();
        if(tenantId != null) {
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
            if(attrs != null) {
                HttpServletRequest request = attrs.getRequest();

                // Set request details
                event.setIpAddress(getClientIpAddress(request));
                event.setUserAgent(request.getHeader("User - Agent"));
                event.setSessionId(request.getSession(false) != null ?
                    request.getSession().getId() : null);
                event.setRequestId(request.getHeader("X - Request - ID"));

                // Set API endpoint if not already set
                if(event.getApiEndpoint() == null) {
                    event.setApiEndpoint(request.getRequestURI());
                }
                if(event.getHttpMethod() == null) {
                    event.setHttpMethod(request.getMethod());
                }
            }
        } catch(Exception e) {
            logger.debug("Could not enrich audit event with request context", e);
        }

        // Set MDC values
        event.setCorrelationId(MDC.get("correlationId"));
        if(event.getRequestId() == null) {
            event.setRequestId(MDC.get("requestId"));
        }
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X - Forwarded - For");
        if(xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X - Real - IP");
        if(xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * Mask sensitive data
     */
    private String maskValue(String value) {
        if(value == null || value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    /**
     * Publish audit event for real - time monitoring
     */
    private void publishEvent(AuditEvent event) {
        try {
            eventPublisher.publishEvent(new AuditEventNotification(event));
        } catch(Exception e) {
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

            Map<String, String> finalDetails = new HashMap<>(details);
            finalDetails.put("durationMs", String.valueOf(duration.toMillis()));

            AuditEvent event = AuditEvent.baseBuilder(AuditEvent.AuditEventType.SYSTEM_START)
                .action(action)
                .outcome(AuditEvent.AuditOutcome.SUCCESS)
                .details(convertMapToJson(finalDetails))
                .build();

            enrichWithSecurityContext(event);
            enrichWithRequestContext(event);

            if(asyncEnabled) {
                CompletableFuture.runAsync(() -> auditRepository.save(event));
            } else {
                auditRepository.save(event);
            }
        }
    }

    /**
     * Convert Map to JSON string
     */
    private String convertMapToJson(Map<String, ?> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            logger.error("Failed to convert map to JSON", e);
            return map.toString();
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
