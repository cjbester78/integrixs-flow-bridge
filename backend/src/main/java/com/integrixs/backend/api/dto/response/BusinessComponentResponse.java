package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;

/**
 * Response DTO for business component
 */
public class BusinessComponentResponse {
    private String id;
    private String name;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long associatedAdapterCount;

    public static BusinessComponentResponseBuilder builder() {
        return new BusinessComponentResponseBuilder();
    }

            public static class BusinessComponentResponseBuilder {
        private String id;
        private String name;
        private String description;
        private String contactEmail;
        private String contactPhone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long associatedAdapterCount;

        public BusinessComponentResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BusinessComponentResponseBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BusinessComponentResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BusinessComponentResponseBuilder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public BusinessComponentResponseBuilder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public BusinessComponentResponseBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BusinessComponentResponseBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public BusinessComponentResponseBuilder associatedAdapterCount(Long associatedAdapterCount) {
            this.associatedAdapterCount = associatedAdapterCount;
            return this;
        }

        public BusinessComponentResponse build() {
            BusinessComponentResponse response = new BusinessComponentResponse();
            response.id = this.id;
            response.name = this.name;
            response.description = this.description;
            response.contactEmail = this.contactEmail;
            response.contactPhone = this.contactPhone;
            response.createdAt = this.createdAt;
            response.updatedAt = this.updatedAt;
            response.associatedAdapterCount = this.associatedAdapterCount;
            return response;
        }
    }

    // Default constructor
    public BusinessComponentResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getAssociatedAdapterCount() {
        return associatedAdapterCount;
    }

    public void setAssociatedAdapterCount(Long associatedAdapterCount) {
        this.associatedAdapterCount = associatedAdapterCount;
    }
}
