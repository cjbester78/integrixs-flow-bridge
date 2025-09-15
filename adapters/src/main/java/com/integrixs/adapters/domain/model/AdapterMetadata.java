package com.integrixs.adapters.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Domain model for adapter metadata
 */
public class AdapterMetadata {
    private String adapterName;
    private AdapterConfiguration.AdapterTypeEnum adapterType;
    private AdapterConfiguration.AdapterModeEnum adapterMode;
    private String version;
    private String description;
    private List<String> supportedOperations;
    private List<String> requiredProperties;
    private List<String> optionalProperties;
    private Map<String, String> propertyDescriptions;
    private Map<String, Object> capabilities;
    private boolean supportsAsync;
    private boolean supportsBatch;
    private boolean supportsStreaming;
    private boolean requiresAuthentication;
    private List<AuthenticationConfig.AuthenticationType> supportedAuthTypes;

    /**
     * Check if adapter supports a specific operation
     * @param operation Operation name
     * @return true if supported
     */
    public boolean supportsOperation(String operation) {
        return supportedOperations != null && supportedOperations.contains(operation);
    }

    /**
     * Check if a property is required
     * @param property Property name
     * @return true if required
     */
    public boolean isPropertyRequired(String property) {
        return requiredProperties != null && requiredProperties.contains(property);
    }
    // Getters and Setters
    public String getAdapterName() {
        return adapterName;
    }
    public void setAdapterName(String adapterName) {
        this.adapterName = adapterName;
    }
    public AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return adapterType;
    }
    public void setAdapterType(AdapterConfiguration.AdapterTypeEnum adapterType) {
        this.adapterType = adapterType;
    }
    public AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return adapterMode;
    }
    public void setAdapterMode(AdapterConfiguration.AdapterModeEnum adapterMode) {
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
    public Map<String, String> getPropertyDescriptions() {
        return propertyDescriptions;
    }
    public void setPropertyDescriptions(Map<String, String> propertyDescriptions) {
        this.propertyDescriptions = propertyDescriptions;
    }
    public Map<String, Object> getCapabilities() {
        return capabilities;
    }
    public void setCapabilities(Map<String, Object> capabilities) {
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
    public boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }
    public void setRequiresAuthentication(boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }
    public List<AuthenticationConfig.AuthenticationType> getSupportedAuthTypes() {
        return supportedAuthTypes;
    }
    public void setSupportedAuthTypes(List<AuthenticationConfig.AuthenticationType> supportedAuthTypes) {
        this.supportedAuthTypes = supportedAuthTypes;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterName;
        private AdapterConfiguration.AdapterTypeEnum adapterType;
        private AdapterConfiguration.AdapterModeEnum adapterMode;
        private String version;
        private String description;
        private List<String> supportedOperations;
        private List<String> requiredProperties;
        private List<String> optionalProperties;
        private Map<String, String> propertyDescriptions;
        private Map<String, Object> capabilities;
        private boolean supportsAsync;
        private boolean supportsBatch;
        private boolean supportsStreaming;
        private boolean requiresAuthentication;
        private List<AuthenticationConfig.AuthenticationType> supportedAuthTypes;

        public Builder adapterName(String adapterName) {
            this.adapterName = adapterName;
            return this;
        }

        public Builder adapterType(AdapterConfiguration.AdapterTypeEnum adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public Builder adapterMode(AdapterConfiguration.AdapterModeEnum adapterMode) {
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

        public Builder propertyDescriptions(Map<String, String> propertyDescriptions) {
            this.propertyDescriptions = propertyDescriptions;
            return this;
        }

        public Builder capabilities(Map<String, Object> capabilities) {
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

        public Builder requiresAuthentication(boolean requiresAuthentication) {
            this.requiresAuthentication = requiresAuthentication;
            return this;
        }

        public Builder supportedAuthTypes(List<AuthenticationConfig.AuthenticationType> supportedAuthTypes) {
            this.supportedAuthTypes = supportedAuthTypes;
            return this;
        }

        public AdapterMetadata build() {
            AdapterMetadata obj = new AdapterMetadata();
            obj.adapterName = this.adapterName;
            obj.adapterType = this.adapterType;
            obj.adapterMode = this.adapterMode;
            obj.version = this.version;
            obj.description = this.description;
            obj.supportedOperations = this.supportedOperations;
            obj.requiredProperties = this.requiredProperties;
            obj.optionalProperties = this.optionalProperties;
            obj.propertyDescriptions = this.propertyDescriptions;
            obj.capabilities = this.capabilities;
            obj.supportsAsync = this.supportsAsync;
            obj.supportsBatch = this.supportsBatch;
            obj.supportsStreaming = this.supportsStreaming;
            obj.requiresAuthentication = this.requiresAuthentication;
            obj.supportedAuthTypes = this.supportedAuthTypes;
            return obj;
        }
    }
}
