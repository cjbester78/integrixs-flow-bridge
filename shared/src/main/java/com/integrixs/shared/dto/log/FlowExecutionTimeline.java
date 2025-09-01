package com.integrixs.shared.dto.log;

import com.integrixs.data.model.SystemLog;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a timeline of flow execution.
 */
@Data
public class FlowExecutionTimeline {
    
    private String flowId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Duration totalDuration;
    private List<ExecutionStep> executionSteps;
    private List<Bottleneck> bottlenecks;
    private Map<String, Object> executionGraph;
    
    /**
     * Execution step in the flow.
     */
    @Data
    public static class ExecutionStep {
        private String component;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Duration duration;
        private String status;
        private List<SystemLog> logs;
        private List<String> keyEvents;
    }
    
    /**
     * Identified bottleneck.
     */
    @Data
    public static class Bottleneck {
        private String component;
        private Duration duration;
        private String severity;
        private String impact;
    }
}