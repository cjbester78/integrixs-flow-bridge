package com.integrixs.backend.marketplace.dto;

import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateCategory;
import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateType;
import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
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
}