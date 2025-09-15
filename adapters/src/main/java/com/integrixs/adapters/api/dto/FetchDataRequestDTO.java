package com.integrixs.adapters.api.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for fetch data requests
 */
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

    // Getters and Setters
    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getLastFetchMarker() {
        return lastFetchMarker;
    }

    public void setLastFetchMarker(String lastFetchMarker) {
        this.lastFetchMarker = lastFetchMarker;
    }

    public boolean isDeltaFetch() {
        return deltaFetch;
    }

    public void setDeltaFetch(boolean deltaFetch) {
        this.deltaFetch = deltaFetch;
    }

    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
}
