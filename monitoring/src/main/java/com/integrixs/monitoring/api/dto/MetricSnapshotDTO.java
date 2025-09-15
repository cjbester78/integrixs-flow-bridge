package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for metric snapshot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricSnapshotDTO {
    private String metricId;
    private String metricName;
    private String metricType;
    private double value;
    private String unit;
    private LocalDateTime timestamp;
    private String source;
    private String domainType;
    private String domainReferenceId;
    private Map<String, String> tags;
    private Map<String, Object> dimensions;
}
