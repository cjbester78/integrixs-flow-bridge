package com.integrixs.engine.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for workflow events
 * Used for audit and monitoring of workflow execution
 */
@Data
@Builder
public class WorkflowEvent {
    private String eventId;
    private String workflowId;
    private String flowId;
    private EventType eventType;
    private String eventName;
    private String description;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    private String stepId;
    private String stepName;
    @Builder.Default
    private Map<String, Object> eventData = new HashMap<>();
    private String userId;
    private String source;
    
    /**
     * Event types
     */
    public enum EventType {
        WORKFLOW_STARTED,
        WORKFLOW_COMPLETED,
        WORKFLOW_FAILED,
        WORKFLOW_CANCELLED,
        WORKFLOW_SUSPENDED,
        WORKFLOW_RESUMED,
        STEP_STARTED,
        STEP_COMPLETED,
        STEP_FAILED,
        STEP_RETRY,
        VALIDATION_PASSED,
        VALIDATION_FAILED,
        ERROR_OCCURRED,
        WARNING_RAISED,
        CUSTOM
    }
    
    /**
     * Add event data
     * @param key Data key
     * @param value Data value
     */
    public void addEventData(String key, Object value) {
        this.eventData.put(key, value);
    }
}