package com.integrixs.backend.dto.dashboard.health;

import java.time.Instant;
import java.util.Map;

public class DiagnosticResult {
    private boolean success;
    private String message;
    private String diagnosticType;
    private Map<String, Object> details;
    private Instant timestamp;
    private Long executionTimeMs;
    private String recommendedAction;
    private String testName;
    private boolean passed;

    // Default constructor
    public DiagnosticResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDiagnosticType() {
        return diagnosticType;
    }

    public void setDiagnosticType(String diagnosticType) {
        this.diagnosticType = diagnosticType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }
}