package com.integrixs.monitoring.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for metric aggregation request
 */
@Data
public class MetricAggregationRequestDTO {
    private String metricName;
    private String aggregationType;
    private Long startTime;
    private Long endTime;
    private Map<String, String> tags;
}
