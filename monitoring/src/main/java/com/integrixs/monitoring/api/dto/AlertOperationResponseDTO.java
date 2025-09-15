package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for alert operation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertOperationResponseDTO {
    private boolean success;
    private String alertId;
    private String status;
    private String message;
    private String errorMessage;
}
