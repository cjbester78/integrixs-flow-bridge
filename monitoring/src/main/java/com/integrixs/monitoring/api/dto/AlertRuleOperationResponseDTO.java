package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for alert rule operation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRuleOperationResponseDTO {
    private boolean success;
    private String ruleId;
    private String message;
    private String errorMessage;
}
