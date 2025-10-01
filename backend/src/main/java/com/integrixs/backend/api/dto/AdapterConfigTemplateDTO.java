package com.integrixs.backend.api.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class AdapterConfigTemplateDTO {
    private UUID id;
    private UUID adapterTypeId;
    private String adapterTypeName;
    private String name;
    private String description;
    private String direction;
    private Map<String, Object> configuration;
    private boolean isDefault;
    private boolean isPublic;
    private String[] tags;
    private LocalDateTime createdAt;
    private String createdByUsername;

    // Default constructor
    public AdapterConfigTemplateDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAdapterTypeId() {
        return adapterTypeId;
    }

    public void setAdapterTypeId(UUID adapterTypeId) {
        this.adapterTypeId = adapterTypeId;
    }

    public String getAdapterTypeName() {
        return adapterTypeName;
    }

    public void setAdapterTypeName(String adapterTypeName) {
        this.adapterTypeName = adapterTypeName;
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public boolean isIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }
}
