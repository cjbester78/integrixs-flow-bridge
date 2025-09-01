package com.integrixs.adapters.api.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for fetch data requests
 */
@Data
public class FetchDataRequestDTO {
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String query;
    private Integer limit;
    private Integer offset;
    private String lastFetchMarker;
    private boolean deltaFetch = false;
    private Long timeout;
    
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