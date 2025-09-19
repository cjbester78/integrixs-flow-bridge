package com.integrixs.backend.marketplace.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class InstallationDto {
    private UUID id;
    private UUID templateId;
    private String templateName;
    private String templateVersion;
    private UUID installedBy;
    private LocalDateTime installedAt;
    private LocalDateTime lastUsedAt;
    private boolean active;

    // Default constructor
    public InstallationDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTemplateId() {
        return templateId;
    }

    public void setTemplateId(UUID templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(String templateVersion) {
        this.templateVersion = templateVersion;
    }

    public UUID getInstalledBy() {
        return installedBy;
    }

    public void setInstalledBy(UUID installedBy) {
        this.installedBy = installedBy;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}