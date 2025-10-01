package com.integrixs.backend.marketplace.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrganizationDto {
    private UUID id;
    private String slug;
    private String name;
    private String description;
    private String website;
    private String logoUrl;
    private boolean verified;
    private LocalDateTime createdAt;

    // Default constructor
    public OrganizationDto() {
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

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    // Builder
    public static OrganizationDtoBuilder builder() {
        return new OrganizationDtoBuilder();
    }

    public static class OrganizationDtoBuilder {
        private UUID id;
        private String slug;
        private String name;
        private String logoUrl;
        private boolean verified;

        public OrganizationDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public OrganizationDtoBuilder slug(String slug) {
            this.slug = slug;
            return this;
        }

        public OrganizationDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public OrganizationDtoBuilder logoUrl(String logoUrl) {
            this.logoUrl = logoUrl;
            return this;
        }

        public OrganizationDtoBuilder verified(boolean verified) {
            this.verified = verified;
            return this;
        }

        public OrganizationDto build() {
            OrganizationDto dto = new OrganizationDto();
            dto.setId(this.id);
            dto.setSlug(this.slug);
            dto.setName(this.name);
            dto.setLogoUrl(this.logoUrl);
            dto.setVerified(this.verified);
            return dto;
        }
    }
}