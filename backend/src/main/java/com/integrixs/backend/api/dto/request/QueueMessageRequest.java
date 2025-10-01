package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;

/**
 * Request object for queuing a new message
 */
public class QueueMessageRequest {

    @NotBlank(message = "Flow ID is required")
    private String flowId;

    @NotNull(message = "Payload is required")
    private String payload;

    private Map<String, String> headers;

    @Min(0)
    @Max(10)
    private int priority = 5;

    private String correlationId;

    private Map<String, Object> metadata;

    private boolean async = true;

    // Default constructor
    public QueueMessageRequest() {
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
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
}
