package com.integrixs.monitoring.api.dto;

import lombok.Data;

/**
 * DTO for acknowledge alert request
 */
@Data
public class AcknowledgeAlertRequestDTO {
    private String userId;
    private String comment;
}