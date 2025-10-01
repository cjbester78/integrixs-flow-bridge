package com.integrixs.backend.dto.configuration;

import java.util.UUID;

public class ConfigurationCategoryDto {
    private String id;
    private String code;
    private String name;
    private String description;
    private String parentCategoryId;
    private Integer displayOrder;
    private Boolean isActive;

    // Constructors
    public ConfigurationCategoryDto() {
    }

    public ConfigurationCategoryDto(String id, String code, String name, String description,
                                  String parentCategoryId, Integer displayOrder, Boolean isActive) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.parentCategoryId = parentCategoryId;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(String parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}