package com.integrixs.engine.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for workflow events
 * Used for audit and monitoring of workflow execution
 */
public class WorkflowEvent {
    private String eventId;
    private String workflowId;
    private String flowId;
    private EventType eventType;
    private String eventName;
    private String description;
    private LocalDateTime timestamp = LocalDateTime.now();
    private String stepId;
    private String stepName;
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

    // Default constructor
    public WorkflowEvent() {
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Map<String, Object> getEventData() {
        return eventData;
    }

    public void setEventData(Map<String, Object> eventData) {
        this.eventData = eventData;
    }

    // Builder
    public static WorkflowEventBuilder builder() {
        return new WorkflowEventBuilder();
    }

    public static class WorkflowEventBuilder {
        private String eventId;
        private String workflowId;
        private String flowId;
        private EventType eventType;
        private String eventName;
        private String description;
        private LocalDateTime timestamp;
        private String stepId;
        private String stepName;
        private Map<String, Object> eventData;
        private String userId;
        private String source;

        public WorkflowEventBuilder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public WorkflowEventBuilder workflowId(String workflowId) {
            this.workflowId = workflowId;
            return this;
        }

        public WorkflowEventBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public WorkflowEventBuilder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public WorkflowEventBuilder eventName(String eventName) {
            this.eventName = eventName;
            return this;
        }

        public WorkflowEventBuilder description(String description) {
            this.description = description;
            return this;
        }

        public WorkflowEventBuilder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public WorkflowEventBuilder stepId(String stepId) {
            this.stepId = stepId;
            return this;
        }

        public WorkflowEventBuilder stepName(String stepName) {
            this.stepName = stepName;
            return this;
        }

        public WorkflowEventBuilder eventData(Map<String, Object> eventData) {
            this.eventData = eventData;
            return this;
        }

        public WorkflowEventBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public WorkflowEventBuilder source(String source) {
            this.source = source;
            return this;
        }

        public WorkflowEvent build() {
            WorkflowEvent instance = new WorkflowEvent();
            instance.setEventId(this.eventId);
            instance.setWorkflowId(this.workflowId);
            instance.setFlowId(this.flowId);
            instance.setEventType(this.eventType);
            instance.setEventName(this.eventName);
            instance.setDescription(this.description);
            instance.setTimestamp(this.timestamp != null ? this.timestamp : LocalDateTime.now());
            instance.setStepId(this.stepId);
            instance.setStepName(this.stepName);
            instance.setEventData(this.eventData != null ? this.eventData : new HashMap<>());
            instance.setUserId(this.userId);
            instance.setSource(this.source);
            return instance;
        }
    }
}
