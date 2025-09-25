package com.integrixs.backend.marketplace.dto;

import com.integrixs.data.model.FlowTemplate.TemplateCategory;
import com.integrixs.data.model.FlowTemplate.TemplateType;
import com.integrixs.data.model.FlowTemplate.TemplateVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public class CreateTemplateRequest {

    @NotBlank(message = "Template name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Size(max = 5000, message = "Detailed description must not exceed 5000 characters")
    private String detailedDescription;

    @NotNull(message = "Category is required")
    private TemplateCategory category;

    @NotNull(message = "Type is required")
    private TemplateType type;

    @NotNull(message = "Visibility is required")
    private TemplateVisibility visibility = TemplateVisibility.PUBLIC;

    private UUID organizationId;

    @NotBlank(message = "Flow definition is required")
    private String flowDefinition;

    private String configurationSchema;

    @NotNull(message = "Tags are required")
    @Size(min = 1, max = 10, message = "Must have between 1 and 10 tags")
    private List<String> tags;

    private List<String> requirements;

    private String minPlatformVersion;

    private String maxPlatformVersion;

    private String documentationUrl;

    private String sourceRepositoryUrl;

    // Default constructor
    public CreateTemplateRequest() {
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

    public String getDetailedDescription() {
        return detailedDescription;
    }

    public void setDetailedDescription(String detailedDescription) {
        this.detailedDescription = detailedDescription;
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

    public TemplateVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(TemplateVisibility visibility) {
        this.visibility = visibility;
    }

    public UUID getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(UUID organizationId) {
        this.organizationId = organizationId;
    }

    public String getFlowDefinition() {
        return flowDefinition;
    }

    public void setFlowDefinition(String flowDefinition) {
        this.flowDefinition = flowDefinition;
    }

    public String getConfigurationSchema() {
        return configurationSchema;
    }

    public void setConfigurationSchema(String configurationSchema) {
        this.configurationSchema = configurationSchema;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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
