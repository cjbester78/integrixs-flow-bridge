package com.integrixs.backend.api.dto.request;

import com.integrixs.data.model.TargetFieldMapping;
import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a target field mapping
 */
public class CreateTargetFieldMappingRequest {

    @NotBlank(message = "Source field path is required")
    @Size(max = 500, message = "Source field path cannot exceed 500 characters")
    private String sourceFieldPath;

    @NotBlank(message = "Target field path is required")
    @Size(max = 500, message = "Target field path cannot exceed 500 characters")
    private String targetFieldPath;

    private TargetFieldMapping.MappingType mappingType = TargetFieldMapping.MappingType.DIRECT;

    private String transformationExpression;

    @Size(max = 1000, message = "Constant value cannot exceed 1000 characters")
    private String constantValue;

    private String conditionExpression;

    @Size(max = 1000, message = "Default value cannot exceed 1000 characters")
    private String defaultValue;

    private String targetDataType;

    private boolean required = false;

    @Min(value = 0, message = "Mapping order must be non - negative")
    private Integer mappingOrder;

    private String visualFlowData;

    private String validationRules;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private boolean active = true;

    // Default constructor
    public CreateTargetFieldMappingRequest() {
    }

    public String getSourceFieldPath() {
        return sourceFieldPath;
    }

    public void setSourceFieldPath(String sourceFieldPath) {
        this.sourceFieldPath = sourceFieldPath;
    }

    public String getTargetFieldPath() {
        return targetFieldPath;
    }

    public void setTargetFieldPath(String targetFieldPath) {
        this.targetFieldPath = targetFieldPath;
    }

    public TargetFieldMapping.MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(TargetFieldMapping.MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public String getTransformationExpression() {
        return transformationExpression;
    }

    public void setTransformationExpression(String transformationExpression) {
        this.transformationExpression = transformationExpression;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

    public String getConditionExpression() {
        return conditionExpression;
    }

    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getTargetDataType() {
        return targetDataType;
    }

    public void setTargetDataType(String targetDataType) {
        this.targetDataType = targetDataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Integer getMappingOrder() {
        return mappingOrder;
    }

    public void setMappingOrder(Integer mappingOrder) {
        this.mappingOrder = mappingOrder;
    }

    public String getVisualFlowData() {
        return visualFlowData;
    }

    public void setVisualFlowData(String visualFlowData) {
        this.visualFlowData = visualFlowData;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
