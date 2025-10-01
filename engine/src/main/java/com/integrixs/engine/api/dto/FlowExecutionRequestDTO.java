package com.integrixs.engine.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for flow execution requests
 */
public class FlowExecutionRequestDTO {
    private String executionId;
    private String flowId;
    private Object message;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();
    private boolean async = false;
    private Integer timeout; // milliseconds
    private String correlationId;

    // Default constructor
    public FlowExecutionRequestDTO() {
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

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
