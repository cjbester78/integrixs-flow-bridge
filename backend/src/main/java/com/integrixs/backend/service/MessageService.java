package com.integrixs.backend.service;

import com.integrixs.data.model.*;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.data.sql.repository.AdapterPayloadSqlRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling message logging, processing steps, and adapter activity logging
 */
@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);


    private final SystemLogSqlRepository systemLogRepository;
    private final AdapterPayloadSqlRepository adapterPayloadRepository;

    public MessageService(SystemLogSqlRepository systemLogRepository,
                         AdapterPayloadSqlRepository adapterPayloadRepository) {
        this.systemLogRepository = systemLogRepository;
        this.adapterPayloadRepository = adapterPayloadRepository;
    }

    /**
     * Log a processing step for a flow execution
     */
    public void logProcessingStep(String correlationId, IntegrationFlow flow, String message,
                                  String details, SystemLog.LogLevel level) {
        try {
            SystemLog logEntry = SystemLog.builder()
                .timestamp(LocalDateTime.now())
                .level(level)
                .message(message)
                .details(details)
                .source("FLOW_EXECUTION")
                .sourceId(flow.getId().toString())
                .sourceName(flow.getName())
                .component("AdapterExecutionService")
                .domainType("IntegrationFlow")
                .domainReferenceId(flow.getId().toString())
                .correlationId(correlationId)
                .category("FLOW_PROCESSING")
                .build();

            systemLogRepository.save(logEntry);

            // Also log to application logs
            switch(level) {
                case ERROR:
                    log.error("[ {}] {} - {}", correlationId, message, details);
                    break;
                case WARN:
                    log.warn("[ {}] {} - {}", correlationId, message, details);
                    break;
                case INFO:
                    log.info("[ {}] {} - {}", correlationId, message, details);
                    break;
                default:
                    log.debug("[ {}] {} - {}", correlationId, message, details);
            }
        } catch(Exception e) {
            log.error("Failed to log processing step: {}", e.getMessage(), e);
        }
    }

    /**
     * Log adapter activity
     */
    public void logAdapterActivity(CommunicationAdapter adapter, String message, String details,
                                   SystemLog.LogLevel level, String correlationId) {
        try {
            SystemLog logEntry = SystemLog.builder()
                .timestamp(LocalDateTime.now())
                .level(level)
                .message(message)
                .details(details)
                .source("ADAPTER")
                .sourceId(adapter.getId().toString())
                .sourceName(adapter.getName())
                .component(adapter.getType().name())
                .domainType("CommunicationAdapter")
                .domainReferenceId(adapter.getId().toString())
                .correlationId(correlationId)
                .category("ADAPTER_ACTIVITY")
                .build();

            systemLogRepository.save(logEntry);

            // Also log to application logs
            String logMessage = String.format("[%s] Adapter %s(%s): %s - %s",
                correlationId, adapter.getName(), adapter.getType(), message, details);

            switch(level) {
                case ERROR:
                    log.error(logMessage);
                    break;
                case WARN:
                    log.warn(logMessage);
                    break;
                case INFO:
                    log.info(logMessage);
                    break;
                default:
                    log.debug(logMessage);
            }
        } catch(Exception e) {
            log.error("Failed to log adapter activity: {}", e.getMessage(), e);
        }
    }

    /**
     * Log adapter payload(request or response)
     */
    public void logAdapterPayload(String correlationId, CommunicationAdapter adapter,
                                  String payloadType, String payload, String direction) {
        try {
            // Don't log empty payloads
            if(payload == null || payload.isEmpty()) {
                return;
            }

            // Truncate large payloads for database storage(keep first 50KB)
            String storedPayload = payload;
            int maxPayloadSize = 50000; // 50KB
            if(payload.length() > maxPayloadSize) {
                storedPayload = payload.substring(0, maxPayloadSize) + "... [TRUNCATED]";
                log.warn("Payload truncated for storage. Original size: {} bytes", payload.length());
            }

            AdapterPayload adapterPayload = AdapterPayload.builder()
                .correlationId(correlationId)
                .adapterId(adapter.getId())
                .adapterName(adapter.getName())
                .adapterType(adapter.getType().name())
                .direction(direction)
                .payloadType(payloadType)
                .payload(storedPayload)
                .payloadSize(payload.length())
                .createdAt(LocalDateTime.now())
                .build();

            adapterPayloadRepository.save(adapterPayload);

            // Log summary to application logs
            log.debug("[ {}] Adapter payload logged - Adapter: {} ( {}), Direction: {}, Type: {}, Size: {} bytes",
                correlationId, adapter.getName(), adapter.getType(), direction, payloadType, payload.length());

        } catch(Exception e) {
            log.error("Failed to log adapter payload: {}", e.getMessage(), e);
        }
    }

    /**
     * Get adapter payloads for a correlation ID
     */
    public List<AdapterPayload> getAdapterPayloads(String correlationId) {
        return adapterPayloadRepository.findByCorrelationIdOrderByCreatedAt(correlationId);
    }

    /**
     * Get system logs for a correlation ID
     */
    public List<SystemLog> getSystemLogs(String correlationId) {
        return systemLogRepository.findByCorrelationIdOrderByTimestamp(correlationId);
    }

    /**
     * Clean up old logs and payloads
     */
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);

        try {
            // Delete old adapter payloads
            int deletedPayloads = adapterPayloadRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("Deleted {} old adapter payloads", deletedPayloads);

            // Delete old system logs
            int deletedLogs = systemLogRepository.deleteByTimestampBefore(cutoffDate);
            log.info("Deleted {} old system logs", deletedLogs);

        } catch(Exception e) {
            log.error("Error cleaning up old logs: {}", e.getMessage(), e);
        }
    }
}
