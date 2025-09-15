package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for record metric response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordMetricResponseDTO {
    private boolean success;
    private String metricId;
    private LocalDateTime timestamp;
    private int alertsTriggered;
    private String errorMessage;
}
