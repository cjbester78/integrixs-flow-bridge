package com.integrixs.monitoring.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for log event response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogEventResponseDTO {
    private boolean success;
    private String eventId;
    private LocalDateTime timestamp;
    private int alertsTriggered;
    private String errorMessage;
}