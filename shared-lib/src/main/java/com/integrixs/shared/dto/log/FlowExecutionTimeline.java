package com.integrixs.shared.dto.log;

import com.integrixs.shared.dto.system.SystemLogDTO;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Represents a timeline of flow execution.
 */
public class FlowExecutionTimeline {

    private String flowId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration totalDuration;
    private List<ExecutionStep> executionSteps;
    private List<Bottleneck> bottlenecks;
    private Map<String, Object> executionGraph;

    // Default constructor
    public FlowExecutionTimeline() {
        this.executionSteps = new ArrayList<>();
        this.bottlenecks = new ArrayList<>();
        this.executionGraph = new HashMap<>();
    }

    // All args constructor
    public FlowExecutionTimeline(String flowId, LocalDateTime startTime, LocalDateTime endTime, Duration totalDuration, List<ExecutionStep> executionSteps, List<Bottleneck> bottlenecks, Map<String, Object> executionGraph) {
        this.flowId = flowId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalDuration = totalDuration;
        this.executionSteps = executionSteps != null ? executionSteps : new ArrayList<>();
        this.bottlenecks = bottlenecks != null ? bottlenecks : new ArrayList<>();
        this.executionGraph = executionGraph != null ? executionGraph : new HashMap<>();
    }

    // Getters
    public String getFlowId() { return flowId; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public Duration getTotalDuration() { return totalDuration; }
    public List<ExecutionStep> getExecutionSteps() { return executionSteps; }
    public List<Bottleneck> getBottlenecks() { return bottlenecks; }
    public Map<String, Object> getExecutionGraph() { return executionGraph; }

    // Setters
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public void setTotalDuration(Duration totalDuration) { this.totalDuration = totalDuration; }
    public void setExecutionSteps(List<ExecutionStep> executionSteps) { this.executionSteps = executionSteps; }
    public void setBottlenecks(List<Bottleneck> bottlenecks) { this.bottlenecks = bottlenecks; }
    public void setExecutionGraph(Map<String, Object> executionGraph) { this.executionGraph = executionGraph; }

    // Builder
    public static FlowExecutionTimelineBuilder builder() {
        return new FlowExecutionTimelineBuilder();
    }

    public static class FlowExecutionTimelineBuilder {
        private String flowId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Duration totalDuration;
        private List<ExecutionStep> executionSteps = new ArrayList<>();
        private List<Bottleneck> bottlenecks = new ArrayList<>();
        private Map<String, Object> executionGraph = new HashMap<>();

        public FlowExecutionTimelineBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public FlowExecutionTimelineBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public FlowExecutionTimelineBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public FlowExecutionTimelineBuilder totalDuration(Duration totalDuration) {
            this.totalDuration = totalDuration;
            return this;
        }

        public FlowExecutionTimelineBuilder executionSteps(List<ExecutionStep> executionSteps) {
            this.executionSteps = executionSteps;
            return this;
        }

        public FlowExecutionTimelineBuilder bottlenecks(List<Bottleneck> bottlenecks) {
            this.bottlenecks = bottlenecks;
            return this;
        }

        public FlowExecutionTimelineBuilder executionGraph(Map<String, Object> executionGraph) {
            this.executionGraph = executionGraph;
            return this;
        }

        public FlowExecutionTimeline build() {
            return new FlowExecutionTimeline(flowId, startTime, endTime, totalDuration, executionSteps, bottlenecks, executionGraph);
        }
    }

    /**
     * Represents a single execution step in the timeline
     */
    public static class ExecutionStep {
        private String component;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Duration duration;
        private String status;
        private List<SystemLogDTO> logs;
        private List<String> keyEvents;

        // Default constructor
        public ExecutionStep() {
            this.logs = new ArrayList<>();
            this.keyEvents = new ArrayList<>();
        }

        // All args constructor
        public ExecutionStep(String component, LocalDateTime startTime, LocalDateTime endTime, Duration duration, String status, List<SystemLogDTO> logs, List<String> keyEvents) {
            this.component = component;
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
            this.status = status;
            this.logs = logs != null ? logs : new ArrayList<>();
            this.keyEvents = keyEvents != null ? keyEvents : new ArrayList<>();
        }

        // Getters
        public String getComponent() { return component; }
        public LocalDateTime getStartTime() { return startTime; }
        public LocalDateTime getEndTime() { return endTime; }
        public Duration getDuration() { return duration; }
        public String getStatus() { return status; }
        public List<SystemLogDTO> getLogs() { return logs; }
        public List<String> getKeyEvents() { return keyEvents; }

        // Setters
        public void setComponent(String component) { this.component = component; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        public void setDuration(Duration duration) { this.duration = duration; }
        public void setStatus(String status) { this.status = status; }
        public void setLogs(List<SystemLogDTO> logs) { this.logs = logs; }
        public void setKeyEvents(List<String> keyEvents) { this.keyEvents = keyEvents; }
    }

    /**
     * Represents a performance bottleneck
     */
    public static class Bottleneck {
        private String component;
        private Duration duration;
        private String severity;
        private String impact;

        // Default constructor
        public Bottleneck() {
        }

        // All args constructor
        public Bottleneck(String component, Duration duration, String severity, String impact) {
            this.component = component;
            this.duration = duration;
            this.severity = severity;
            this.impact = impact;
        }

        // Getters
        public String getComponent() { return component; }
        public Duration getDuration() { return duration; }
        public String getSeverity() { return severity; }
        public String getImpact() { return impact; }

        // Setters
        public void setComponent(String component) { this.component = component; }
        public void setDuration(Duration duration) { this.duration = duration; }
        public void setSeverity(String severity) { this.severity = severity; }
        public void setImpact(String impact) { this.impact = impact; }
    }
}