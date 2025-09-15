package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.ConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;

@Data
public class TestConditionRequest {

    @NotBlank(message = "Condition is required")
    private String condition;

    @NotNull(message = "Condition type is required")
    private ConditionType conditionType;

    @NotNull(message = "Payload is required")
    private Map<String, Object> payload;

    private Map<String, String> headers;

    private Map<String, Object> metadata;
}
