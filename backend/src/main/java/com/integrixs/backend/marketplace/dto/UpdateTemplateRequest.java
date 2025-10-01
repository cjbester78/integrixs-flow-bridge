package com.integrixs.backend.marketplace.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class UpdateTemplateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    private String detailedDescription;
    private String category;
    private List<String> tags;
    private List<String> requirements;
    private String visibility;
    private String documentationUrl;
    private String sourceRepositoryUrl;
    private String icon;
    private String templateData;
    private String documentation;

    // Default constructor
    public UpdateTemplateRequest() {
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
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

    public String getTemplateData() {
        return templateData;
    }

    public void setTemplateData(String templateData) {
        this.templateData = templateData;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
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
}