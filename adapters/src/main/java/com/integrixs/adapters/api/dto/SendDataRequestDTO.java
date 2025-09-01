package com.integrixs.adapters.api.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for send data requests
 */
@Data
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
}