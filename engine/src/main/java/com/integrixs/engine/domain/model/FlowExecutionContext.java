package com.integrixs.engine.domain.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model for flow execution context
 */
public class FlowExecutionContext {
    private String executionId;
    private String flowId;
    private UUID inboundAdapterId;
    private UUID outboundAdapterId;
    private String mappingMode;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
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

    // Default constructor
    public FlowExecutionContext() {
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public UUID getInboundAdapterId() {
        return inboundAdapterId;
    }

    public void setInboundAdapterId(UUID inboundAdapterId) {
        this.inboundAdapterId = inboundAdapterId;
    }

    public UUID getOutboundAdapterId() {
        return outboundAdapterId;
    }

    public void setOutboundAdapterId(UUID outboundAdapterId) {
        this.outboundAdapterId = outboundAdapterId;
    }

    public String getMappingMode() {
        return mappingMode;
    }

    public void setMappingMode(String mappingMode) {
        this.mappingMode = mappingMode;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
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

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    // Builder
    public static FlowExecutionContextBuilder builder() {
        return new FlowExecutionContextBuilder();
    }

    public static class FlowExecutionContextBuilder {
        private String executionId;
        private String flowId;
        private UUID inboundAdapterId;
        private UUID outboundAdapterId;
        private String mappingMode;
        private Map<String, Object> parameters;
        private Map<String, String> headers;
        private Map<String, Object> metadata;
        private String correlationId;
        private boolean async;
        private Integer timeout;

        public FlowExecutionContextBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public FlowExecutionContextBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public FlowExecutionContextBuilder inboundAdapterId(UUID inboundAdapterId) {
            this.inboundAdapterId = inboundAdapterId;
            return this;
        }

        public FlowExecutionContextBuilder outboundAdapterId(UUID outboundAdapterId) {
            this.outboundAdapterId = outboundAdapterId;
            return this;
        }

        public FlowExecutionContextBuilder mappingMode(String mappingMode) {
            this.mappingMode = mappingMode;
            return this;
        }

        public FlowExecutionContextBuilder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public FlowExecutionContextBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public FlowExecutionContextBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public FlowExecutionContextBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public FlowExecutionContextBuilder async(boolean async) {
            this.async = async;
            return this;
        }

        public FlowExecutionContextBuilder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public FlowExecutionContext build() {
            FlowExecutionContext instance = new FlowExecutionContext();
            instance.setExecutionId(this.executionId);
            instance.setFlowId(this.flowId);
            instance.setInboundAdapterId(this.inboundAdapterId);
            instance.setOutboundAdapterId(this.outboundAdapterId);
            instance.setMappingMode(this.mappingMode);
            instance.setParameters(this.parameters != null ? this.parameters : new HashMap<>());
            instance.setHeaders(this.headers != null ? this.headers : new HashMap<>());
            instance.setMetadata(this.metadata != null ? this.metadata : new HashMap<>());
            instance.setCorrelationId(this.correlationId);
            instance.setAsync(this.async);
            instance.setTimeout(this.timeout);
            return instance;
        }
    }
}
