package com.integrixs.backend.api.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class TestConditionResponse {
    
    private String id;
    private Instant timestamp;
    private String condition;
    private String conditionType;
    private boolean result;
    private Long executionTimeMs;
    private String error;
    private TestDetails details;
    
    @Data
    @Builder
    public static class TestDetails {
        private String evaluatedExpression;
        private Map<String, Object> variables;
        private List<ExecutionStep> steps;
    }
    
    @Data
    @Builder
    public static class ExecutionStep {
        private String description;
        private Object result;
        private Long durationMs;
    }
}