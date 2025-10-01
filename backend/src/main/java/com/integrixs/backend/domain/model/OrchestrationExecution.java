package com.integrixs.backend.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain model for orchestration execution
 */
public class OrchestrationExecution {
    private String executionId;
    private String flowId;
    private String flowName;
    private String status = "PENDING";
    private String currentStep;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Object inputData;
    private Object transformedData;
    private Object outputData;
    private Map<String, Object> executionContext = new HashMap<>();
    private List<String> logs = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Add a log entry
     * @param message The log message
     */
    public void addLog(String message) {
        this.logs.add(message);
    }

    /**
     * Update execution status
     * @param status New status
     */
    public void updateStatus(String status) {
        this.status = status;
        if("RUNNING".equals(status) && startTime == null) {
            this.startTime = LocalDateTime.now();
        } else if(("COMPLETED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status))
                   && endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }

    /**
     * Add context data
     * @param key Context key
     * @param value Context value
     */
    public void addContext(String key, Object value) {
        this.executionContext.put(key, value);
    }

    /**
     * Add metadata
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }

    /**
     * Check if execution is in progress
     * @return true if in progress
     */
    public boolean isInProgress() {
        return "RUNNING".equals(status) || "PENDING".equals(status);
    }

    /**
     * Check if execution is complete
     * @return true if complete
     */
    public boolean isComplete() {
        return "COMPLETED".equals(status) || "FAILED".equals(status) || "CANCELLED".equals(status);
    }

    // Default constructor
    public OrchestrationExecution() {
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

    public Object getInputData() {
        return inputData;
    }

    public void setInputData(Object inputData) {
        this.inputData = inputData;
    }

    public Object getTransformedData() {
        return transformedData;
    }

    public void setTransformedData(Object transformedData) {
        this.transformedData = transformedData;
    }

    public Object getOutputData() {
        return outputData;
    }

    public void setOutputData(Object outputData) {
        this.outputData = outputData;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void setLogs(List<String> logs) {
        this.logs = logs;
    }

    public Map<String, Object> getExecutionContext() {
        return executionContext;
    }

    public void setExecutionContext(Map<String, Object> executionContext) {
        this.executionContext = executionContext;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
