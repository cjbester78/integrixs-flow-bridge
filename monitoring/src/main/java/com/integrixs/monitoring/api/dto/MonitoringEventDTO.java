package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for monitoring event
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringEventDTO {
    private String eventId;
    private String eventType;
    private String level;
    private String source;
    private String message;
    private LocalDateTime timestamp;
    private String userId;
    private String correlationId;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata;
    private String stackTrace;
}