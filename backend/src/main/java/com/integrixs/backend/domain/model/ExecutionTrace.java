package com.integrixs.backend.domain.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for flow execution trace
 */
@Data
public class ExecutionTrace {
    
    private String executionId;
    private String flowId;
    private String flowType;
    private ExecutionStatus status;
    private String currentStep;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdate;
    private long executionDurationMs;
    private String completionMessage;
    private String errorMessage;
    private String exceptionDetails;
    private List<TraceEvent> events = new ArrayList<>();
    
    /**
     * Adds a new event to the trace
     */
    public void addEvent(TraceEvent event) {
        events.add(event);
    }
}