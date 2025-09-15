package com.integrixs.monitoring.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing a monitoring event
 */
@Data
@Builder
public class MonitoringEvent {
    private String eventId;
    private EventType eventType;
    private EventLevel level;
    private String source;
    private String message;
    private LocalDateTime timestamp;
    private String userId;
    private String correlationId;
    private String domainType;
    private String domainReferenceId;
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private String stackTrace;

    /**
     * Event types
     */
    public enum EventType {
        SYSTEM_LOG,
        FLOW_EXECUTION,
        ADAPTER_OPERATION,
        USER_ACTIVITY,
        ERROR,
        PERFORMANCE,
        SECURITY,
        AUDIT
    }

    /**
     * Event levels
     */
    public enum EventLevel {
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }


    /**
     * Add metadata entry
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Check if event is error level or higher
     */
    public boolean isError() {
        return level == EventLevel.ERROR || level == EventLevel.CRITICAL;
    }

    /**
     * Check if event requires immediate attention
     */
    public boolean isCritical() {
        return level == EventLevel.CRITICAL;
    }
}
