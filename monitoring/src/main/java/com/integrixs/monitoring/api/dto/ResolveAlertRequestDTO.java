package com.integrixs.monitoring.api.dto;

import lombok.Data;

/**
 * DTO for resolve alert request
 */
@Data
public class ResolveAlertRequestDTO {
    private String resolution;
    private String resolvedBy;
}
