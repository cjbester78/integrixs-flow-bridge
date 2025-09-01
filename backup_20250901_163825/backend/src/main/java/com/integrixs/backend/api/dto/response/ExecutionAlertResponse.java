package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for execution alert
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionAlertResponse {
    
    private String type;
    private String severity;
    private String executionId;
    private String flowId;
    private String flowName;
    private String message;
    private LocalDateTime timestamp;
}