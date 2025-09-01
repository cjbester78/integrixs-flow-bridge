package com.integrixs.backend.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.domain.repository.SystemLogRepository;
import com.integrixs.backend.domain.service.LogManagementService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SystemLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Application service for log management
 * Orchestrates logging operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LogManagementApplicationService {
    
    private final SystemLogRepository systemLogRepository;
    private final LogManagementService logManagementService;
    private final ObjectMapper objectMapper;
    
    /**
     * Log a system event
     */
    @Transactional
    public void log(SystemLog log) {
        try {
            // Set defaults if not provided
            if (log.getId() == null) {
                log.setId(UUID.randomUUID());
            }
            if (log.getTimestamp() == null) {
                log.setTimestamp(java.time.LocalDateTime.now());
            }
            if (log.getCreatedAt() == null) {
                log.setCreatedAt(java.time.LocalDateTime.now());
            }
            
            // Validate
            logManagementService.validateLog(log);
            
            // Save
            systemLogRepository.save(log);
            
            LogManagementApplicationService.log.debug("Logged system event: {} - {}", log.getLevel(), log.getMessage());
            
        } catch (Exception e) {
            LogManagementApplicationService.log.error("Failed to save system log: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log user management error
     */
    @Async
    @Transactional
    public void logUserManagementError(String action, String message, String detailsJson, String userId, String controller) {
        try {
            UUID userUuid = userId != null ? UUID.fromString(userId) : null;
            
            SystemLog log = logManagementService.createUserManagementErrorLog(
                action, message, detailsJson, userUuid, controller
            );
            
            systemLogRepository.save(log);
            
            LogManagementApplicationService.log.error("User management error logged: {} - {}", action, message);
            
        } catch (Exception e) {
            LogManagementApplicationService.log.error("Failed to log user management error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log flow execution success
     */
    @Async
    @Transactional
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
            
        } catch (Exception e) {
            LogManagementApplicationService.log.error("Failed to log flow execution success: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Log flow execution error
     */
    @Async
    @Transactional
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
            
        } catch (Exception e) {
            LogManagementApplicationService.log.error("Failed to log flow execution error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create a system log entry
     */
    @Transactional
    public void createSystemLog(SystemLog.LogLevel level, String message, String source, 
                               String details, UUID userId, String domainType, String domainReferenceId) {
        try {
            SystemLog log = logManagementService.createLog(level, message, source, details, userId, domainType, domainReferenceId);
            systemLogRepository.save(log);
            
            LogManagementApplicationService.log.debug("System log created: {} - {}", level, message);
            
        } catch (Exception e) {
            LogManagementApplicationService.log.error("Failed to create system log: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Convert object to JSON string
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            LogManagementApplicationService.log.error("Failed to serialize to JSON", e);
            return "{\"error\": \"Failed to serialize details\"}";
        }
    }
}