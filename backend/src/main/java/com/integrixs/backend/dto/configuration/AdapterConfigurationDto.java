package com.integrixs.backend.dto.configuration;

public class AdapterConfigurationDto {
    private String id;
    private String adapterTypeId;
    private String configKey;
    private String configValue;
    private String configType;
    private String defaultValue;
    private String description;
    private Boolean isRequired;
    private Boolean isEncrypted;
    private String validationRules;
    private Integer displayOrder;
    private String uiComponent;
    private String uiOptions;

    // Constructors
    public AdapterConfigurationDto() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAdapterTypeId() {
        return adapterTypeId;
    }

    public void setAdapterTypeId(String adapterTypeId) {
        this.adapterTypeId = adapterTypeId;
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

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getUiComponent() {
        return uiComponent;
    }

    public void setUiComponent(String uiComponent) {
        this.uiComponent = uiComponent;
    }

    public String getUiOptions() {
        return uiOptions;
    }

    public void setUiOptions(String uiOptions) {
        this.uiOptions = uiOptions;
    }
}