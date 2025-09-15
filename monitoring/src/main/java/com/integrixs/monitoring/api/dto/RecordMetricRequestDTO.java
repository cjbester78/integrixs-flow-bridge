package com.integrixs.monitoring.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for record metric request
 */
@Data
public class RecordMetricRequestDTO {
    private String metricName;
    private String metricType;
    private double value;
    private String unit;
    private String source;
    private String domainType;
    private String domainReferenceId;
    private Map<String, String> tags;
    private Map<String, Object> dimensions;
}
