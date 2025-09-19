package com.integrixs.backend.marketplace.dto;

import java.time.LocalDateTime;

/**
 * DTO for template version information
 */
public class TemplateVersionDto {
    private String id;
    private String templateId;
    private String version;
    private String releaseNotes;
    private String downloadUrl;
    private LocalDateTime releasedAt;
    private boolean isLatest;
    private boolean isCompatible;
    private String minVersion;
    private String maxVersion;
    
    // Default constructor
    public TemplateVersionDto() {
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
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
    
    public String getReleaseNotes() {
        return releaseNotes;
    }
    
    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }
    
    public String getDownloadUrl() {
        return downloadUrl;
    }
    
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
    
    public LocalDateTime getReleasedAt() {
        return releasedAt;
    }
    
    public void setReleasedAt(LocalDateTime releasedAt) {
        this.releasedAt = releasedAt;
    }
    
    public boolean isLatest() {
        return isLatest;
    }
    
    public void setLatest(boolean isLatest) {
        this.isLatest = isLatest;
    }
    
    public boolean isCompatible() {
        return isCompatible;
    }
    
    public void setCompatible(boolean isCompatible) {
        this.isCompatible = isCompatible;
    }
    
    public String getMinVersion() {
        return minVersion;
    }
    
    public void setMinVersion(String minVersion) {
        this.minVersion = minVersion;
    }
    
    public String getMaxVersion() {
        return maxVersion;
    }
    
    public void setMaxVersion(String maxVersion) {
        this.maxVersion = maxVersion;
    }
    
    // Builder
    public static TemplateVersionDtoBuilder builder() {
        return new TemplateVersionDtoBuilder();
    }
    
    public static class TemplateVersionDtoBuilder {
        private String id;
        private String templateId;
        private String version;
        private String releaseNotes;
        private String downloadUrl;
        private LocalDateTime releasedAt;
        private boolean isLatest;
        private boolean isCompatible;
        private String minVersion;
        private String maxVersion;
        
        public TemplateVersionDtoBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public TemplateVersionDtoBuilder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }
        
        public TemplateVersionDtoBuilder version(String version) {
            this.version = version;
            return this;
        }
        
        public TemplateVersionDtoBuilder releaseNotes(String releaseNotes) {
            this.releaseNotes = releaseNotes;
            return this;
        }
        
        public TemplateVersionDtoBuilder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }
        
        public TemplateVersionDtoBuilder releasedAt(LocalDateTime releasedAt) {
            this.releasedAt = releasedAt;
            return this;
        }
        
        public TemplateVersionDtoBuilder isLatest(boolean isLatest) {
            this.isLatest = isLatest;
            return this;
        }
        
        public TemplateVersionDtoBuilder isCompatible(boolean isCompatible) {
            this.isCompatible = isCompatible;
            return this;
        }
        
        public TemplateVersionDtoBuilder minVersion(String minVersion) {
            this.minVersion = minVersion;
            return this;
        }
        
        public TemplateVersionDtoBuilder maxVersion(String maxVersion) {
            this.maxVersion = maxVersion;
            return this;
        }
        
        public TemplateVersionDto build() {
            TemplateVersionDto dto = new TemplateVersionDto();
            dto.setId(this.id);
            dto.setTemplateId(this.templateId);
            dto.setVersion(this.version);
            dto.setReleaseNotes(this.releaseNotes);
            dto.setDownloadUrl(this.downloadUrl);
            dto.setReleasedAt(this.releasedAt);
            dto.setLatest(this.isLatest);
            dto.setCompatible(this.isCompatible);
            dto.setMinVersion(this.minVersion);
            dto.setMaxVersion(this.maxVersion);
            return dto;
        }
    }
}