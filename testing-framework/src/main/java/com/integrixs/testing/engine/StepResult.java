package com.integrixs.testing.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of a step execution
 */
public class StepResult {
    
    private Object output;
    private Map<String, String> headers;
    private Map<String, Object> metadata;
    private boolean successful;
    private String error;
    private long executionTime;
    
    public StepResult() {
        this.headers = new HashMap<>();
        this.metadata = new HashMap<>();
        this.successful = true;
    }
    
    public static StepResult success(Object output) {
        StepResult result = new StepResult();
        result.setOutput(output);
        result.setSuccessful(true);
        return result;
    }
    
    public static StepResult failure(String error) {
        StepResult result = new StepResult();
        result.setError(error);
        result.setSuccessful(false);
        return result;
    }
    
    // Builder methods
    public StepResult withOutput(Object output) {
        this.output = output;
        return this;
    }
    
    public StepResult withHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }
    
    public StepResult withHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }
    
    public StepResult withMetadata(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
    
    public StepResult withExecutionTime(long executionTime) {
        this.executionTime = executionTime;
        return this;
    }
    
    // Getters and setters
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
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public boolean isSuccessful() {
        return successful;
    }
    
    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
        this.successful = false;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
}