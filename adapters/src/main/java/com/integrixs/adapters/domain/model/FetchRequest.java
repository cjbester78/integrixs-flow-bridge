package com.integrixs.adapters.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for fetch requests(used by inbound adapters)
 */
public class FetchRequest {
    private String requestId;
    private String adapterId;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private String query;
    private Integer limit;
    private Integer offset;
    private String lastFetchMarker;
    private boolean deltaFetch;
    private Long timeout; // milliseconds

    public FetchRequest() {
    }

    private FetchRequest(Builder builder) {
        this.requestId = builder.requestId;
        this.adapterId = builder.adapterId;
        this.parameters = builder.parameters != null ? builder.parameters : new HashMap<>();
        this.headers = builder.headers != null ? builder.headers : new HashMap<>();
        this.query = builder.query;
        this.limit = builder.limit;
        this.offset = builder.offset;
        this.lastFetchMarker = builder.lastFetchMarker;
        this.deltaFetch = builder.deltaFetch;
        this.timeout = builder.timeout;
    }

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
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

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

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String requestId;
        private String adapterId;
        private Map<String, Object> parameters = new HashMap<>();
        private Map<String, String> headers = new HashMap<>();
        private String query;
        private Integer limit;
        private Integer offset;
        private String lastFetchMarker;
        private boolean deltaFetch;
        private Long timeout;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public Builder limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Builder offset(Integer offset) {
            this.offset = offset;
            return this;
        }

        public Builder lastFetchMarker(String lastFetchMarker) {
            this.lastFetchMarker = lastFetchMarker;
            return this;
        }

        public Builder deltaFetch(boolean deltaFetch) {
            this.deltaFetch = deltaFetch;
            return this;
        }

        public Builder timeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public FetchRequest build() {
            return new FetchRequest(this);
        }
    }
}
