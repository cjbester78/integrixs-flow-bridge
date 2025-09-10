package com.integrixs.testing.runners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Context for flow execution
 */
public class FlowContext {
    
    private FlowExecution execution;
    private Map<String, Object> variables;
    private Map<String, String> headers;
    private Object payload;
    private Object stepInput;
    private Map<String, Object> properties;
    
    public FlowContext() {
        this.variables = new ConcurrentHashMap<>();
        this.headers = new ConcurrentHashMap<>();
        this.properties = new ConcurrentHashMap<>();
    }
    
    /**
     * Get variable value
     */
    public Object getVariable(String name) {
        return variables.get(name);
    }
    
    /**
     * Set variable value
     */
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
    
    /**
     * Remove variable
     */
    public Object removeVariable(String name) {
        return variables.remove(name);
    }
    
    /**
     * Check if variable exists
     */
    public boolean hasVariable(String name) {
        return variables.containsKey(name);
    }
    
    /**
     * Get header value
     */
    public String getHeader(String name) {
        return headers.get(name);
    }
    
    /**
     * Set header value
     */
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }
    
    /**
     * Remove header
     */
    public String removeHeader(String name) {
        return headers.remove(name);
    }
    
    /**
     * Get property value
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }
    
    /**
     * Set property value
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }
    
    /**
     * Create a child context
     */
    public FlowContext createChildContext() {
        FlowContext child = new FlowContext();
        child.execution = this.execution;
        child.variables = new ConcurrentHashMap<>(this.variables);
        child.headers = new ConcurrentHashMap<>(this.headers);
        child.properties = new ConcurrentHashMap<>(this.properties);
        child.payload = this.payload;
        return child;
    }
    
    /**
     * Merge another context into this one
     */
    public void merge(FlowContext other) {
        if (other.variables != null) {
            this.variables.putAll(other.variables);
        }
        if (other.headers != null) {
            this.headers.putAll(other.headers);
        }
        if (other.properties != null) {
            this.properties.putAll(other.properties);
        }
        if (other.payload != null) {
            this.payload = other.payload;
        }
    }
    
    // Getters and setters
    public FlowExecution getExecution() {
        return execution;
    }
    
    public void setExecution(FlowExecution execution) {
        this.execution = execution;
    }
    
    public Map<String, Object> getVariables() {
        return variables;
    }
    
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public Object getStepInput() {
        return stepInput;
    }
    
    public void setStepInput(Object stepInput) {
        this.stepInput = stepInput;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}