package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.RouteCondition.ConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class TestConditionRequest {

    @NotBlank(message = "Condition is required")
    private String condition;

    @NotNull(message = "Condition type is required")
    private ConditionType conditionType;

    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;

    private Map<String, String> headers;

    private Map<String, Object> metadata;

    // Default constructor
    public TestConditionRequest() {
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
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
