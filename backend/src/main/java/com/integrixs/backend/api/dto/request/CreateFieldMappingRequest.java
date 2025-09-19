package com.integrixs.backend.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for creating a field mapping
 */
public class CreateFieldMappingRequest {

    @NotNull(message = "Source fields are required")
    private List<String> sourceFields;

    private String targetField; // Deprecated - use targetFields

    private List<String> targetFields; // New - supports 1 - to - many mappings

    private String mappingType = "DIRECT"; // DIRECT, SPLIT, AGGREGATE, CONDITIONAL, ITERATE

    private Object splitConfiguration; // Configuration for SPLIT type mappings

    private String javaFunction;
    private String mappingRule;
    private String inputTypes;
    private String outputType;
    private String description;
    private String functionName;
    private boolean active = true;
    private boolean arrayMapping = false;
    private String arrayContextPath;
    private String sourceXPath;
    private String targetXPath;
    private Integer mappingOrder;
    private Object visualFlowData;
    private Object functionNode;

    // Helper methods for backward compatibility
    public List<String> getTargetFieldsList() {
        if(targetFields != null && !targetFields.isEmpty()) {
            return targetFields;
        } else if(targetField != null && !targetField.isEmpty()) {
            return List.of(targetField);
        }
        return List.of();
    }

    public void setTargetFieldsList(List<String> fields) {
        this.targetFields = fields;
        // Set single field for backward compatibility
        if(fields != null && fields.size() == 1) {
            this.targetField = fields.get(0);
        } else {
            this.targetField = null;
        }
    }

    // Default constructor
    public CreateFieldMappingRequest() {
    }

    public List<String> getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(List<String> sourceFields) {
        this.sourceFields = sourceFields;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public List<String> getTargetFields() {
        return targetFields;
    }

    public void setTargetFields(List<String> targetFields) {
        this.targetFields = targetFields;
    }

    public String getMappingType() {
        return mappingType;
    }

    public void setMappingType(String mappingType) {
        this.mappingType = mappingType;
    }

    public Object getSplitConfiguration() {
        return splitConfiguration;
    }

    public void setSplitConfiguration(Object splitConfiguration) {
        this.splitConfiguration = splitConfiguration;
    }

    public String getJavaFunction() {
        return javaFunction;
    }

    public void setJavaFunction(String javaFunction) {
        this.javaFunction = javaFunction;
    }

    public String getMappingRule() {
        return mappingRule;
    }

    public void setMappingRule(String mappingRule) {
        this.mappingRule = mappingRule;
    }

    public String getInputTypes() {
        return inputTypes;
    }

    public void setInputTypes(String inputTypes) {
        this.inputTypes = inputTypes;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isArrayMapping() {
        return arrayMapping;
    }

    public void setArrayMapping(boolean arrayMapping) {
        this.arrayMapping = arrayMapping;
    }

    public String getArrayContextPath() {
        return arrayContextPath;
    }

    public void setArrayContextPath(String arrayContextPath) {
        this.arrayContextPath = arrayContextPath;
    }

    public String getSourceXPath() {
        return sourceXPath;
    }

    public void setSourceXPath(String sourceXPath) {
        this.sourceXPath = sourceXPath;
    }

    public String getTargetXPath() {
        return targetXPath;
    }

    public void setTargetXPath(String targetXPath) {
        this.targetXPath = targetXPath;
    }

    public Integer getMappingOrder() {
        return mappingOrder;
    }

    public void setMappingOrder(Integer mappingOrder) {
        this.mappingOrder = mappingOrder;
    }

    public Object getVisualFlowData() {
        return visualFlowData;
    }

    public void setVisualFlowData(Object visualFlowData) {
        this.visualFlowData = visualFlowData;
    }

    public Object getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(Object functionNode) {
        this.functionNode = functionNode;
    }
}
