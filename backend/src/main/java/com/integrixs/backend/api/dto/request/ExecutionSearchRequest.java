package com.integrixs.backend.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for searching flow executions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionSearchRequest {

    @Size(max = 36, message = "Flow ID must be a valid UUID")
    private String flowId;

    private String status; // STARTED, RUNNING, COMPLETED, FAILED, ERROR, CANCELLED

    @JsonFormat(pattern = "yyyy - MM - dd'T'HH:mm:ss")
    private LocalDateTime startTimeAfter;

    @JsonFormat(pattern = "yyyy - MM - dd'T'HH:mm:ss")
    private LocalDateTime startTimeBefore;

    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 1000, message = "Limit cannot exceed 1000")
    @Builder.Default
    private int limit = 100;
}
