package com.integrixs.testing.runners;

import com.integrixs.data.model.IntegrationFlow;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a single flow execution
 */
public class FlowExecution {
    
    private final String executionId;
    private final IntegrationFlow flow;
    private final Map<String, StepExecution> stepExecutions;
    private final List<String> executedSteps;
    private final ObjectMapper objectMapper;
    
    private Object input;
    private Object output;
    private Map<String, String> headers;
    private String state;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long executionTime;
    private boolean successful;
    private String error;
    private Exception exception;
    private boolean cancelled;
    
    public FlowExecution(String executionId, IntegrationFlow flow) {
        this.executionId = executionId;
        this.flow = flow;
        this.stepExecutions = new ConcurrentHashMap<>();
        this.executedSteps = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        this.headers = new HashMap<>();
        this.state = "CREATED";
    }
    
    /**
     * Start the execution
     */
    public void start() {
        this.startTime = LocalDateTime.now();
        this.state = "RUNNING";
    }
    
    /**
     * Complete the execution
     */
    public void complete() {
        this.endTime = LocalDateTime.now();
        this.executionTime = System.currentTimeMillis() - 
            startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.state = "COMPLETED";
        this.successful = true;
    }
    
    /**
     * Fail the execution
     */
    public void fail(String error) {
        this.endTime = LocalDateTime.now();
        this.executionTime = System.currentTimeMillis() - 
            startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.state = "FAILED";
        this.error = error;
        this.successful = false;
    }
    
    /**
     * Cancel the execution
     */
    public void cancel() {
        this.cancelled = true;
        this.state = "CANCELLED";
        if (this.endTime == null) {
            this.endTime = LocalDateTime.now();
        }
    }
    
    /**
     * Start a step execution
     */
    public void startStep(String stepName) {
        StepExecution stepExecution = new StepExecution(stepName);
        stepExecution.start();
        stepExecutions.put(stepName, stepExecution);
        executedSteps.add(stepName);
    }
    
    /**
     * Complete a step execution
     */
    public void completeStep(String stepName) {
        StepExecution stepExecution = stepExecutions.get(stepName);
        if (stepExecution != null) {
            stepExecution.complete();
        }
    }
    
    /**
     * Fail a step execution
     */
    public void failStep(String stepName, String error) {
        StepExecution stepExecution = stepExecutions.get(stepName);
        if (stepExecution != null) {
            stepExecution.fail(error);
        }
    }
    
    /**
     * Get output as specific type
     */
    public <T> T getOutput(Class<T> type) {
        if (output == null) {
            return null;
        }
        
        if (type.isAssignableFrom(output.getClass())) {
            return type.cast(output);
        }
        
        // Try conversion through Jackson
        try {
            return objectMapper.convertValue(output, type);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert output to " + type.getName(), e);
        }
    }
    
    /**
     * Get output as string
     */
    public String getOutputAsString() {
        if (output == null) {
            return null;
        }
        
        if (output instanceof String) {
            return (String) output;
        }
        
        try {
            return objectMapper.writeValueAsString(output);
        } catch (Exception e) {
            return output.toString();
        }
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        // Clean up any temporary resources
        // This is a placeholder for cleanup logic
    }
    
    /**
     * Get execution summary
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("executionId", executionId);
        summary.put("flowName", flow != null ? flow.getName() : "Unknown");
        summary.put("state", state);
        summary.put("successful", successful);
        summary.put("startTime", startTime);
        summary.put("endTime", endTime);
        summary.put("executionTime", executionTime);
        summary.put("stepsExecuted", executedSteps.size());
        
        if (!successful && error != null) {
            summary.put("error", error);
        }
        
        // Add step summaries
        List<Map<String, Object>> stepSummaries = new ArrayList<>();
        stepExecutions.forEach((name, execution) -> {
            Map<String, Object> stepSummary = new HashMap<>();
            stepSummary.put("name", name);
            stepSummary.put("state", execution.getState());
            stepSummary.put("executionTime", execution.getExecutionTime());
            if (execution.getError() != null) {
                stepSummary.put("error", execution.getError());
            }
            stepSummaries.add(stepSummary);
        });
        summary.put("steps", stepSummaries);
        
        return summary;
    }
    
    // Getters and setters
    public String getExecutionId() {
        return executionId;
    }
    
    public IntegrationFlow getFlow() {
        return flow;
    }
    
    public Object getInput() {
        return input;
    }
    
    public void setInput(Object input) {
        this.input = input;
    }
    
    public Object getOutput() {
        return output;
    }
    
    public void setOutput(Object output) {
        this.output = output;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public String getState() {
        return state;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public String getError() {
        return error;
    }
    
    public Exception getException() {
        return exception;
    }
    
    public void setException(Exception exception) {
        this.exception = exception;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public List<String> getExecutedSteps() {
        return executedSteps;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Step execution details
     */
    private static class StepExecution {
        private final String name;
        private String state;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private long executionTime;
        private String error;
        
        public StepExecution(String name) {
            this.name = name;
            this.state = "PENDING";
        }
        
        public void start() {
            this.startTime = LocalDateTime.now();
            this.state = "RUNNING";
        }
        
        public void complete() {
            this.endTime = LocalDateTime.now();
            this.executionTime = System.currentTimeMillis() - 
                startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.state = "COMPLETED";
        }
        
        public void fail(String error) {
            this.endTime = LocalDateTime.now();
            this.executionTime = System.currentTimeMillis() - 
                startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            this.state = "FAILED";
            this.error = error;
        }
        
        public String getName() {
            return name;
        }
        
        public String getState() {
            return state;
        }
        
        public long getExecutionTime() {
            return executionTime;
        }
        
        public String getError() {
            return error;
        }
    }
}