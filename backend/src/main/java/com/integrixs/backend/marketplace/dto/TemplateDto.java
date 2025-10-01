package com.integrixs.backend.marketplace.dto;

import com.integrixs.data.model.FlowTemplate.TemplateCategory;
import com.integrixs.data.model.FlowTemplate.TemplateType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TemplateDto {
    private UUID id;
    private String slug;
    private String name;
    private String description;
    private TemplateCategory category;
    private TemplateType type;
    private AuthorDto author;
    private OrganizationDto organization;
    private String version;
    private String iconUrl;
    private List<String> tags;
    private Long downloadCount;
    private Long installCount;
    private Double averageRating;
    private Long ratingCount;
    private boolean certified;
    private boolean featured;
    private LocalDateTime publishedAt;

    // Default constructor
    public TemplateDto() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
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

    public TemplateCategory getCategory() {
        return category;
    }

    public void setCategory(TemplateCategory category) {
        this.category = category;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public boolean isCertified() {
        return certified;
    }

    public void setCertified(boolean certified) {
        this.certified = certified;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    // Builder
    public static TemplateDtoBuilder builder() {
        return new TemplateDtoBuilder();
    }

    public static class TemplateDtoBuilder {
        private UUID id;
        private String slug;
        private String name;
        private String description;
        private TemplateCategory category;
        private TemplateType type;
        private AuthorDto author;
        private OrganizationDto organization;
        private String version;
        private String iconUrl;
        private List<String> tags;
        private Long downloadCount;
        private Long installCount;
        private Double averageRating;
        private Long ratingCount;
        private boolean certified;
        private boolean featured;
        private LocalDateTime publishedAt;

        public TemplateDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public TemplateDtoBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public TemplateDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TemplateDtoBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TemplateDtoBuilder category(TemplateCategory category) {
            this.category = category;
            return this;
        }

        public TemplateDtoBuilder type(TemplateType type) {
            this.type = type;
            return this;
        }

        public TemplateDtoBuilder author(AuthorDto author) {
            this.author = author;
            return this;
        }

        public TemplateDtoBuilder organization(OrganizationDto organization) {
            this.organization = organization;
            return this;
        }

        public TemplateDtoBuilder version(String version) {
            this.version = version;
            return this;
        }

        public TemplateDtoBuilder iconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public TemplateDtoBuilder tags(List<String> tags) {
            this.tags = tags;
            return this;
        }

        public TemplateDtoBuilder downloadCount(Long downloadCount) {
            this.downloadCount = downloadCount;
            return this;
        }

        public TemplateDtoBuilder installCount(Long installCount) {
            this.installCount = installCount;
            return this;
        }

        public TemplateDtoBuilder averageRating(Double averageRating) {
            this.averageRating = averageRating;
            return this;
        }

        public TemplateDtoBuilder ratingCount(Long ratingCount) {
            this.ratingCount = ratingCount;
            return this;
        }

        public TemplateDtoBuilder certified(boolean certified) {
            this.certified = certified;
            return this;
        }

        public TemplateDtoBuilder featured(boolean featured) {
            this.featured = featured;
            return this;
        }

        public TemplateDtoBuilder publishedAt(LocalDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public TemplateDto build() {
            TemplateDto dto = new TemplateDto();
            dto.setId(this.id);
            dto.setSlug(this.slug);
            dto.setName(this.name);
            dto.setDescription(this.description);
            dto.setCategory(this.category);
            dto.setType(this.type);
            dto.setAuthor(this.author);
            dto.setOrganization(this.organization);
            dto.setVersion(this.version);
            dto.setIconUrl(this.iconUrl);
            dto.setTags(this.tags);
            dto.setDownloadCount(this.downloadCount);
            dto.setInstallCount(this.installCount);
            dto.setAverageRating(this.averageRating);
            dto.setRatingCount(this.ratingCount);
            dto.setCertified(this.certified);
            dto.setFeatured(this.featured);
            dto.setPublishedAt(this.publishedAt);
            return dto;
        }
    }
}
