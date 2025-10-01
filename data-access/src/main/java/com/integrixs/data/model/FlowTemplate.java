package com.integrixs.data.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a flow template in the marketplace
 */
public class FlowTemplate extends BaseEntity {

    private String name;
    private String slug;
    private String description;
    private String shortDescription;
    private UUID organizationId;
    private UUID authorId;
    private String category;
    private String type;
    private String visibility;
    private String status;
    private String iconUrl;
    private String version;
    private String compatibleVersions;
    private String tags;
    private String screenshots;
    private String documentation;
    private String flowDefinition;
    private String configuration;
    private String requirements;
    private String dependencies;
    private boolean isCertified;
    private LocalDateTime certifiedAt;
    private boolean isFeatured;
    private LocalDateTime featuredUntil;
    private int downloadCount;
    private int installCount;
    private double averageRating;
    private int ratingCount;
    private LocalDateTime publishedAt;

    /**
     * Template categories
     */
    public enum TemplateCategory {
        INTEGRATION("Integration"),
        TRANSFORMATION("Transformation"),
        ROUTING("Routing"),
        ERROR_HANDLING("Error Handling"),
        MONITORING("Monitoring"),
        SECURITY("Security"),
        UTILITY("Utility"),
        INDUSTRY_SPECIFIC("Industry Specific"),
        OTHER("Other");

        private final String displayName;

        TemplateCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Template types
     */
    public enum TemplateType {
        FLOW("Flow"),
        ADAPTER("Adapter"),
        TRANSFORMATION("Transformation"),
        COMPOSITE("Composite"),
        STARTER("Starter"),
        COMPLETE_SOLUTION("Complete Solution");

        private final String displayName;

        TemplateType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Template visibility
     */
    public enum TemplateVisibility {
        PUBLIC("Public"),
        PRIVATE("Private"),
        ORGANIZATION("Organization");

        private final String displayName;

        TemplateVisibility(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCompatibleVersions() {
        return compatibleVersions;
    }

    public void setCompatibleVersions(String compatibleVersions) {
        this.compatibleVersions = compatibleVersions;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getScreenshots() {
        return screenshots;
    }

    public void setScreenshots(String screenshots) {
        this.screenshots = screenshots;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getFlowDefinition() {
        return flowDefinition;
    }

    public void setFlowDefinition(String flowDefinition) {
        this.flowDefinition = flowDefinition;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getDependencies() {
        return dependencies;
    }

    public void setDependencies(String dependencies) {
        this.dependencies = dependencies;
    }

    public boolean isCertified() {
        return isCertified;
    }

    public void setCertified(boolean certified) {
        isCertified = certified;
    }

    public LocalDateTime getCertifiedAt() {
        return certifiedAt;
    }

    public void setCertifiedAt(LocalDateTime certifiedAt) {
        this.certifiedAt = certifiedAt;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public LocalDateTime getFeaturedUntil() {
        return featuredUntil;
    }

    public void setFeaturedUntil(LocalDateTime featuredUntil) {
        this.featuredUntil = featuredUntil;
    }

    public int getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(int downloadCount) {
        this.downloadCount = downloadCount;
    }

    public int getInstallCount() {
        return installCount;
    }

    public void setInstallCount(int installCount) {
        this.installCount = installCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
}