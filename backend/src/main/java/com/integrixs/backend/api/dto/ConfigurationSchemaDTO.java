package com.integrixs.backend.api.dto;

import java.util.Map;

public class ConfigurationSchemaDTO {
    private String direction;
    private Map<String, Object> schema;
    private boolean hasAdvancedOptions;
    private boolean requiresAuthentication;
    private String[] supportedAuthMethods;

    // Default constructor
    public ConfigurationSchemaDTO() {
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Map<String, Object> getSchema() {
        return schema;
    }

    public void setSchema(Map<String, Object> schema) {
        this.schema = schema;
    }

    public boolean isHasAdvancedOptions() {
        return hasAdvancedOptions;
    }

    public void setHasAdvancedOptions(boolean hasAdvancedOptions) {
        this.hasAdvancedOptions = hasAdvancedOptions;
    }

    public boolean isRequiresAuthentication() {
        return requiresAuthentication;
    }

    public void setRequiresAuthentication(boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public String[] getSupportedAuthMethods() {
        return supportedAuthMethods;
    }

    public void setSupportedAuthMethods(String[] supportedAuthMethods) {
        this.supportedAuthMethods = supportedAuthMethods;
    }

    // Builder
    public static ConfigurationSchemaDTOBuilder builder() {
        return new ConfigurationSchemaDTOBuilder();
    }

    public static class ConfigurationSchemaDTOBuilder {
        private String direction;
        private Map<String, Object> schema;
        private boolean hasAdvancedOptions;
        private boolean requiresAuthentication;
        private String[] supportedAuthMethods;

        public ConfigurationSchemaDTOBuilder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public ConfigurationSchemaDTOBuilder schema(Map<String, Object> schema) {
            this.schema = schema;
            return this;
        }

        public ConfigurationSchemaDTOBuilder hasAdvancedOptions(boolean hasAdvancedOptions) {
            this.hasAdvancedOptions = hasAdvancedOptions;
            return this;
        }

        public ConfigurationSchemaDTOBuilder requiresAuthentication(boolean requiresAuthentication) {
            this.requiresAuthentication = requiresAuthentication;
            return this;
        }

        public ConfigurationSchemaDTOBuilder supportedAuthMethods(String[] supportedAuthMethods) {
            this.supportedAuthMethods = supportedAuthMethods;
            return this;
        }

        public ConfigurationSchemaDTO build() {
            ConfigurationSchemaDTO dto = new ConfigurationSchemaDTO();
            dto.setDirection(direction);
            dto.setSchema(schema);
            dto.setHasAdvancedOptions(hasAdvancedOptions);
            dto.setRequiresAuthentication(requiresAuthentication);
            dto.setSupportedAuthMethods(supportedAuthMethods);
            return dto;
        }
    }
}
