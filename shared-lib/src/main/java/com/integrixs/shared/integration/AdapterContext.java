package com.integrixs.shared.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Context for adapter execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterContext {
    private String executionId;
    private String flowId;
    private Map<String, Object> inputData;
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
    private String correlationId;
    private Long timeout;
}