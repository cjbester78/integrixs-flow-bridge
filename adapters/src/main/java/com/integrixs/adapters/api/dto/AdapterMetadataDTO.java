package com.integrixs.adapters.api.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for adapter metadata
 */
public class AdapterMetadataDTO {
    public AdapterMetadataDTO() {
    }


    private String adapterName;
    private String adapterType;
    private String adapterMode;
    private String version;
    private String description;
    private List<String> supportedOperations;
    private List<String> requiredProperties;
    private List<String> optionalProperties;
    private Map<String, String> capabilities;
    private boolean supportsAsync;
    private boolean supportsBatch;
    private boolean supportsStreaming;
    private Map<String, String> propertyDescriptions;
    private Map<String, Object> defaultPropertyValues;
    // Getters and Setters
    public String getAdapterName() {
        return adapterName;
    }
    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
    public String getAdapterType() {
        return adapterType;
    }
    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }
    public String getAdapterMode() {
        return adapterMode;
    }
    public void setAdapterMode(String adapterMode) {
        this.adapterMode = adapterMode;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public List<String> getSupportedOperations() {
        return supportedOperations;
    }
    public void setSupportedOperations(List<String> supportedOperations) {
        this.supportedOperations = supportedOperations;
    }
    public List<String> getRequiredProperties() {
        return requiredProperties;
    }
    public void setRequiredProperties(List<String> requiredProperties) {
        this.requiredProperties = requiredProperties;
    }
    public List<String> getOptionalProperties() {
        return optionalProperties;
    }
    public void setOptionalProperties(List<String> optionalProperties) {
        this.optionalProperties = optionalProperties;
    }
    public Map<String, String> getCapabilities() {
        return capabilities;
    }
    public void setCapabilities(Map<String, String> capabilities) {
        this.capabilities = capabilities;
    }
    public boolean isSupportsAsync() {
        return supportsAsync;
    }
    public void setSupportsAsync(boolean supportsAsync) {
        this.supportsAsync = supportsAsync;
    }
    public boolean isSupportsBatch() {
        return supportsBatch;
    }
    public void setSupportsBatch(boolean supportsBatch) {
        this.supportsBatch = supportsBatch;
    }
    public boolean isSupportsStreaming() {
        return supportsStreaming;
    }
    public void setSupportsStreaming(boolean supportsStreaming) {
        this.supportsStreaming = supportsStreaming;
    }
    public Map<String, String> getPropertyDescriptions() {
        return propertyDescriptions;
    }
    public void setPropertyDescriptions(Map<String, String> propertyDescriptions) {
        this.propertyDescriptions = propertyDescriptions;
    }
    public Map<String, Object> getDefaultPropertyValues() {
        return defaultPropertyValues;
    }
    public void setDefaultPropertyValues(Map<String, Object> defaultPropertyValues) {
        this.defaultPropertyValues = defaultPropertyValues;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterName;
        private String adapterType;
        private String adapterMode;
        private String version;
        private String description;
        private List<String> supportedOperations;
        private List<String> requiredProperties;
        private List<String> optionalProperties;
        private Map<String, String> capabilities;
        private boolean supportsAsync;
        private boolean supportsBatch;
        private boolean supportsStreaming;
        private Map<String, String> propertyDescriptions;
        private Map<String, Object> defaultPropertyValues;

        public Builder adapterName(String adapterName) {
            this.adapterName = adapterName;
            return this;
        }

        public Builder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public Builder adapterMode(String adapterMode) {
            this.adapterMode = adapterMode;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder supportedOperations(List<String> supportedOperations) {
            this.supportedOperations = supportedOperations;
            return this;
        }

        public Builder requiredProperties(List<String> requiredProperties) {
            this.requiredProperties = requiredProperties;
            return this;
        }

        public Builder optionalProperties(List<String> optionalProperties) {
            this.optionalProperties = optionalProperties;
            return this;
        }

        public Builder capabilities(Map<String, String> capabilities) {
            this.capabilities = capabilities;
            return this;
        }

        public Builder supportsAsync(boolean supportsAsync) {
            this.supportsAsync = supportsAsync;
            return this;
        }

        public Builder supportsBatch(boolean supportsBatch) {
            this.supportsBatch = supportsBatch;
            return this;
        }

        public Builder supportsStreaming(boolean supportsStreaming) {
            this.supportsStreaming = supportsStreaming;
            return this;
        }

        public Builder propertyDescriptions(Map<String, String> propertyDescriptions) {
            this.propertyDescriptions = propertyDescriptions;
            return this;
        }

        public Builder defaultPropertyValues(Map<String, Object> defaultPropertyValues) {
            this.defaultPropertyValues = defaultPropertyValues;
            return this;
        }

        public AdapterMetadataDTO build() {
            AdapterMetadataDTO obj = new AdapterMetadataDTO();
            obj.adapterName = this.adapterName;
            obj.adapterType = this.adapterType;
            obj.adapterMode = this.adapterMode;
            obj.version = this.version;
            obj.description = this.description;
            obj.supportedOperations = this.supportedOperations;
            obj.requiredProperties = this.requiredProperties;
            obj.optionalProperties = this.optionalProperties;
            obj.capabilities = this.capabilities;
            obj.supportsAsync = this.supportsAsync;
            obj.supportsBatch = this.supportsBatch;
            obj.supportsStreaming = this.supportsStreaming;
            obj.propertyDescriptions = this.propertyDescriptions;
            obj.defaultPropertyValues = this.defaultPropertyValues;
            return obj;
        }
    }
}
