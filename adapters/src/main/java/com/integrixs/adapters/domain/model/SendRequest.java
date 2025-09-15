package com.integrixs.adapters.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for send requests(used by outbound adapters)
 */
public class SendRequest {
    private String requestId;
    private String adapterId;
    private Object payload;
    private Map<String, Object> parameters = new HashMap<>();
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
    public Object getPayload() {
        return payload;
    }
    public void setPayload(Object payload) {
        this.payload = payload;
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
    public String getDestination() {
        return destination;
    }
    public void setDestination(String destination) {
        this.destination = destination;
    }
    public String getOperation() {
        return operation;
    }
    public void setOperation(String operation) {
        this.operation = operation;
    }
    public boolean isSynchronous() {
        return synchronous;
    }
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }
    public Long getTimeout() {
        return timeout;
    }
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }
    public Integer getPriority() {
        return priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String requestId;
        private String adapterId;
        private Object payload;
        private Map<String, Object> parameters;
        private Map<String, String> headers;
        private String destination;
        private String operation;
        private boolean synchronous;
        private Long timeout;
        private Integer priority;

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder payload(Object payload) {
            this.payload = payload;
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

        public Builder destination(String destination) {
            this.destination = destination;
            return this;
        }

        public Builder operation(String operation) {
            this.operation = operation;
            return this;
        }

        public Builder synchronous(boolean synchronous) {
            this.synchronous = synchronous;
            return this;
        }

        public Builder timeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder priority(Integer priority) {
            this.priority = priority;
            return this;
        }

        public SendRequest build() {
            SendRequest obj = new SendRequest();
            obj.requestId = this.requestId;
            obj.adapterId = this.adapterId;
            obj.payload = this.payload;
            obj.parameters = this.parameters;
            obj.headers = this.headers;
            obj.destination = this.destination;
            obj.operation = this.operation;
            obj.synchronous = this.synchronous;
            obj.timeout = this.timeout;
            obj.priority = this.priority;
            return obj;
        }
    }
}
