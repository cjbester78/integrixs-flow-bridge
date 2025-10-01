package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

public class AdapterCategory {

    private UUID id;

    private String code;

    private String name;

    private String description;

    private String icon;

    private AdapterCategory parentCategory;

    private Integer displayOrder = 0;

        private LocalDateTime createdAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public AdapterCategory getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(AdapterCategory parentCategory) {
        this.parentCategory = parentCategory;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
