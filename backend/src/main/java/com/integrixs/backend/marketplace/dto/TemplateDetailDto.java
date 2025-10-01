package com.integrixs.backend.marketplace.dto;

import com.integrixs.data.model.FlowTemplate.TemplateCategory;
import com.integrixs.data.model.FlowTemplate.TemplateType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TemplateDetailDto {
    private UUID id;
    private String name;
    private String description;
    private String detailedDescription;
    private String slug;
    private TemplateCategory category;
    private TemplateType type;
    private AuthorDto author;
    private OrganizationDto organization;
    private List<String> tags;
    private String iconUrl;
    private boolean featured;
    private boolean certified;
    private String version;
    private String documentationUrl;
    private String sourceRepositoryUrl;
    private List<String> requirements;
    private String minPlatformVersion;
    private String maxPlatformVersion;
    private Long downloadCount;
    private Long installCount;
    private Double averageRating;
    private Long ratingCount;
    private List<TemplateVersionDto> versions;
    private List<TemplateDto> dependencies;
    private UUID organizationId;
    private String organizationName;
    private LocalDateTime publishedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastUpdatedAt;
    private int downloads;
    private double rating;
    private int reviewCount;
    private String templateData;
    private List<String> screenshots;
    private String documentation;
    private String icon;

    // Default constructor
    public TemplateDetailDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public TemplateCategory getCategory() {
        return category;
    }

    public void setCategory(String category) {
        // This method exists for backward compatibility
        // Should use setCategory(TemplateCategory) when possible
        if (category != null) {
            try {
                this.category = TemplateCategory.valueOf(category.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Handle invalid category
            }
        }
    }

    public void setCategory(TemplateCategory category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isCertified() {
        return certified;
    }

    public void setCertified(boolean certified) {
        this.certified = certified;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public String getTemplateData() {
        return templateData;
    }

    public void setTemplateData(String templateData) {
        this.templateData = templateData;
    }

    public List<String> getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(List<String> screenshots) {
        this.screenshots = screenshots;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    // New getters and setters
    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public TemplateType getType() {
        return type;
    }

    public void setType(TemplateType type) {
        this.type = type;
    }

    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
    }

    public OrganizationDto getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDto organization) {
        this.organization = organization;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getSourceRepositoryUrl() {
        return sourceRepositoryUrl;
    }

    public void setSourceRepositoryUrl(String sourceRepositoryUrl) {
        this.sourceRepositoryUrl = sourceRepositoryUrl;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
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

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Long getInstallCount() {
        return installCount;
    }

    public void setInstallCount(Long installCount) {
        this.installCount = installCount;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Long getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Long ratingCount) {
        this.ratingCount = ratingCount;
    }

    public List<TemplateVersionDto> getVersions() {
        return versions;
    }

    public void setVersions(List<TemplateVersionDto> versions) {
        this.versions = versions;
    }

    public List<TemplateDto> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<TemplateDto> dependencies) {
        this.dependencies = dependencies;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}