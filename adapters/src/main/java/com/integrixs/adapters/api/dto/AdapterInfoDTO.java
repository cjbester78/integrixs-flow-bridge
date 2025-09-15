package com.integrixs.adapters.api.dto;

/**
 * DTO for adapter information
 */
public class AdapterInfoDTO {
    public AdapterInfoDTO() {
    }


    private String adapterId;
    private String name;
    private String description;
    private String adapterType;
    private String adapterMode;
    private boolean isActive;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    // Getters and Setters
    public String getAdapterId() {
        return adapterId;
    }
    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
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
    public String getAdapterType() {
        return adapterType;
    }
    public void setAdapterType(String adapterType) {
        this.adapterType = adapterType;
    }
    public String getAdapterMode() {
        return adapterMode;
    }
    public void setAdapterMode(String adapterMode) {
        this.adapterMode = adapterMode;
    }
    public boolean isActive() {
        return isActive;
    }
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Long getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
    public Long getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String adapterId;
        private String name;
        private String description;
        private String adapterType;
        private String adapterMode;
        private boolean isActive;
        private String status;
        private Long createdAt;
        private Long updatedAt;

        public Builder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder adapterType(String adapterType) {
            this.adapterType = adapterType;
            return this;
        }

        public Builder adapterMode(String adapterMode) {
            this.adapterMode = adapterMode;
            return this;
        }

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder createdAt(Long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Long updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public AdapterInfoDTO build() {
            AdapterInfoDTO obj = new AdapterInfoDTO();
            obj.adapterId = this.adapterId;
            obj.name = this.name;
            obj.description = this.description;
            obj.adapterType = this.adapterType;
            obj.adapterMode = this.adapterMode;
            obj.isActive = this.isActive;
            obj.status = this.status;
            obj.createdAt = this.createdAt;
            obj.updatedAt = this.updatedAt;
            return obj;
        }
    }
}
