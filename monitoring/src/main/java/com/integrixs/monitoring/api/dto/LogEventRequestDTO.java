package com.integrixs.monitoring.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for log event request
 */
@Data
public class LogEventRequestDTO {
    private String eventType;
    private String level;
    private String source;
    private String message;
    private String userId;
    private String correlationId;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata;
    private String stackTrace;
}