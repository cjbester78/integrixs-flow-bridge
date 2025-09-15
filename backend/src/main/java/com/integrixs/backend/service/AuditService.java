package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.repository.SystemLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for audit logging and tracking
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private SystemLogRepository systemLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Log package creation audit event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPackageCreation(UUID correlationId, String status, String message, Map<String, Object> resources) {
        try {
            SystemLog log = new SystemLog();
            log.setModule("PACKAGE_CREATION");
            log.setAction("CREATE_PACKAGE");
            log.setStatus(status);
            log.setMessage(message);
            log.setCorrelationId(correlationId.toString());
            log.setTimestamp(LocalDateTime.now());

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
     * Log flow execution audit event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFlowExecution(UUID flowId, String executionId, String status, String message, Map<String, Object> details) {
        try {
            SystemLog log = new SystemLog();
            log.setModule("FLOW_ENGINE");
            log.setAction("EXECUTE_FLOW");
            log.setStatus(status);
            log.setMessage(message);
            log.setCorrelationId(executionId);
            log.setTimestamp(LocalDateTime.now());

            // Add flow details
            Map<String, Object> logDetails = new HashMap<>();
            logDetails.put("flowId", flowId);
            logDetails.put("executionId", executionId);
            logDetails.put("status", status);
            if(details != null) {
                logDetails.putAll(details);
            }

            log.setDetails(objectMapper.writeValueAsString(logDetails));

            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create flow execution audit log", e);
        }
    }

    /**
     * Log adapter execution audit event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAdapterExecution(UUID adapterId, String adapterName, String direction,
                                  String status, String message, long duration) {
        try {
            SystemLog log = new SystemLog();
            log.setModule("ADAPTER");
            log.setAction("EXECUTE_ADAPTER");
            log.setStatus(status);
            log.setMessage(message);
            log.setTimestamp(LocalDateTime.now());

            // Add adapter details
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
     * Log transformation execution audit event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logTransformationExecution(String transformationId, String status,
                                         String message, Map<String, Object> metrics) {
        try {
            SystemLog log = new SystemLog();
            log.setModule("TRANSFORMATION");
            log.setAction("EXECUTE_TRANSFORMATION");
            log.setStatus(status);
            log.setMessage(message);
            log.setTimestamp(LocalDateTime.now());

            // Add transformation details
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
     * Log security event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSecurityEvent(String eventType, String userId, String status,
                                String message, Map<String, Object> context) {
        try {
            SystemLog log = new SystemLog();
            log.setModule("SECURITY");
            log.setAction(eventType);
            log.setStatus(status);
            log.setMessage(message);
            log.setUserId(userId);
            log.setTimestamp(LocalDateTime.now());

            if(context != null) {
                log.setDetails(objectMapper.writeValueAsString(context));
            }

            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create security audit log", e);
        }
    }

    /**
     * Log configuration change
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logConfigurationChange(String resourceType, UUID resourceId, String userId,
                                     String action, Map<String, Object> oldConfig,
                                     Map<String, Object> newConfig) {
        try {
            SystemLog log = new SystemLog();
            log.setModule("CONFIGURATION");
            log.setAction(action);
            log.setStatus("SUCCESS");
            log.setMessage(String.format("%s %s configuration updated", resourceType, resourceId));
            log.setUserId(userId);
            log.setTimestamp(LocalDateTime.now());

            // Add configuration details
            Map<String, Object> details = new HashMap<>();
            details.put("resourceType", resourceType);
            details.put("resourceId", resourceId);
            details.put("oldConfig", oldConfig);
            details.put("newConfig", newConfig);

            // Calculate changes
            Map<String, Object> changes = new HashMap<>();
            if(oldConfig != null && newConfig != null) {
                newConfig.forEach((key, value) -> {
                    Object oldValue = oldConfig.get(key);
                    if(!Objects.equals(oldValue, value)) {
                        changes.put(key, Map.of("old", oldValue, "new", value));
                    }
                });
            }
            details.put("changes", changes);

            log.setDetails(objectMapper.writeValueAsString(details));

            systemLogRepository.save(log);

        } catch(Exception e) {
            logger.error("Failed to create configuration change audit log", e);
        }
    }

    /**
     * Log error event
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(String module, String action, String errorMessage,
                       String stackTrace, Map<String, Object> context) {
        try {
            SystemLog log = new SystemLog();
            log.setModule(module);
            log.setAction(action);
            log.setStatus("ERROR");
            log.setMessage(errorMessage);
            log.setTimestamp(LocalDateTime.now());

            // Add error details
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
     * Log performance metrics
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logPerformanceMetrics(String operation, long duration,
                                    Map<String, Object> metrics) {
        try {
            SystemLog log = new SystemLog();
            log.setModule("PERFORMANCE");
            log.setAction(operation);
            log.setStatus("SUCCESS");
            log.setMessage(String.format("Operation completed in %d ms", duration));
            log.setTimestamp(LocalDateTime.now());

            // Add performance details
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

    private static class Objects {
        public static boolean equals(Object a, Object b) {
            return(a == b) || (a != null && a.equals(b));
        }
    }
}
