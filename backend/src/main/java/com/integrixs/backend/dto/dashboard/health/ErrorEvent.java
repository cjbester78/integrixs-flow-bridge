package com.integrixs.backend.dto.dashboard.health;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Error event in adapter history.
 */
@Data
public class ErrorEvent {
    private LocalDateTime timestamp;
    private String errorType;
    private String message;
    private String impact; // HIGH, MEDIUM, LOW
}
