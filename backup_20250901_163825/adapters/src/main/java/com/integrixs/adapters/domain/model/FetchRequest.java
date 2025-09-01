package com.integrixs.adapters.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for fetch requests (used by sender adapters)
 */
@Data
@Builder
public class FetchRequest {
    private String requestId;
    private String adapterId;
    @Builder.Default
    private Map<String, Object> parameters = new HashMap<>();
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();
    private String query;
    private Integer limit;
    private Integer offset;
    private String lastFetchMarker;
    private boolean deltaFetch;
    private Long timeout; // milliseconds
    
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
}