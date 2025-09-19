package com.integrixs.engine.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for workflow execution requests
 */
public class WorkflowExecutionRequestDTO {
    private String flowId;
    private Object inputData;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, Object> initialVariables = new HashMap<>();
    private String correlationId;
    private boolean async = false;
    private Integer timeout; // milliseconds
    private String initiatedBy;

    // Default constructor
    public WorkflowExecutionRequestDTO() {
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public Object getInputData() {
        return inputData;
    }

    public void setInputData(Object inputData) {
        this.inputData = inputData;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }
}
