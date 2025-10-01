package com.integrixs.backend.dto.configuration;

import java.util.List;

public class ConfigurationDto {
    private String id;
    private String categoryId;
    private String configKey;
    private String configValue;
    private String configType;
    private String defaultValue;
    private String description;
    private Boolean isRequired;
    private Boolean isEncrypted;
    private Boolean isSensitive;
    private String validationRules;
    private List<String> allowedValues;
    private String environment;
    private String profile;

    // Constructors
    public ConfigurationDto() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean getIsEncrypted() {
        return isEncrypted;
    }

    public void setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
    }

    public Boolean getIsSensitive() {
        return isSensitive;
    }

    public void setIsSensitive(Boolean isSensitive) {
        this.isSensitive = isSensitive;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<String> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}