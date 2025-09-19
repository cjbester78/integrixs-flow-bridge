package com.integrixs.backend.marketplace.dto;

/**
 * Request DTO for featuring a template
 */
public class FeatureTemplateRequest {
    private String templateId;
    private boolean featured;
    private String reason;
    
    // Default constructor
    public FeatureTemplateRequest() {
    }
    
    // Getters and setters
    public String getTemplateId() {
        return templateId;
    }
    
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
    
    public boolean isFeatured() {
        return featured;
    }
    
    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}