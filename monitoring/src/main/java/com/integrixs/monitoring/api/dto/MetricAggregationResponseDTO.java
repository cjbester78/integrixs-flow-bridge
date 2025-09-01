package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for metric aggregation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricAggregationResponseDTO {
    private boolean success;
    private String metricName;
    private String aggregationType;
    private double value;
    private Long startTime;
    private Long endTime;
    private String errorMessage;
}