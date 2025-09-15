package com.integrixs.engine.api.dto;

import com.integrixs.engine.domain.model.WorkflowStep;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for workflow execution responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowExecutionResponseDTO {
    private String workflowId;
    private String flowId;
    private String executionId;
    private String state;
    private boolean success;
    @Builder.Default
    private List<WorkflowStep> steps = new ArrayList<>();
    private WorkflowStep currentStep;
    private Object outputData;
    private String errorMessage;
    @Builder.Default
    private Map<String, Object> globalVariables = new HashMap<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private String correlationId;
    private Long startTime;
    private Long endTime;
    private Long executionTimeMs;
    private String initiatedBy;
}
