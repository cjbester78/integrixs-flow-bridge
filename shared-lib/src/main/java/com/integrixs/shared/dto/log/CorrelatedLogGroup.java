package com.integrixs.shared.dto.log;

import com.integrixs.shared.dto.system.SystemLogDTO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Represents a group of correlated logs.
 */
public class CorrelatedLogGroup {

    private String correlationId;
    private String flowId;
    private List<SystemLogDTO> logs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration duration;
    private List<String> involvedComponents;
    private Map<String, List<TimelineEntry>> componentTimeline;
    private List<ErrorChainEntry> errorChain;
    private Map<String, Object> statistics;
    private String relationType;

    // Default constructor
    public CorrelatedLogGroup() {
        this.logs = new ArrayList<>();
        this.involvedComponents = new ArrayList<>();
        this.componentTimeline = new HashMap<>();
        this.errorChain = new ArrayList<>();
        this.statistics = new HashMap<>();
    }

    // All args constructor
    public CorrelatedLogGroup(String correlationId, String flowId, List<SystemLogDTO> logs, LocalDateTime startTime, LocalDateTime endTime, Duration duration, List<String> involvedComponents, Map<String, List<TimelineEntry>> componentTimeline, List<ErrorChainEntry> errorChain, Map<String, Object> statistics, String relationType) {
        this.correlationId = correlationId;
        this.flowId = flowId;
        this.logs = logs != null ? logs : new ArrayList<>();
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
        this.involvedComponents = involvedComponents != null ? involvedComponents : new ArrayList<>();
        this.componentTimeline = componentTimeline != null ? componentTimeline : new HashMap<>();
        this.errorChain = errorChain != null ? errorChain : new ArrayList<>();
        this.statistics = statistics != null ? statistics : new HashMap<>();
        this.relationType = relationType;
    }

    // Getters
    public String getCorrelationId() { return correlationId; }
    public String getFlowId() { return flowId; }
    public List<SystemLogDTO> getLogs() { return logs; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Duration getDuration() { return duration; }
    public List<String> getInvolvedComponents() { return involvedComponents; }
    public Map<String, List<TimelineEntry>> getComponentTimeline() { return componentTimeline; }
    public List<ErrorChainEntry> getErrorChain() { return errorChain; }
    public Map<String, Object> getStatistics() { return statistics; }
    public String getRelationType() { return relationType; }

    // Setters
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public void setLogs(List<SystemLogDTO> logs) { this.logs = logs; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setDuration(Duration duration) { this.duration = duration; }
    public void setInvolvedComponents(List<String> involvedComponents) { this.involvedComponents = involvedComponents; }
    public void setComponentTimeline(Map<String, List<TimelineEntry>> componentTimeline) { this.componentTimeline = componentTimeline; }
    public void setErrorChain(List<ErrorChainEntry> errorChain) { this.errorChain = errorChain; }
    public void setStatistics(Map<String, Object> statistics) { this.statistics = statistics; }
    public void setRelationType(String relationType) { this.relationType = relationType; }

    // Builder
    public static CorrelatedLogGroupBuilder builder() {
        return new CorrelatedLogGroupBuilder();
    }

    public static class CorrelatedLogGroupBuilder {
        private String correlationId;
        private String flowId;
        private List<SystemLogDTO> logs = new ArrayList<>();
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Duration duration;
        private List<String> involvedComponents = new ArrayList<>();
        private Map<String, List<TimelineEntry>> componentTimeline = new HashMap<>();
        private List<ErrorChainEntry> errorChain = new ArrayList<>();
        private Map<String, Object> statistics = new HashMap<>();
        private String relationType;

        public CorrelatedLogGroupBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public CorrelatedLogGroupBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public CorrelatedLogGroupBuilder logs(List<SystemLogDTO> logs) {
            this.logs = logs;
            return this;
        }

        public CorrelatedLogGroupBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public CorrelatedLogGroupBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public CorrelatedLogGroupBuilder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public CorrelatedLogGroupBuilder involvedComponents(List<String> involvedComponents) {
            this.involvedComponents = involvedComponents;
            return this;
        }

        public CorrelatedLogGroupBuilder componentTimeline(Map<String, List<TimelineEntry>> componentTimeline) {
            this.componentTimeline = componentTimeline;
            return this;
        }

        public CorrelatedLogGroupBuilder errorChain(List<ErrorChainEntry> errorChain) {
            this.errorChain = errorChain;
            return this;
        }

        public CorrelatedLogGroupBuilder statistics(Map<String, Object> statistics) {
            this.statistics = statistics;
            return this;
        }

        public CorrelatedLogGroupBuilder relationType(String relationType) {
            this.relationType = relationType;
            return this;
        }

        public CorrelatedLogGroup build() {
            return new CorrelatedLogGroup(correlationId, flowId, logs, startTime, endTime, duration, involvedComponents, componentTimeline, errorChain, statistics, relationType);
        }
    }

    /**
     * Represents a timeline entry
     */
    public static class TimelineEntry {
        private LocalDateTime timestamp;
        private String level;
        private String message;
        private String category;

        // Default constructor
        public TimelineEntry() {
        }

        // All args constructor
        public TimelineEntry(LocalDateTime timestamp, String level, String message, String category) {
            this.timestamp = timestamp;
            this.level = level;
            this.message = message;
            this.category = category;
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getLevel() { return level; }
        public String getMessage() { return message; }
        public String getCategory() { return category; }

        // Setters
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public void setLevel(String level) { this.level = level; }
        public void setMessage(String message) { this.message = message; }
        public void setCategory(String category) { this.category = category; }
    }

    /**
     * Represents an error chain entry
     */
    public static class ErrorChainEntry {
        private LocalDateTime timestamp;
        private String component;
        private String message;
        private String stackTrace;
        private Duration propagationDelay;

        // Default constructor
        public ErrorChainEntry() {
        }

        // All args constructor
        public ErrorChainEntry(LocalDateTime timestamp, String component, String message, String stackTrace, Duration propagationDelay) {
            this.timestamp = timestamp;
            this.component = component;
            this.message = message;
            this.stackTrace = stackTrace;
            this.propagationDelay = propagationDelay;
        }

        // Getters
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getComponent() { return component; }
        public String getMessage() { return message; }
        public String getStackTrace() { return stackTrace; }
        public Duration getPropagationDelay() { return propagationDelay; }

        // Setters
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public void setComponent(String component) { this.component = component; }
        public void setMessage(String message) { this.message = message; }
        public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }
        public void setPropagationDelay(Duration propagationDelay) { this.propagationDelay = propagationDelay; }
    }
}