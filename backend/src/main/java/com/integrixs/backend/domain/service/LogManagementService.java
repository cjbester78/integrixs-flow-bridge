package com.integrixs.backend.domain.service;

import com.integrixs.data.model.SystemLog;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for system log management
 * Contains core logging business logic
 */
@Service
public class LogManagementService {

    /**
     * Create a basic log entry
     */

    private static final Logger log = LoggerFactory.getLogger(LogManagementService.class);

    public SystemLog createLog(SystemLog.LogLevel level, String message, String source,
                              String details, UUID userId, String domainType, String domainReferenceId) {
        SystemLog log = new SystemLog();
        log.setId(UUID.randomUUID());
        log.setTimestamp(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        log.setLevel(level);
        log.setMessage(message);
        log.setSource(source);
        log.setDetails(details);
        log.setUserId(userId);
        log.setDomainType(domainType);
        log.setDomainReferenceId(domainReferenceId);

        return log;
    }

    /**
     * Create a user management error log
     */
    public SystemLog createUserManagementErrorLog(String action, String message, String detailsJson,
                                                 UUID userId, String controller) {
        return createLog(
            SystemLog.LogLevel.ERROR,
            message,
            controller,
            detailsJson,
            userId,
            "UserManagement",
            action
       );
    }

    /**
     * Create flow execution success log
     */
    public SystemLog createFlowExecutionSuccessLog(IntegrationFlow flow, String inputData, String outputData, String detailsJson) {
        String message = "Flow executed successfully: " + flow.getName();

        return createLog(
            SystemLog.LogLevel.INFO,
            message,
            "FlowExecution",
            detailsJson,
            null,
            "INTEGRATION_FLOW",
            flow.getId().toString()
       );
    }

    /**
     * Create flow execution error log
     */
    public SystemLog createFlowExecutionErrorLog(IntegrationFlow flow, Exception e, String detailsJson) {
        String message = "Flow execution failed: " + flow.getName();

        return createLog(
            SystemLog.LogLevel.ERROR,
            message,
            "FlowExecution",
            detailsJson,
            null,
            "INTEGRATION_FLOW",
            flow.getId().toString()
       );
    }

    /**
     * Build details map for flow execution success
     */
    public Map<String, Object> buildSuccessDetails(String inputData, String outputData) {
        Map<String, Object> details = new HashMap<>();
        details.put("input", inputData);
        details.put("output", outputData);
        details.put("timestamp", LocalDateTime.now());

        return details;
    }

    /**
     * Build details map for flow execution error
     */
    public Map<String, Object> buildErrorDetails(Exception e) {
        Map<String, Object> details = new HashMap<>();
        details.put("exception", e.getClass().getName());
        details.put("message", e.getMessage());
        details.put("stackTrace", getStackTraceAsString(e));
        details.put("timestamp", LocalDateTime.now());

        return details;
    }

    /**
     * Convert exception stack trace to string
     */
    private String getStackTraceAsString(Exception e) {
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Validate log entry
     */
    public void validateLog(SystemLog log) {
        if(log.getLevel() == null) {
            throw new IllegalArgumentException("Log level is required");
        }

        if(log.getMessage() == null || log.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Log message is required");
        }

        if(log.getSource() == null || log.getSource().trim().isEmpty()) {
            throw new IllegalArgumentException("Log source is required");
        }
    }
}
