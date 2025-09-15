package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for alert
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO {
    private String alertId;
    private String alertName;
    private String alertType;
    private String severity;
    private String status;
    private String source;
    private String message;
    private String condition;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private String domainType;
    private String domainReferenceId;
    private Map<String, Object> metadata;
}
