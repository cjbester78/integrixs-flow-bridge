package com.integrixs.backend.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for field mapping
 */
public class FieldMappingResponse {

    private String id;
    private String transformationId;
    private List<String> sourceFields;
    private String targetField; // Deprecated - use targetFields
    private List<String> targetFields; // New - supports 1 - to - many mappings
    private String mappingType; // DIRECT, SPLIT, AGGREGATE, CONDITIONAL, ITERATE
    private Object splitConfiguration; // Configuration for SPLIT type mappings
    private String javaFunction;
    private String mappingRule;
    private String inputTypes;
    private String outputType;
    private String description;
    private String version;
    private String functionName;
    private boolean active;
    private boolean arrayMapping;
    private String arrayContextPath;
    private String sourceXPath;
    private String targetXPath;
    private Integer mappingOrder;
    private Object visualFlowData;
    private Object functionNode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Helper methods for backward compatibility
    public List<String> getTargetFieldsList() {
        if(targetFields != null && !targetFields.isEmpty()) {
            return targetFields;
        } else if(targetField != null && !targetField.isEmpty()) {
            return List.of(targetField);
        }
        return List.of();
    }

    public boolean isOneToManyMapping() {
        return targetFields != null && targetFields.size() > 1;
    }

    // Default constructor
    public FieldMappingResponse() {
    }

    // Builder
    public static FieldMappingResponseBuilder builder() {
        return new FieldMappingResponseBuilder();
    }

    public static class FieldMappingResponseBuilder {
        private FieldMappingResponse response = new FieldMappingResponse();

        public FieldMappingResponseBuilder id(String id) {
            response.id = id;
            return this;
        }

        public FieldMappingResponseBuilder transformationId(String transformationId) {
            response.transformationId = transformationId;
            return this;
        }

        public FieldMappingResponseBuilder sourceFields(List<String> sourceFields) {
            response.sourceFields = sourceFields;
            return this;
        }

        public FieldMappingResponseBuilder targetField(String targetField) {
            response.targetField = targetField;
            return this;
        }

        public FieldMappingResponseBuilder targetFields(List<String> targetFields) {
            response.targetFields = targetFields;
            return this;
        }

        public FieldMappingResponseBuilder mappingType(String mappingType) {
            response.mappingType = mappingType;
            return this;
        }

        public FieldMappingResponseBuilder splitConfiguration(Object splitConfiguration) {
            response.splitConfiguration = splitConfiguration;
            return this;
        }

        public FieldMappingResponseBuilder javaFunction(String javaFunction) {
            response.javaFunction = javaFunction;
            return this;
        }

        public FieldMappingResponseBuilder mappingRule(String mappingRule) {
            response.mappingRule = mappingRule;
            return this;
        }

        public FieldMappingResponseBuilder inputTypes(String inputTypes) {
            response.inputTypes = inputTypes;
            return this;
        }

        public FieldMappingResponseBuilder outputType(String outputType) {
            response.outputType = outputType;
            return this;
        }

        public FieldMappingResponseBuilder description(String description) {
            response.description = description;
            return this;
        }

        public FieldMappingResponseBuilder version(String version) {
            response.version = version;
            return this;
        }

        public FieldMappingResponseBuilder functionName(String functionName) {
            response.functionName = functionName;
            return this;
        }

        public FieldMappingResponseBuilder active(boolean active) {
            response.active = active;
            return this;
        }

        public FieldMappingResponseBuilder arrayMapping(boolean arrayMapping) {
            response.arrayMapping = arrayMapping;
            return this;
        }

        public FieldMappingResponseBuilder arrayContextPath(String arrayContextPath) {
            response.arrayContextPath = arrayContextPath;
            return this;
        }

        public FieldMappingResponseBuilder sourceXPath(String sourceXPath) {
            response.sourceXPath = sourceXPath;
            return this;
        }

        public FieldMappingResponseBuilder targetXPath(String targetXPath) {
            response.targetXPath = targetXPath;
            return this;
        }

        public FieldMappingResponseBuilder mappingOrder(Integer mappingOrder) {
            response.mappingOrder = mappingOrder;
            return this;
        }

        public FieldMappingResponseBuilder visualFlowData(Object visualFlowData) {
            response.visualFlowData = visualFlowData;
            return this;
        }

        public FieldMappingResponseBuilder functionNode(Object functionNode) {
            response.functionNode = functionNode;
            return this;
        }

        public FieldMappingResponseBuilder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public FieldMappingResponseBuilder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public FieldMappingResponse build() {
            return response;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransformationId() {
        return transformationId;
    }

    public void setTransformationId(String transformationId) {
        this.transformationId = transformationId;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
}
