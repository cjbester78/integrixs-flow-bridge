package com.integrixs.engine.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for adapter execution requests
 */
@Data
@NoArgsConstructor
public class AdapterExecutionRequestDTO {
    private String adapterId;
    private String flowId;
    private String stepId;
    private Object data;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();
    private boolean async = false;
    private Integer timeout; // milliseconds
    private String correlationId;
}
