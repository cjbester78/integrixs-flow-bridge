package com.integrixs.shared.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Context for adapter execution
 */
public class AdapterContext {
    private String executionId;
    private String flowId;
    private Map<String, Object> inputData;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, Object> properties = new HashMap<>();
    private String correlationId;
    private Long timeout;

    // Default constructor
    public AdapterContext() {
    }

    // All args constructor
    public AdapterContext(String executionId, String flowId, Map<String, Object> inputData,
                         Map<String, String> headers, Map<String, Object> properties,
                         String correlationId, Long timeout) {
        this.executionId = executionId;
        this.flowId = flowId;
        this.inputData = inputData;
        this.headers = headers != null ? headers : new HashMap<>();
        this.properties = properties != null ? properties : new HashMap<>();
        this.correlationId = correlationId;
        this.timeout = timeout;
    }

    // Getters
    public String getExecutionId() {
        return executionId;
    }

    public String getFlowId() {
        return flowId;
    }

    public Map<String, Object> getInputData() {
        return inputData;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Long getTimeout() {
        return timeout;
    }

    // Setters
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public void setInputData(Map<String, Object> inputData) {
        this.inputData = inputData;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String executionId;
        private String flowId;
        private Map<String, Object> inputData;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, Object> properties = new HashMap<>();
        private String correlationId;
        private Long timeout;

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public Builder inputData(Map<String, Object> inputData) {
            this.inputData = inputData;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties = properties;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder timeout(Long timeout) {
            this.timeout = timeout;
            return this;
        }

        public AdapterContext build() {
            return new AdapterContext(executionId, flowId, inputData, headers,
                                    properties, correlationId, timeout);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdapterContext that = (AdapterContext) o;
        return Objects.equals(executionId, that.executionId) &&
               Objects.equals(flowId, that.flowId) &&
               Objects.equals(inputData, that.inputData) &&
               Objects.equals(headers, that.headers) &&
               Objects.equals(properties, that.properties) &&
               Objects.equals(correlationId, that.correlationId) &&
               Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(executionId, flowId, inputData, headers, properties, correlationId, timeout);
    }

    @Override
    public String toString() {
        return "AdapterContext{" +
                "executionId='" + executionId + '\'' +
                ", flowId='" + flowId + '\'' +
                ", inputData=" + inputData +
                ", headers=" + headers +
                ", properties=" + properties +
                ", correlationId='" + correlationId + '\'' +
                ", timeout=" + timeout +
                '}';
    }
}
