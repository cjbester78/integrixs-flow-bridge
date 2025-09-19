package com.integrixs.backend.marketplace.dto;

import java.util.Map;

/**
 * Request DTO for installing a template
 */
public class InstallTemplateRequest {
    private String templateId;
    private String versionId;
    private String targetBusinessComponentId;
    private String name;
    private String description;
    private Map<String, Object> configuration;
    
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
    
    public String getVersionId() {
        return versionId;
    }
    
    public void setVersionId(String versionId) {
        this.versionId = versionId;
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
}