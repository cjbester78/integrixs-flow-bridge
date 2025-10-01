package com.integrixs.backend.api.dto.response;

import com.integrixs.data.model.TargetFieldMapping;
import java.time.LocalDateTime;

/**
 * Response DTO for target field mapping
 */
public class TargetFieldMappingResponse {

    private String id;
    private String orchestrationTargetId;
    private String sourceFieldPath;
    private String targetFieldPath;
    private TargetFieldMapping.MappingType mappingType;
    private String transformationExpression;
    private String constantValue;
    private String conditionExpression;
    private String defaultValue;
    private String targetDataType;
    private boolean required;
    private Integer mappingOrder;
    private String visualFlowData;
    private String validationRules;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Default constructor
    public TargetFieldMappingResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrchestrationTargetId() {
        return orchestrationTargetId;
    }

    public void setOrchestrationTargetId(String orchestrationTargetId) {
        this.orchestrationTargetId = orchestrationTargetId;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
