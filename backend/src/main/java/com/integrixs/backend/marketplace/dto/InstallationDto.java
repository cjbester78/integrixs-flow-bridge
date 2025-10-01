package com.integrixs.backend.marketplace.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class InstallationDto {
    private UUID id;
    private TemplateDto template;
    private String version;
    private UUID flowId;
    private LocalDateTime installedAt;
    private LocalDateTime lastUpdated;
    private boolean autoUpdateEnabled;
    private UUID templateId;
    private String templateName;
    private String templateVersion;
    private UUID installedBy;
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

    public TemplateDto getTemplate() {
        return template;
    }

    public void setTemplate(TemplateDto template) {
        this.template = template;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UUID getFlowId() {
        return flowId;
    }

    public void setFlowId(UUID flowId) {
        this.flowId = flowId;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }

    public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
        this.autoUpdateEnabled = autoUpdateEnabled;
    }

    // Builder
    public static InstallationDtoBuilder builder() {
        return new InstallationDtoBuilder();
    }

    public static class InstallationDtoBuilder {
        private UUID id;
        private TemplateDto template;
        private String version;
        private UUID flowId;
        private LocalDateTime installedAt;
        private LocalDateTime lastUpdated;
        private boolean autoUpdateEnabled;

        public InstallationDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public InstallationDtoBuilder template(TemplateDto template) {
            this.template = template;
            return this;
        }

        public InstallationDtoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public InstallationDtoBuilder flowId(UUID flowId) {
            this.flowId = flowId;
            return this;
        }

        public InstallationDtoBuilder installedAt(LocalDateTime installedAt) {
            this.installedAt = installedAt;
            return this;
        }

        public InstallationDtoBuilder lastUpdated(LocalDateTime lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public InstallationDtoBuilder autoUpdateEnabled(boolean autoUpdateEnabled) {
            this.autoUpdateEnabled = autoUpdateEnabled;
            return this;
        }

        public InstallationDto build() {
            InstallationDto dto = new InstallationDto();
            dto.setId(this.id);
            dto.setTemplate(this.template);
            dto.setVersion(this.version);
            dto.setFlowId(this.flowId);
            dto.setInstalledAt(this.installedAt);
            dto.setLastUpdated(this.lastUpdated);
            dto.setAutoUpdateEnabled(this.autoUpdateEnabled);
            return dto;
        }
    }
}