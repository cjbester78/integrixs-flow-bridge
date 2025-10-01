package com.integrixs.backend.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
public class PublishVersionRequest {
    @NotBlank
    private String version;

    @NotBlank
    private String releaseNotes;

    @NotBlank
    private String flowDefinition;

    private boolean stable;
    private String minPlatformVersion;
    private String maxPlatformVersion;
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

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public String getFlowDefinition() {
        return flowDefinition;
    }

    public void setFlowDefinition(String flowDefinition) {
        this.flowDefinition = flowDefinition;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    public String getMinPlatformVersion() {
        return minPlatformVersion;
    }

    public void setMinPlatformVersion(String minPlatformVersion) {
        this.minPlatformVersion = minPlatformVersion;
    }

    public String getMaxPlatformVersion() {
        return maxPlatformVersion;
    }

    public void setMaxPlatformVersion(String maxPlatformVersion) {
        this.maxPlatformVersion = maxPlatformVersion;
    }

    public boolean isIsBreakingChange() {
        return isBreakingChange;
    }

    public void setIsBreakingChange(boolean isBreakingChange) {
        this.isBreakingChange = isBreakingChange;
    }
}