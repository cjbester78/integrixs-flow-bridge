package com.integrixs.adapters.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for send requests (used by outbound adapters)
 */
@Data
@Builder
public class SendRequest {
    private String requestId;
    private String adapterId;
    private Object payload;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    private String destination;
    private String operation;
    private boolean synchronous;
    private Long timeout; // milliseconds
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
     * Get payload as specific type
     * @param type Target type
     * @return Payload cast to type
     */
    public <T> T getPayloadAs(Class<T> type) {
        return type.cast(payload);
    }
}