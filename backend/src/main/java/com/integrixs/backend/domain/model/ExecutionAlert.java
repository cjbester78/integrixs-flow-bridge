package com.integrixs.backend.domain.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Domain model for execution alert
 */
@Data
@Builder
public class ExecutionAlert {
    private AlertType type;
    private String executionId;
    private String flowId;
    private String message;
    private LocalDateTime timestamp;
}
