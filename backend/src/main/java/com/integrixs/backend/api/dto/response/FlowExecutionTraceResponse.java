package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for flow execution trace
 */
public class FlowExecutionTraceResponse {

    private String executionId;
    private String flowId;
    private String flowName;
    private String flowType;
    private String status;
    private String currentStep;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime lastUpdate;
    private Long executionDurationMs;
    private String completionMessage;
    private String errorMessage;
    private List<TraceEventResponse> events;

    public static class TraceEventResponse {
        private String eventType;
        private String message;
        private LocalDateTime timestamp;

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public static TraceEventResponseBuilder builder() {
            return new TraceEventResponseBuilder();
        }

        public static class TraceEventResponseBuilder {
            private TraceEventResponse response = new TraceEventResponse();

            public TraceEventResponseBuilder eventType(String eventType) {
                response.setEventType(eventType);
                return this;
            }

            public TraceEventResponseBuilder message(String message) {
                response.setMessage(message);
                return this;
            }

            public TraceEventResponseBuilder timestamp(LocalDateTime timestamp) {
                response.setTimestamp(timestamp);
                return this;
            }

            public TraceEventResponse build() {
                return response;
            }
        }
    }

    // Default constructor
    public FlowExecutionTraceResponse() {
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getFlowType() {
        return flowType;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public String getCompletionMessage() {
        return completionMessage;
    }

    public void setCompletionMessage(String completionMessage) {
        this.completionMessage = completionMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<TraceEventResponse> getEvents() {
        return events;
    }

    public void setEvents(List<TraceEventResponse> events) {
        this.events = events;
    }

    public static FlowExecutionTraceResponseBuilder builder() {
        return new FlowExecutionTraceResponseBuilder();
    }

    public static class FlowExecutionTraceResponseBuilder {
        private FlowExecutionTraceResponse response = new FlowExecutionTraceResponse();

        public FlowExecutionTraceResponseBuilder executionId(String executionId) {
            response.setExecutionId(executionId);
            return this;
        }

        public FlowExecutionTraceResponseBuilder flowId(String flowId) {
            response.setFlowId(flowId);
            return this;
        }

        public FlowExecutionTraceResponseBuilder flowName(String flowName) {
            response.setFlowName(flowName);
            return this;
        }

        public FlowExecutionTraceResponseBuilder flowType(String flowType) {
            response.setFlowType(flowType);
            return this;
        }

        public FlowExecutionTraceResponseBuilder status(String status) {
            response.setStatus(status);
            return this;
        }

        public FlowExecutionTraceResponseBuilder currentStep(String currentStep) {
            response.setCurrentStep(currentStep);
            return this;
        }

        public FlowExecutionTraceResponseBuilder startTime(LocalDateTime startTime) {
            response.setStartTime(startTime);
            return this;
        }

        public FlowExecutionTraceResponseBuilder endTime(LocalDateTime endTime) {
            response.setEndTime(endTime);
            return this;
        }

        public FlowExecutionTraceResponseBuilder lastUpdate(LocalDateTime lastUpdate) {
            response.setLastUpdate(lastUpdate);
            return this;
        }

        public FlowExecutionTraceResponseBuilder executionDurationMs(Long executionDurationMs) {
            response.setExecutionDurationMs(executionDurationMs);
            return this;
        }

        public FlowExecutionTraceResponseBuilder completionMessage(String completionMessage) {
            response.setCompletionMessage(completionMessage);
            return this;
        }

        public FlowExecutionTraceResponseBuilder errorMessage(String errorMessage) {
            response.setErrorMessage(errorMessage);
            return this;
        }

        public FlowExecutionTraceResponseBuilder events(List<TraceEventResponse> events) {
            response.setEvents(events);
            return this;
        }

        public FlowExecutionTraceResponse build() {
            return response;
        }
    }
}
