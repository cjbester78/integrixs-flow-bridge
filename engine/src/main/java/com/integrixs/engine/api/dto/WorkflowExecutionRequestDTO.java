package com.integrixs.engine.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for workflow execution requests
 */
@Data
@NoArgsConstructor
public class WorkflowExecutionRequestDTO {
    private String flowId;
    private Object inputData;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, Object> initialVariables = new HashMap<>();
    private String correlationId;
    private boolean async = false;
    private Integer timeout; // milliseconds
    private String initiatedBy;
}
