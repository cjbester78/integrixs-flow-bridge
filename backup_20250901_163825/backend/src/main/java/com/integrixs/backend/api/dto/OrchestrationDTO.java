package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for orchestration execution results
 */
@Data
@NoArgsConstructor
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
}