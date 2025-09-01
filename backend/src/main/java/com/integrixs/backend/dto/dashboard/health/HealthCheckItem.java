package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

/**
 * Individual health check item.
 */
@Data
public class HealthCheckItem {
    private String checkName;
    private String status; // OK, WARNING, ERROR
    private String message;
    private String details;
}