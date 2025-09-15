package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO for orchestration execution details
 */
@Data
@NoArgsConstructor
public class OrchestrationExecutionDTO {
    private String executionId;
    private String flowId;
    private String flowName;
    private String status;
    private String currentStep;
    private String currentStepDisplay;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration; // milliseconds
    private Object inputData;
    private Object outputData;
    private List<String> logs;
    private Map<String, Object> metadata = new HashMap<>();
    private boolean inProgress;
    private boolean complete;
}
