package com.integrixs.engine.domain.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for adapter execution context
 */
public class AdapterExecutionContext {
    private String executionId;
    private String flowId;
    private String stepId;
    private String adapterId;
    private String adapterType;
    private ExecutionMode mode;
    private Object inputData;
    private Map<String, Object> parameters = new HashMap<>();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, Object> metadata = new HashMap<>();
    private Map<String, Object> configParameters = new HashMap<>();
    private boolean async;
    private Integer timeout; // in milliseconds
    private String correlationId;

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
    public AdapterExecutionContext() {
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

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
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

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
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

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getAdapterId() {
        return adapterId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    public String getAdapterType() {
        return adapterType;
    }

    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }

    public ExecutionMode getMode() {
        return mode;
    }

    public void setMode(ExecutionMode mode) {
        this.mode = mode;
    }

    public Object getInputData() {
        return inputData;
    }

    public void setInputData(Object inputData) {
        this.inputData = inputData;
    }

    public Map<String, Object> getConfigParameters() {
        return configParameters;
    }

    public void setConfigParameters(Map<String, Object> configParameters) {
        this.configParameters = configParameters;
    }

    // Builder
    public static AdapterExecutionContextBuilder builder() {
        return new AdapterExecutionContextBuilder();
    }

    public static class AdapterExecutionContextBuilder {
        private String executionId;
        private String flowId;
        private String stepId;
        private String adapterId;
        private String adapterType;
        private ExecutionMode mode;
        private Object inputData;
        private Map<String, Object> parameters;
        private Map<String, String> headers;
        private Map<String, Object> metadata;
        private Map<String, Object> configParameters;
        private boolean async;
        private String correlationId;
        private Integer timeout;

        public AdapterExecutionContextBuilder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public AdapterExecutionContextBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public AdapterExecutionContextBuilder stepId(String stepId) {
            this.stepId = stepId;
            return this;
        }

        public AdapterExecutionContextBuilder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        public AdapterExecutionContextBuilder async(boolean async) {
            this.async = async;
            return this;
        }

        public AdapterExecutionContextBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public AdapterExecutionContextBuilder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public AdapterExecutionContextBuilder mode(ExecutionMode mode) {
            this.mode = mode;
            return this;
        }

        public AdapterExecutionContextBuilder inputData(Object inputData) {
            this.inputData = inputData;
            return this;
        }

        public AdapterExecutionContextBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public AdapterExecutionContextBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public AdapterExecutionContextBuilder configParameters(Map<String, Object> configParameters) {
            this.configParameters = configParameters;
            return this;
        }

        public AdapterExecutionContextBuilder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public AdapterExecutionContextBuilder timeout(Integer timeout) {
            this.timeout = timeout;
            return this;
        }

        public AdapterExecutionContext build() {
            AdapterExecutionContext instance = new AdapterExecutionContext();
            instance.setExecutionId(this.executionId);
            instance.setFlowId(this.flowId);
            instance.setStepId(this.stepId);
            instance.setAdapterId(this.adapterId);
            instance.setAdapterType(this.adapterType);
            instance.setMode(this.mode);
            instance.setInputData(this.inputData);
            instance.setParameters(this.parameters != null ? this.parameters : new HashMap<>());
            instance.setHeaders(this.headers != null ? this.headers : new HashMap<>());
            instance.setMetadata(this.metadata != null ? this.metadata : new HashMap<>());
            instance.setConfigParameters(this.configParameters != null ? this.configParameters : new HashMap<>());
            instance.setAsync(this.async);
            instance.setCorrelationId(this.correlationId);
            instance.setTimeout(this.timeout);
            return instance;
        }
    }
}
