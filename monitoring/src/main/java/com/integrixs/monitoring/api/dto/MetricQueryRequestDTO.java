package com.integrixs.monitoring.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for metric query request
 */
@Data
public class MetricQueryRequestDTO {
    private String metricName;
    private String metricType;
    private Map<String, String> tags;
    private Long startTime;
    private Long endTime;
    private Integer limit;
    private String orderBy;
}
