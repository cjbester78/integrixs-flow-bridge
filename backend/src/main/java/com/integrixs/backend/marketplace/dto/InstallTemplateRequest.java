package com.integrixs.backend.marketplace.dto;

import java.util.Map;

/**
 * Request DTO for installing a template
 */
public class InstallTemplateRequest {
    private String templateId;
    private String version;
    private String targetBusinessComponentId;
    private String name;
    private String description;
    private Map<String, Object> configuration;
    private boolean enableAutoUpdate;
    private String organizationId;

    // Default constructor
    public InstallTemplateRequest() {
    }

    // Getters and setters
    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTargetBusinessComponentId() {
        return targetBusinessComponentId;
    }

    public void setTargetBusinessComponentId(String targetBusinessComponentId) {
        this.targetBusinessComponentId = targetBusinessComponentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public boolean isEnableAutoUpdate() {
        return enableAutoUpdate;
    }

    public void setEnableAutoUpdate(boolean enableAutoUpdate) {
        this.enableAutoUpdate = enableAutoUpdate;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }
}