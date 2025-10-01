package com.integrixs.shared.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * DTO for field mapping data.
 */
public class FieldMappingDTO {

    private String id;
    private String transformationId;
    private List<String> sourceFields;
    private String targetField;
    private List<String> targetFields;
    private String mappingType;
    private Object splitConfiguration;
    private String javaFunction;
    private String mappingRule;
    private String sourceXPath;
    private String targetXPath;
    private boolean isArrayMapping;
    private String arrayContextPath;
    private boolean namespaceAware;
    private String inputTypes;
    private String outputType;
    private String description;
    private String version;
    private String functionName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String name;
    private List<String> sourcePaths;
    private String targetPath;
    private boolean requiresTransformation;
    private Object functionNode;
    private Object visualFlowData;
    private Integer mappingOrder;

    // Default constructor
    public FieldMappingDTO() {
        this.sourceFields = new ArrayList<>();
        this.targetFields = new ArrayList<>();
        this.sourcePaths = new ArrayList<>();
    }

    // All args constructor
    public FieldMappingDTO(String id, String transformationId, List<String> sourceFields, String targetField, List<String> targetFields, String mappingType, Object splitConfiguration, String javaFunction, String mappingRule, String sourceXPath, String targetXPath, boolean isArrayMapping, String arrayContextPath, boolean namespaceAware, String inputTypes, String outputType, String description, String version, String functionName, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt, String name, List<String> sourcePaths, String targetPath, boolean requiresTransformation, Object functionNode, Object visualFlowData, Integer mappingOrder) {
        this.id = id;
        this.transformationId = transformationId;
        this.sourceFields = sourceFields != null ? sourceFields : new ArrayList<>();
        this.targetField = targetField;
        this.targetFields = targetFields != null ? targetFields : new ArrayList<>();
        this.mappingType = mappingType;
        this.splitConfiguration = splitConfiguration;
        this.javaFunction = javaFunction;
        this.mappingRule = mappingRule;
        this.sourceXPath = sourceXPath;
        this.targetXPath = targetXPath;
        this.isArrayMapping = isArrayMapping;
        this.arrayContextPath = arrayContextPath;
        this.namespaceAware = namespaceAware;
        this.inputTypes = inputTypes;
        this.outputType = outputType;
        this.description = description;
        this.version = version;
        this.functionName = functionName;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.name = name;
        this.sourcePaths = sourcePaths != null ? sourcePaths : new ArrayList<>();
        this.targetPath = targetPath;
        this.requiresTransformation = requiresTransformation;
        this.functionNode = functionNode;
        this.visualFlowData = visualFlowData;
        this.mappingOrder = mappingOrder;
    }

