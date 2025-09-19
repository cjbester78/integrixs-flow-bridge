package com.integrixs.backend.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * Request DTO for searching flow executions
 */
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
    private int limit = 100;

    // Default constructor
    public ExecutionSearchRequest() {
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTimeAfter() {
        return startTimeAfter;
    }

    public void setStartTimeAfter(LocalDateTime startTimeAfter) {
        this.startTimeAfter = startTimeAfter;
    }

    public LocalDateTime getStartTimeBefore() {
        return startTimeBefore;
    }

    public void setStartTimeBefore(LocalDateTime startTimeBefore) {
        this.startTimeBefore = startTimeBefore;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
