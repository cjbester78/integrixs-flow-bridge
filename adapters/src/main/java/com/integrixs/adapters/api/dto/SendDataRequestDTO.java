package com.integrixs.adapters.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for send data requests
 */
public class SendDataRequestDTO {
    private Object payload;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String destination;
    private String operation;
    private boolean synchronous = true;
    private Long timeout;
    private Integer priority;

    /**
     * Add parameter
     * @param key Parameter key
     * @param value Parameter value
     */
    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    /**
     * Add header
     * @param key Header key
     * @param value Header value
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Set payload with type safety
     * @param payload Payload data
     */
    public <T> void setTypedPayload(T payload) {
        this.payload = payload;
    }

    /**
     * Get payload as specific type
     * @param type Target type
     * @return Payload cast to type
     */
    public <T> T getPayloadAs(Class<T> type) {
        return type.cast(payload);
    }
    // Getters and Setters
    public Object getPayload() {
        return payload;
    }
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    public Map<String, Object> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public boolean isSynchronous() {
        return synchronous;
    }
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }
    public Long getTimeout() {
        return timeout;
    }
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
