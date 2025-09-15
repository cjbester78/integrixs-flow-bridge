package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for flow execution trace
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TraceEventResponse {
        private String eventType;
        private String message;
        private LocalDateTime timestamp;
    }
}
