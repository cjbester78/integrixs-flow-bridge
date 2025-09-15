package com.integrixs.monitoring.api.dto;

import lombok.Data;

import java.util.Map;

/**
 * DTO for alert action
 */
@Data
public class AlertActionDTO {
    private String type;
    private Map<String, String> parameters;
}
