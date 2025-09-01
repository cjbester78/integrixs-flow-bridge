package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Critical alert requiring immediate attention.
 */
@Data
public class CriticalAlert {
    private String adapterId;
    private String adapterName;
    private String severity; // CRITICAL, HIGH, MEDIUM
    private String message;
    private LocalDateTime timestamp;
    private String actionRequired;
}