package com.integrixs.backend.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for orchestration execution results
 */
public class OrchestrationDTO {
    private boolean success;
    private String executionId;
    private Object data;
    private String message;
    private List<String> logs = new ArrayList<>();
    private long duration; // milliseconds

    public static OrchestrationDTO success(String executionId, Object data) {
        OrchestrationDTO dto = new OrchestrationDTO();
        dto.setSuccess(true);
        dto.setExecutionId(executionId);
        dto.setData(data);
        return dto;
    }

    public static OrchestrationDTO error(String message) {
        OrchestrationDTO dto = new OrchestrationDTO();
        dto.setSuccess(false);
        dto.setMessage(message);
        return dto;
    }

    // Default constructor
    public OrchestrationDTO() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
