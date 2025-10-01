package com.integrixs.backend.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.domain.service.LogManagementService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for log management
 * Orchestrates logging operations
 */
@Service
public class LogManagementApplicationService {

    private static final Logger log = LoggerFactory.getLogger(LogManagementApplicationService.class);


    private final SystemLogSqlRepository systemLogRepository;
    private final LogManagementService logManagementService;
    private final ObjectMapper objectMapper;

    public LogManagementApplicationService(SystemLogSqlRepository systemLogRepository,
                                           LogManagementService logManagementService,
                                           ObjectMapper objectMapper) {
        this.systemLogRepository = systemLogRepository;
        this.logManagementService = logManagementService;
        this.objectMapper = objectMapper;
    }

    /**
     * Log a system event
     */
    public void log(SystemLog log) {
        try {
            // Set defaults if not provided
            if(log.getId() == null) {
                log.setId(UUID.randomUUID());
            }
            if(log.getTimestamp() == null) {
                log.setTimestamp(java.time.LocalDateTime.now());
            }
            if(log.getCreatedAt() == null) {
                log.setCreatedAt(java.time.LocalDateTime.now());
            }

            // Validate
            logManagementService.validateLog(log);

            // Save
            systemLogRepository.save(log);

            LogManagementApplicationService.log.debug("Logged system event: {} - {}", log.getLevel(), log.getMessage());

        } catch(Exception e) {
            LogManagementApplicationService.log.error("Failed to save system log: {}", e.getMessage(), e);
        }
    }

    /**
     * Log user management error
     */
    @Async
    public void logUserManagementError(String action, String message, String detailsJson, String userId, String controller) {
        try {
            UUID userUuid = userId != null ? UUID.fromString(userId) : null;

            SystemLog log = logManagementService.createUserManagementErrorLog(
                action, message, detailsJson, userUuid, controller
           );

            systemLogRepository.save(log);

            LogManagementApplicationService.log.error("User management error logged: {} - {}", action, message);

        } catch(Exception e) {
            LogManagementApplicationService.log.error("Failed to log user management error: {}", e.getMessage(), e);
        }
    }

    /**
     * Log flow execution success
     */
    @Async
    public void logFlowExecutionSuccess(IntegrationFlow flow, String inputData, String outputData) {
        try {
            // Build details
            Map<String, Object> details = logManagementService.buildSuccessDetails(inputData, outputData);
            String detailsJson = toJson(details);

            // Create log
            SystemLog log = logManagementService.createFlowExecutionSuccessLog(flow, inputData, outputData, detailsJson);

            // Save
            systemLogRepository.save(log);

            LogManagementApplicationService.log.info("Flow execution success logged: {}", flow.getName());

        } catch(Exception e) {
            LogManagementApplicationService.log.error("Failed to log flow execution success: {}", e.getMessage(), e);
        }
    }

    /**
     * Log flow execution error
     */
    @Async
    public void logFlowExecutionError(IntegrationFlow flow, Exception exception) {
        try {
            // Build details
            Map<String, Object> details = logManagementService.buildErrorDetails(exception);
            String detailsJson = toJson(details);

            // Create log
            SystemLog log = logManagementService.createFlowExecutionErrorLog(flow, exception, detailsJson);

            // Save
            systemLogRepository.save(log);

            LogManagementApplicationService.log.error("Flow execution error logged: {} - {}", flow.getName(), exception.getMessage());

        } catch(Exception e) {
            LogManagementApplicationService.log.error("Failed to log flow execution error: {}", e.getMessage(), e);
        }
    }

    /**
     * Create a system log entry
     */
    public void createSystemLog(SystemLog.LogLevel level, String message, String source,
                               String details, UUID userId, String domainType, String domainReferenceId) {
        try {
            SystemLog log = logManagementService.createLog(level, message, source, details, userId, domainType, domainReferenceId);
            systemLogRepository.save(log);

            LogManagementApplicationService.log.debug("System log created: {} - {}", level, message);

        } catch(Exception e) {
            LogManagementApplicationService.log.error("Failed to create system log: {}", e.getMessage(), e);
        }
    }

    /**
     * Convert object to JSON string
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch(Exception e) {
            LogManagementApplicationService.log.error("Failed to serialize to JSON", e);
            return " {\"error\": \"Failed to serialize details\"}";
        }
    }
}