    // Getters
    public String getId() { return id; }
    public String getTransformationId() { return transformationId; }
    public List<String> getSourceFields() { return sourceFields; }
    public String getTargetField() { return targetField; }
    public List<String> getTargetFields() { return targetFields; }
    public String getMappingType() { return mappingType; }
    public Object getSplitConfiguration() { return splitConfiguration; }
    public String getJavaFunction() { return javaFunction; }
    public String getMappingRule() { return mappingRule; }
    public String getSourceXPath() { return sourceXPath; }
    public String getTargetXPath() { return targetXPath; }
    public boolean isArrayMapping() { return isArrayMapping; }
    public String getArrayContextPath() { return arrayContextPath; }
    public boolean isNamespaceAware() { return namespaceAware; }
    public String getInputTypes() { return inputTypes; }
    public String getOutputType() { return outputType; }
    public String getDescription() { return description; }
    public String getVersion() { return version; }
    public String getFunctionName() { return functionName; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getName() { return name; }
    public List<String> getSourcePaths() { return sourcePaths; }
    public String getTargetPath() { return targetPath; }
    public boolean isRequiresTransformation() { return requiresTransformation; }
    public Object getFunctionNode() { return functionNode; }
    public Object getVisualFlowData() { return visualFlowData; }
    public Integer getMappingOrder() { return mappingOrder; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTransformationId(String transformationId) { this.transformationId = transformationId; }
    public void setSourceFields(List<String> sourceFields) { this.sourceFields = sourceFields; }
    public void setTargetField(String targetField) { this.targetField = targetField; }
    public void setTargetFields(List<String> targetFields) { this.targetFields = targetFields; }
    public void setMappingType(String mappingType) { this.mappingType = mappingType; }
    public void setSplitConfiguration(Object splitConfiguration) { this.splitConfiguration = splitConfiguration; }
    public void setJavaFunction(String javaFunction) { this.javaFunction = javaFunction; }
    public void setMappingRule(String mappingRule) { this.mappingRule = mappingRule; }
    public void setSourceXPath(String sourceXPath) { this.sourceXPath = sourceXPath; }
    public void setTargetXPath(String targetXPath) { this.targetXPath = targetXPath; }
    public void setArrayMapping(boolean isArrayMapping) { this.isArrayMapping = isArrayMapping; }
    public void setArrayContextPath(String arrayContextPath) { this.arrayContextPath = arrayContextPath; }
    public void setNamespaceAware(boolean namespaceAware) { this.namespaceAware = namespaceAware; }
    public void setInputTypes(String inputTypes) { this.inputTypes = inputTypes; }
    public void setOutputType(String outputType) { this.outputType = outputType; }
    public void setDescription(String description) { this.description = description; }
    public void setVersion(String version) { this.version = version; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    public void setActive(boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setName(String name) { this.name = name; }
    public void setSourcePaths(List<String> sourcePaths) { this.sourcePaths = sourcePaths; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }
    public void setRequiresTransformation(boolean requiresTransformation) { this.requiresTransformation = requiresTransformation; }
    public void setFunctionNode(Object functionNode) { this.functionNode = functionNode; }
    public void setVisualFlowData(Object visualFlowData) { this.visualFlowData = visualFlowData; }
    public void setMappingOrder(Integer mappingOrder) { this.mappingOrder = mappingOrder; }

    // Builder
    public static FieldMappingDTOBuilder builder() {
        return new FieldMappingDTOBuilder();
    }

    public static class FieldMappingDTOBuilder {
        private String id;
        private String transformationId;
        private List<String> sourceFields = new ArrayList<>();
        private String targetField;
        private List<String> targetFields = new ArrayList<>();
        private String mappingType;
        private Object splitConfiguration;
        private String javaFunction;
        private String mappingRule;
        private String sourceXPath;
        private String targetXPath;
        private boolean isArrayMapping;
        private String arrayContextPath;
        private boolean namespaceAware;
        private String inputTypes;
        private String outputType;
        private String description;
        private String version;
        private String functionName;
        private boolean isActive;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String name;
        private List<String> sourcePaths = new ArrayList<>();
        private String targetPath;
        private boolean requiresTransformation;
        private Object functionNode;
        private Object visualFlowData;
        private Integer mappingOrder;

        public FieldMappingDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public FieldMappingDTOBuilder transformationId(String transformationId) {
            this.transformationId = transformationId;
            return this;
        }

        public FieldMappingDTOBuilder sourceFields(List<String> sourceFields) {
            this.sourceFields = sourceFields;
            return this;
        }

        public FieldMappingDTOBuilder targetField(String targetField) {
            this.targetField = targetField;
            return this;
        }

        public FieldMappingDTOBuilder targetFields(List<String> targetFields) {
            this.targetFields = targetFields;
            return this;
        }

        public FieldMappingDTOBuilder mappingType(String mappingType) {
            this.mappingType = mappingType;
            return this;
        }

        public FieldMappingDTOBuilder splitConfiguration(Object splitConfiguration) {
            this.splitConfiguration = splitConfiguration;
            return this;
        }

        public FieldMappingDTOBuilder javaFunction(String javaFunction) {
            this.javaFunction = javaFunction;
            return this;
        }

        public FieldMappingDTOBuilder mappingRule(String mappingRule) {
            this.mappingRule = mappingRule;
            return this;
        }

        public FieldMappingDTOBuilder sourceXPath(String sourceXPath) {
            this.sourceXPath = sourceXPath;
            return this;
        }

        public FieldMappingDTOBuilder targetXPath(String targetXPath) {
            this.targetXPath = targetXPath;
            return this;
        }

        public FieldMappingDTOBuilder isArrayMapping(boolean isArrayMapping) {
            this.isArrayMapping = isArrayMapping;
            return this;
        }

        public FieldMappingDTOBuilder arrayContextPath(String arrayContextPath) {
            this.arrayContextPath = arrayContextPath;
            return this;
        }

        public FieldMappingDTOBuilder namespaceAware(boolean namespaceAware) {
            this.namespaceAware = namespaceAware;
            return this;
        }

        public FieldMappingDTOBuilder inputTypes(String inputTypes) {
            this.inputTypes = inputTypes;
            return this;
        }

        public FieldMappingDTOBuilder outputType(String outputType) {
            this.outputType = outputType;
            return this;
        }

        public FieldMappingDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FieldMappingDTOBuilder version(String version) {
            this.version = version;
            return this;
        }

        public FieldMappingDTOBuilder functionName(String functionName) {
            this.functionName = functionName;
            return this;
        }

        public FieldMappingDTOBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FieldMappingDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FieldMappingDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FieldMappingDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FieldMappingDTOBuilder sourcePaths(List<String> sourcePaths) {
            this.sourcePaths = sourcePaths;
            return this;
        }

        public FieldMappingDTOBuilder targetPath(String targetPath) {
            this.targetPath = targetPath;
            return this;
        }

        public FieldMappingDTOBuilder requiresTransformation(boolean requiresTransformation) {
            this.requiresTransformation = requiresTransformation;
            return this;
        }

        public FieldMappingDTOBuilder functionNode(Object functionNode) {
            this.functionNode = functionNode;
            return this;
        }

        public FieldMappingDTOBuilder visualFlowData(Object visualFlowData) {
            this.visualFlowData = visualFlowData;
            return this;
        }

        public FieldMappingDTOBuilder mappingOrder(Integer mappingOrder) {
            this.mappingOrder = mappingOrder;
            return this;
        }

        public FieldMappingDTO build() {
            return new FieldMappingDTO(id, transformationId, sourceFields, targetField, targetFields, mappingType, splitConfiguration, javaFunction, mappingRule, sourceXPath, targetXPath, isArrayMapping, arrayContextPath, namespaceAware, inputTypes, outputType, description, version, functionName, isActive, createdAt, updatedAt, name, sourcePaths, targetPath, requiresTransformation, functionNode, visualFlowData, mappingOrder);
        }
    }
}
