package com.integrixs.monitoring.api.dto;

import lombok.Data;

/**
 * DTO for event query request
 */
@Data
public class EventQueryRequestDTO {
    private String eventType;
    private String minLevel;
    private String source;
    private String userId;
    private String domainType;
    private String domainReferenceId;
    private String correlationId;
    private Long startTime;
    private Long endTime;
    private Integer limit;
}