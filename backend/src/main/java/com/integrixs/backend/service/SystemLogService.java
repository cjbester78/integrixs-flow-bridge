package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.security.AuditLogEncryptionService;
import com.integrixs.data.model.SystemLog;
// Removed unused import: SystemLogSpecifications
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.shared.dto.log.FrontendLogBatchRequest;
import com.integrixs.shared.dto.log.FrontendLogEntry;
import com.integrixs.shared.dto.system.SystemLogDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing system logs including frontend application logs.
 * Handles log persistence, alerting, and configuration.
 */
@Service
public class SystemLogService {

    private static final Logger log = LoggerFactory.getLogger(SystemLogService.class);


    private final SystemLogSqlRepository systemLogRepository;
    private final UserSqlRepository userRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private AuditLogEncryptionService auditLogEncryptionService;

    public SystemLogService(SystemLogSqlRepository systemLogRepository,
                          UserSqlRepository userRepository,
                          ObjectMapper objectMapper) {
        this.systemLogRepository = systemLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Value("${logging.frontend.level:INFO}")
    private String frontendLogLevel;

    @Value("${logging.frontend.enabled-categories:ALL}")
    private String enabledCategories;

    @Value("${systemlog.encryption.enabled:false}")
    private boolean encryptionEnabled;

    /**
     * Log a frontend event to the system log.
     *
     * @param entry The frontend log entry
     */
    public void logFrontendEvent(FrontendLogEntry entry) {
        try {
            SystemLog systemLog = SystemLog.builder()
                    .timestamp(entry.getTimestamp() != null ? entry.getTimestamp() : LocalDateTime.now())
                    .level(mapLogLevel(entry.getLevel()))
                    .category("FRONTEND_" + entry.getCategory())
                    .message(entry.getMessage())
                    .source("FRONTEND")
                    .userId(entry.getUserId())
                    .username(entry.getUserId() != null ? getUsernameByUuid(entry.getUserId()) : null)
                    .ipAddress(entry.getClientIp())
                    .userAgent(entry.getUserAgent())
                    .correlationId(entry.getCorrelationId())
                    .sessionId(entry.getSessionId())
                    .details(convertDetailsToJson(entry))
                    .stackTrace(entry.getStackTrace())
                    .url(entry.getUrl())
                    .build();

            // Encrypt sensitive data if enabled
            if(encryptionEnabled && auditLogEncryptionService != null) {
                systemLog = auditLogEncryptionService.encryptSystemLog(systemLog);
            }

            systemLogRepository.save(systemLog);

            // Log to server logs as well for debugging
            if("ERROR".equals(entry.getLevel()) || "FATAL".equals(entry.getLevel())) {
                log.error("Frontend error: {}-{}", entry.getCategory(), entry.getMessage());
            } else if("WARN".equals(entry.getLevel())) {
                log.warn("Frontend warning: {}-{}", entry.getCategory(), entry.getMessage());
            } else if("DEBUG".equals(entry.getLevel())) {
                log.debug("Frontend debug: {}-{}", entry.getCategory(), entry.getMessage());
            }

        } catch(Exception e) {
            log.error("Failed to save frontend log entry: {}", e.getMessage(), e);
        }
    }

    /**
     * Process beacon logs asynchronously.
     *
     * @param beaconData The beacon data as string
     */
    @Async
    public CompletableFuture<Void> processBeaconLogs(String beaconData) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Parse the beacon data as JSON
                FrontendLogBatchRequest batch = objectMapper.readValue(beaconData, FrontendLogBatchRequest.class);

                // Process each log entry
                for(FrontendLogEntry entry : batch.getLogs()) {
                    logFrontendEvent(entry);
                }

            } catch(Exception e) {
                log.error("Failed to process beacon logs: {}", e.getMessage());
            }
        });
    }

    /**
     * Send error alert for critical frontend errors.
     *
     * @param entry The log entry that triggered the alert
     */
    @Async
    public void sendErrorAlert(FrontendLogEntry entry) {
        try {
            String subject = String.format("Frontend %s: %s", entry.getLevel(), entry.getCategory());
            String message = String.format(
                "Frontend error detected:\n\n" +
                "Level: %s\n" +
                "Category: %s\n" +
                "Message: %s\n" +
                "User: %s\n" +
                "URL: %s\n" +
                "Time: %s\n" +
                "Session: %s\n" +
                "Correlation ID: %s\n\n" +
                "Stack Trace:\n%s",
                entry.getLevel(),
                entry.getCategory(),
                entry.getMessage(),
                entry.getUserId(),
                entry.getUrl(),
                entry.getTimestamp(),
                entry.getSessionId(),
                entry.getCorrelationId(),
                entry.getStackTrace() != null ? entry.getStackTrace() : "N/A"
           );

            log.error("System alert: {}-{}", subject, message);

        } catch(Exception e) {
            log.error("Failed to send error alert: {}", e.getMessage());
        }
    }

    /**
     * Get the configured frontend log level.
     *
     * @return The log level
     */
    public String getFrontendLogLevel() {
        return frontendLogLevel;
    }

    /**
     * Get enabled log categories.
     *
     * @return List of enabled categories
     */
    public List<String> getEnabledCategories() {
        if("ALL".equals(enabledCategories)) {
            return Arrays.asList("AUTH", "API", "VALIDATION", "USER_ACTION",
                               "NAVIGATION", "PERFORMANCE", "ERROR", "SECURITY",
                               "BUSINESS_LOGIC", "UI", "SYSTEM");
        }
        return Arrays.asList(enabledCategories.split(","));
    }

    /**
     * Map frontend log level to system log level.
     *
     * @param frontendLevel The frontend log level
     * @return The system log level
     */
    private SystemLog.LogLevel mapLogLevel(String frontendLevel) {
        if(frontendLevel == null) {
            return SystemLog.LogLevel.INFO;
        }

        try {
            return SystemLog.LogLevel.valueOf(frontendLevel.toUpperCase());
        } catch(IllegalArgumentException e) {
            log.warn("Unknown frontend log level: {}", frontendLevel);
            return SystemLog.LogLevel.INFO;
        }
    }

    /**
     * Get username by user ID.
     *
     * @param userId The user ID
     * @return The username or null
     */
    private String getUsernameById(String userId) {
        if(userId == null) {
            return null;
        }

        try {
            Optional<User> user = userRepository.findById(UUID.fromString(userId));
            return user.map(User::getUsername).orElse(null);
        } catch(Exception e) {
            log.debug("Failed to get username for ID: {}", userId);
            return null;
        }
    }

    /**
     * Get username by user UUID.
     *
     * @param userId The user UUID
     * @return The username or null if not found
     */
    private String getUsernameByUuid(UUID userId) {
        if(userId == null) {
            return null;
        }

        try {
            Optional<User> user = userRepository.findById(userId);
            return user.map(User::getUsername).orElse(null);
        } catch(Exception e) {
            log.debug("Failed to get username for UUID: {}", userId);
            return null;
        }
    }

    /**
     * Convert log details to JSON string.
     *
     * @param entry The log entry
     * @return JSON string of details
     */
    private String convertDetailsToJson(FrontendLogEntry entry) {
        try {
            Map<String, Object> allDetails = new HashMap<>();

            if(entry.getDetails() != null) {
                allDetails.putAll(entry.getDetails());
            }

            if(entry.getError() != null) {
                allDetails.put("error", entry.getError());
            }

            allDetails.put("frontendTimestamp", entry.getTimestamp());
            allDetails.put("serverReceivedAt", entry.getServerReceivedAt());

            return objectMapper.writeValueAsString(allDetails);

        } catch(Exception e) {
            log.error("Failed to convert details to JSON: {}", e.getMessage());
            return " {}";
        }
    }

    /**
     * Query frontend logs.
     *
     * @param category The log category filter
     * @param level The log level filter
     * @param userId The user ID filter
     * @param startDate The start date filter
     * @param endDate The end date filter
     * @return List of system logs
     */
    public List<SystemLog> queryFrontendLogs(String category, String level, String userId,
                                            LocalDateTime startDate, LocalDateTime endDate) {
        // Build query based on filters
        final String finalCategory;
        if(category != null) {
            finalCategory = "FRONTEND_" + category;
        } else {
            finalCategory = null;
        }

        // Custom filtering implementation
        List<SystemLog> logs = systemLogRepository.findAll()
            .stream()
            .filter(log -> "FRONTEND".equals(log.getSource()))
            .filter(log -> finalCategory == null || finalCategory.equals(log.getCategory()))
            .filter(log -> level == null || SystemLog.LogLevel.valueOf(level).equals(log.getLevel()))
            .filter(log -> userId == null || (log.getUserId() != null && log.getUserId().toString().equals(userId)))
            .filter(log -> startDate == null || (log.getTimestamp() != null && !log.getTimestamp().isBefore(startDate)))
            .filter(log -> endDate == null || (log.getTimestamp() != null && !log.getTimestamp().isAfter(endDate)))
            .collect(Collectors.toList());

        // Decrypt sensitive data if enabled
        if(encryptionEnabled && auditLogEncryptionService != null) {
            return logs.stream()
                .map(log -> auditLogEncryptionService.decryptSystemLog(log))
                .collect(Collectors.toList());
        }

        return logs;
    }

    /**
     * Get frontend error statistics.
     *
     * @param hours Number of hours to look back
     * @return Map of statistics
     */
    public Map<String, Object> getFrontendErrorStats(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        List<SystemLog> errors = systemLogRepository.findBySourceAndLevelAndTimestampAfter(
            "FRONTEND",
            SystemLog.LogLevel.ERROR,
            since
       );

        // Decrypt sensitive data if enabled(for statistics, we might not need decryption)
        // But keeping for consistency
        if(encryptionEnabled && auditLogEncryptionService != null) {
            errors = errors.stream()
                .map(log -> auditLogEncryptionService.decryptSystemLog(log))
                .collect(Collectors.toList());
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalErrors", errors.size());
        stats.put("uniqueUsers", errors.stream().map(SystemLog::getUserId).distinct().count());
        stats.put("topCategories", getTopCategories(errors));
        stats.put("errorsByHour", groupErrorsByHour(errors));

        return stats;
    }

    /**
     * Get top error categories.
     *
     * @param logs The logs to analyze
     * @return Map of category counts
     */
    private Map<String, Long> getTopCategories(List<SystemLog> logs) {
        Map<String, Long> categoryCounts = new HashMap<>();

        for(SystemLog log : logs) {
            String category = log.getCategory().replace("FRONTEND_", "");
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0L) + 1);
        }

        return categoryCounts;
    }

    /**
     * Group errors by hour.
     *
     * @param logs The logs to analyze
     * @return Map of hourly counts
     */
    private Map<String, Long> groupErrorsByHour(List<SystemLog> logs) {
        Map<String, Long> hourlyCounts = new TreeMap<>();

        for(SystemLog log : logs) {
            String hour = log.getTimestamp().toLocalDate() + " " + log.getTimestamp().getHour() + ":00";
            hourlyCounts.put(hour, hourlyCounts.getOrDefault(hour, 0L) + 1);
        }

        return hourlyCounts;
    }

    /**
     * Get logs for a specific adapter.
     *
     * @param adapterId The adapter ID
     * @param pageable The pagination parameters
     * @return Page of system logs
     */
    public Page<SystemLogDTO> getAdapterLogs(String adapterId, Pageable pageable) {
        log.debug("Fetching logs for adapter: {}", adapterId);

        // Use the findByComponentId method for adapter logs
        Page<SystemLog> logs = systemLogRepository.findByComponentId(adapterId, pageable);

        // Decrypt sensitive data if enabled before converting to DTOs
        if(encryptionEnabled && auditLogEncryptionService != null) {
            List<SystemLog> decryptedLogs = logs.getContent().stream()
                .map(log -> auditLogEncryptionService.decryptSystemLog(log))
                .collect(Collectors.toList());
            logs = new org.springframework.data.domain.PageImpl<>(decryptedLogs, pageable, logs.getTotalElements());
        }

        // Convert to DTOs
        return logs.map(this::convertToDTO);
    }

    /**
     * Convert SystemLog entity to DTO.
     *
     * @param log The system log entity
     * @return The system log DTO
     */
    private SystemLogDTO convertToDTO(SystemLog log) {
        return SystemLogDTO.builder()
                .id(log.getId().toString())
                .timestamp(log.getTimestamp())
                .level(log.getLevel().toString())
                .message(log.getMessage())
                .details(log.getDetails())
                .source(log.getSource())
                .sourceId(log.getSourceId())
                .sourceName(log.getSourceName())
                .component(log.getComponent())
                .componentId(log.getComponentId())
                .domainType(log.getDomainType())
                .domainReferenceId(log.getDomainReferenceId())
                .correlationId(log.getCorrelationId())
                .userId(log.getUserId() != null ? log.getUserId().toString() : null)
                .clientIp(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
