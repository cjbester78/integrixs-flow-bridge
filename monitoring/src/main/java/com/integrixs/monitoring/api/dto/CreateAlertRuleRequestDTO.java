package com.integrixs.monitoring.api.dto;

import lombok.Data;

/**
 * DTO for create alert rule request
 */
@Data
public class CreateAlertRuleRequestDTO {
    private String ruleName;
    private String condition;
    private String alertType;
    private String severity;
    private boolean enabled;
    private int evaluationInterval;
    private String targetMetric;
    private double threshold;
    private String comparison;
    private AlertActionDTO action;
}
