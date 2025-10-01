package com.integrixs.backend.marketplace.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Result DTO for template installation
 */
public class InstallationResultDto {
    private boolean success;
    private String installationId;
    private String templateId;
    private String templateName;
    private String versionId;
    private String version;
    private String businessComponentId;
    private String flowId;
    private String status;
    private LocalDateTime installedAt;
    private List<String> createdResources;
    private Map<String, Object> configuration;
    private String message;

    // Default constructor
    public InstallationResultDto() {
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getInstallationId() {
        return installationId;
    }

    public void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getBusinessComponentId() {
        return businessComponentId;
    }

    public void setBusinessComponentId(String businessComponentId) {
        this.businessComponentId = businessComponentId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public List<String> getCreatedResources() {
        return createdResources;
    }

    public void setCreatedResources(List<String> createdResources) {
        this.createdResources = createdResources;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Builder
    public static InstallationResultDtoBuilder builder() {
        return new InstallationResultDtoBuilder();
    }

    public static class InstallationResultDtoBuilder {
        private boolean success;
        private String installationId;
        private String templateId;
        private String templateName;
        private String versionId;
        private String version;
        private String businessComponentId;
        private String flowId;
        private String status;
        private LocalDateTime installedAt;
        private List<String> createdResources;
        private Map<String, Object> configuration;
        private String message;

        public InstallationResultDtoBuilder success(boolean success) {
            this.success = success;
            return this;
        }

        public InstallationResultDtoBuilder installationId(String installationId) {
            this.installationId = installationId;
            return this;
        }

        public InstallationResultDtoBuilder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public InstallationResultDtoBuilder templateName(String templateName) {
            this.templateName = templateName;
            return this;
        }

        public InstallationResultDtoBuilder versionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public InstallationResultDtoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public InstallationResultDtoBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public InstallationResultDtoBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public InstallationResultDtoBuilder status(String status) {
            this.status = status;
            return this;
        }

        public InstallationResultDtoBuilder installedAt(LocalDateTime installedAt) {
            this.installedAt = installedAt;
            return this;
        }

        public InstallationResultDtoBuilder createdResources(List<String> createdResources) {
            this.createdResources = createdResources;
            return this;
        }

        public InstallationResultDtoBuilder configuration(Map<String, Object> configuration) {
            this.configuration = configuration;
            return this;
        }

        public InstallationResultDtoBuilder message(String message) {
            this.message = message;
            return this;
        }

        public InstallationResultDto build() {
            InstallationResultDto dto = new InstallationResultDto();
            dto.setSuccess(this.success);
            dto.setInstallationId(this.installationId);
            dto.setTemplateId(this.templateId);
            dto.setTemplateName(this.templateName);
            dto.setVersionId(this.versionId);
            dto.setVersion(this.version);
            dto.setBusinessComponentId(this.businessComponentId);
            dto.setFlowId(this.flowId);
            dto.setStatus(this.status);
            dto.setInstalledAt(this.installedAt);
            dto.setCreatedResources(this.createdResources);
            dto.setConfiguration(this.configuration);
            dto.setMessage(this.message);
            return dto;
        }
    }
}