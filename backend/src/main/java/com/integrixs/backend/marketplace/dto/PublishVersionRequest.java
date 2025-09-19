package com.integrixs.backend.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
public class PublishVersionRequest {
    @NotBlank
    private String version;
    
    @NotBlank
    private String changelog;
    
    private boolean isBreakingChange;

    // Default constructor
    public PublishVersionRequest() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public boolean isIsBreakingChange() {
        return isBreakingChange;
    }

    public void setIsBreakingChange(boolean isBreakingChange) {
        this.isBreakingChange = isBreakingChange;
    }
}