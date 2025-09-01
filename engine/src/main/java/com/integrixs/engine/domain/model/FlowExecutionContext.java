package com.integrixs.engine.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model for flow execution context
 */
@Data
@Builder
public class FlowExecutionContext {
    private String executionId;
    private String flowId;
    private UUID sourceAdapterId;
    private UUID targetAdapterId;
    private String mappingMode;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    private String correlationId;
    private boolean async;
    private Integer timeout;
    
    /**
     * Add a parameter to the context
     * @param key Parameter key
     * @param value Parameter value
     */
    public void addParameter(String key, Object value) {
        this.parameters.put(key, value);
    }
    
    /**
     * Add a header to the context
     * @param key Header key
     * @param value Header value
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
    
    /**
     * Add metadata to the context
     * @param key Metadata key
     * @param value Metadata value
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
}