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
}
