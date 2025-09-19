package com.integrixs.data.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a field mapping within a transformation.
 *
 * <p>Field mappings define how source fields are transformed to target fields.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "field_mappings", indexes = {
    @Index(name = "idx_mapping_transformation", columnList = "transformation_id"),
    @Index(name = "idx_mapping_target_field", columnList = "target_field"),
    @Index(name = "idx_mapping_active", columnList = "is_active")
})
public class FieldMapping {

    /**
     * Unique identifier(UUID) for the entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    /**
     * The transformation this mapping belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transformation_id", nullable = false)
    @NotNull(message = "Transformation is required")
    private FlowTransformation transformation;

    /**
     * Source fields in JSON format
     */
    @Column(name = "source_fields", columnDefinition = "json", nullable = false)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @NotBlank(message = "Source fields are required")
    @Size(max = 5000, message = "Source fields cannot exceed 5000 characters")
    private String sourceFields;

    /**
     * Target field name(deprecated - use targetFields for 1 - to - many support)
     */
    @Column(name = "target_field", length = 500)
    @Size(max = 500, message = "Target field cannot exceed 500 characters")
    private String targetField;

    /**
     * Target fields in JSON format(supports 1 - to - many mappings)
     */
    @Column(name = "target_fields", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Size(max = 5000, message = "Target fields cannot exceed 5000 characters")
    private String targetFields;

    /**
     * Mapping type indicating how source maps to target
     */
    @Column(name = "mapping_type", length = 50)
    @Enumerated(EnumType.STRING)
    private MappingType mappingType = MappingType.DIRECT;

    /**
     * For SPLIT type: defines how to split source into multiple targets
     */
    @Column(name = "split_configuration", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String splitConfiguration;

    /**
     * JavaScript/Java function for transformation
     */
    @Column(name = "java_function", columnDefinition = "TEXT")
    @Size(max = 10000, message = "Function cannot exceed 10000 characters")
    private String javaFunction;

    /**
     * Mapping rule definition
     */
    @Column(name = "mapping_rule", columnDefinition = "TEXT")
    @Size(max = 5000, message = "Mapping rule cannot exceed 5000 characters")
    private String mappingRule;

    /**
     * Source XPath for XML mappings
     */
    @Column(name = "source_xpath", length = 1000)
    @Size(max = 1000, message = "Source XPath cannot exceed 1000 characters")
    private String sourceXPath;

    /**
     * Target XPath for XML mappings
     */
    @Column(name = "target_xpath", length = 1000)
    @Size(max = 1000, message = "Target XPath cannot exceed 1000 characters")
    private String targetXPath;

    /**
     * Whether this is an array mapping
     */
    @Column(name = "is_array_mapping", nullable = false)
    @NotNull(message = "Array mapping flag is required")
    private boolean isArrayMapping = false;

    /**
     * Array context path for nested arrays
     */
    @Column(name = "array_context_path", length = 500)
    @Size(max = 500, message = "Array context path cannot exceed 500 characters")
    private String arrayContextPath;

    /**
     * Whether XML namespaces should be considered
     */
    @Column(name = "namespace_aware", nullable = false)
    @NotNull(message = "Namespace aware flag is required")
    private boolean namespaceAware = false;

    /**
     * Input data types in JSON format
     */
    @Column(name = "input_types", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Size(max = 1000, message = "Input types cannot exceed 1000 characters")
    private String inputTypes;

    /**
     * Output data type
     */
    @Column(name = "output_type", length = 100)
    @Size(max = 100, message = "Output type cannot exceed 100 characters")
    private String outputType;

    /**
     * Detailed description of the mapping
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Mapping version
     */
    @Column(length = 50)
    @Size(max = 50, message = "Version cannot exceed 50 characters")
    private String version;

    /**
     * Function name for reference
     */
    @Column(name = "function_name", length = 255)
    @Size(max = 255, message = "Function name cannot exceed 255 characters")
    private String functionName;

    /**
     * Whether this mapping is active
     */
    @Column(name = "is_active", nullable = false)
    @NotNull(message = "Active status is required")
    private boolean isActive = true;

    /**
     * Visual flow data(nodes and edges) in JSON format
     */
    @Column(name = "visual_flow_data", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String visualFlowData;

    /**
     * Function node configuration in JSON format
     */
    @Column(name = "function_node", columnDefinition = "json")
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String functionNode;

    /**
     * Order of this mapping within the transformation
     */
    @Column(name = "mapping_order")
    private Integer mappingOrder = 0;

    /**
     * Timestamp of entity creation
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * User who created this field mapping
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    /**
     * User who last updated this field mapping
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * Transient field for parsed source fields list
     */
    @Transient
    private List<String> parsedSourceFields;

    /**
     * Transient field for parsed target fields list
     */
    @Transient
    private List<String> parsedTargetFields;

    /**
     * Gets the source fields as a list
     *
     * @return list of source field names
     */
    public List<String> getSourceFieldsList() {
        if(parsedSourceFields == null && sourceFields != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                parsedSourceFields = mapper.readValue(sourceFields, new TypeReference<List<String>>() {});
            } catch(Exception e) {
                parsedSourceFields = Collections.emptyList();
            }
        }
        return parsedSourceFields != null ? parsedSourceFields : Collections.emptyList();
    }

    /**
     * Sets the source fields from a list
     *
     * @param fields list of field names
     */
    public void setSourceFieldsList(List<String> fields) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.sourceFields = mapper.writeValueAsString(fields);
            this.parsedSourceFields = fields;
        } catch(Exception e) {
            this.sourceFields = "[]";
        }
    }

    /**
     * Gets the target fields as a list
     *
     * @return list of target field names
     */
    public List<String> getTargetFieldsList() {
        // If targetFields is set, use it(new multi - target support)
        if(targetFields != null && !targetFields.isEmpty()) {
            if(parsedTargetFields == null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    parsedTargetFields = mapper.readValue(targetFields, new TypeReference<List<String>>() {});
                } catch(Exception e) {
                    parsedTargetFields = Collections.emptyList();
                }
            }
            return parsedTargetFields != null ? parsedTargetFields : Collections.emptyList();
        }
        // Fall back to single targetField for backward compatibility
        else if(targetField != null && !targetField.isEmpty()) {
            return Collections.singletonList(targetField);
        }
        return Collections.emptyList();
    }

    /**
     * Sets the target fields from a list
     *
     * @param fields list of target field names
     */
    public void setTargetFieldsList(List<String> fields) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.targetFields = mapper.writeValueAsString(fields);
            this.parsedTargetFields = fields;
            // Clear single targetField when using multiple
            if(fields != null && fields.size() > 1) {
                this.targetField = null;
            } else if(fields != null && fields.size() == 1) {
                // Set single field for backward compatibility
                this.targetField = fields.get(0);
            }
        } catch(Exception e) {
            this.targetFields = "[]";
        }
    }

    /**
     * Checks if this is a 1 - to - many mapping
     */
    public boolean isOneToManyMapping() {
        return getTargetFieldsList().size() > 1;
    }

    /**
     * Mapping type enum
     */
    public enum MappingType {
        DIRECT,      // Direct field to field(s) mapping
        SPLIT,       // Split single value to multiple fields
        AGGREGATE,   // Aggregate multiple sources to multiple targets
        CONDITIONAL, // Conditional mapping based on rules
        ITERATE       // Iterate over arrays/collections
    }

    /**
     * Convenience method to parse inputTypes JSON into List<String>
     *
     * @return list of input types
     */
    @Transient
    public List<String> getParsedInputTypes() {
        if(inputTypes == null || inputTypes.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return new ObjectMapper().readValue(inputTypes, new TypeReference<List<String>>() {});
        } catch(Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Convenience setter to set inputTypes from List<String>
     *
     * @param types list of input types
     */
    @Transient
    public void setParsedInputTypes(List<String> types) {
        try {
            this.inputTypes = new ObjectMapper().writeValueAsString(types);
        } catch(Exception e) {
            this.inputTypes = "[]";
        }
    }

    // Default constructor
    public FieldMapping() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FlowTransformation getTransformation() {
        return transformation;
    }

    public void setTransformation(FlowTransformation transformation) {
        this.transformation = transformation;
    }

    public String getSourceFields() {
        return sourceFields;
    }

    public void setSourceFields(String sourceFields) {
        this.sourceFields = sourceFields;
    }

    public String getTargetField() {
        return targetField;
    }

    public void setTargetField(String targetField) {
        this.targetField = targetField;
    }

    public String getTargetFields() {
        return targetFields;
    }

    public void setTargetFields(String targetFields) {
        this.targetFields = targetFields;
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public String getSplitConfiguration() {
        return splitConfiguration;
    }

    public void setSplitConfiguration(String splitConfiguration) {
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

    public boolean isArrayMapping() {
        return isArrayMapping;
    }

    public void setArrayMapping(boolean isArrayMapping) {
        this.isArrayMapping = isArrayMapping;
    }

    public String getArrayContextPath() {
        return arrayContextPath;
    }

    public void setArrayContextPath(String arrayContextPath) {
        this.arrayContextPath = arrayContextPath;
    }

    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
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

    public boolean isIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public String getVisualFlowData() {
        return visualFlowData;
    }

    public void setVisualFlowData(String visualFlowData) {
        this.visualFlowData = visualFlowData;
    }

    public String getFunctionNode() {
        return functionNode;
    }

    public void setFunctionNode(String functionNode) {
        this.functionNode = functionNode;
    }

    public Integer getMappingOrder() {
        return mappingOrder;
    }

    public void setMappingOrder(Integer mappingOrder) {
        this.mappingOrder = mappingOrder;
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

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public List<String> getParsedSourceFields() {
        return parsedSourceFields;
    }

    public void setParsedSourceFields(List<String> parsedSourceFields) {
        this.parsedSourceFields = parsedSourceFields;
    }

    public List<String> getParsedTargetFields() {
        return parsedTargetFields;
    }

    public void setParsedTargetFields(List<String> parsedTargetFields) {
        this.parsedTargetFields = parsedTargetFields;
    }

    // Builder
    public static FieldMappingBuilder builder() {
        return new FieldMappingBuilder();
    }

    public static class FieldMappingBuilder {
        private UUID id;
        private FlowTransformation transformation;
        private String sourceFields;
        private String targetField;
        private String targetFields;
        private MappingType mappingType;
        private String splitConfiguration;
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
        private String visualFlowData;
        private String functionNode;
        private Integer mappingOrder;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private User createdBy;
        private User updatedBy;
        private List<String> parsedSourceFields;
        private List<String> parsedTargetFields;

        public FieldMappingBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FieldMappingBuilder transformation(FlowTransformation transformation) {
            this.transformation = transformation;
            return this;
        }

        public FieldMappingBuilder sourceFields(String sourceFields) {
            this.sourceFields = sourceFields;
            return this;
        }

        public FieldMappingBuilder targetField(String targetField) {
            this.targetField = targetField;
            return this;
        }

        public FieldMappingBuilder targetFields(String targetFields) {
            this.targetFields = targetFields;
            return this;
        }

        public FieldMappingBuilder mappingType(MappingType mappingType) {
            this.mappingType = mappingType;
            return this;
        }

        public FieldMappingBuilder splitConfiguration(String splitConfiguration) {
            this.splitConfiguration = splitConfiguration;
            return this;
        }

        public FieldMappingBuilder javaFunction(String javaFunction) {
            this.javaFunction = javaFunction;
            return this;
        }

        public FieldMappingBuilder mappingRule(String mappingRule) {
            this.mappingRule = mappingRule;
            return this;
        }

        public FieldMappingBuilder sourceXPath(String sourceXPath) {
            this.sourceXPath = sourceXPath;
            return this;
        }

        public FieldMappingBuilder targetXPath(String targetXPath) {
            this.targetXPath = targetXPath;
            return this;
        }

        public FieldMappingBuilder isArrayMapping(boolean isArrayMapping) {
            this.isArrayMapping = isArrayMapping;
            return this;
        }

        public FieldMappingBuilder arrayContextPath(String arrayContextPath) {
            this.arrayContextPath = arrayContextPath;
            return this;
        }

        public FieldMappingBuilder namespaceAware(boolean namespaceAware) {
            this.namespaceAware = namespaceAware;
            return this;
        }

        public FieldMappingBuilder inputTypes(String inputTypes) {
            this.inputTypes = inputTypes;
            return this;
        }

        public FieldMappingBuilder outputType(String outputType) {
            this.outputType = outputType;
            return this;
        }

        public FieldMappingBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FieldMappingBuilder version(String version) {
            this.version = version;
            return this;
        }

        public FieldMappingBuilder functionName(String functionName) {
            this.functionName = functionName;
            return this;
        }

        public FieldMappingBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FieldMappingBuilder visualFlowData(String visualFlowData) {
            this.visualFlowData = visualFlowData;
            return this;
        }

        public FieldMappingBuilder functionNode(String functionNode) {
            this.functionNode = functionNode;
            return this;
        }

        public FieldMappingBuilder mappingOrder(Integer mappingOrder) {
            this.mappingOrder = mappingOrder;
            return this;
        }

        public FieldMappingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FieldMappingBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FieldMappingBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public FieldMappingBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public FieldMappingBuilder parsedSourceFields(List<String> parsedSourceFields) {
            this.parsedSourceFields = parsedSourceFields;
            return this;
        }

        public FieldMappingBuilder parsedTargetFields(List<String> parsedTargetFields) {
            this.parsedTargetFields = parsedTargetFields;
            return this;
        }

        public FieldMapping build() {
            FieldMapping instance = new FieldMapping();
            instance.setId(this.id);
            instance.setTransformation(this.transformation);
            instance.setSourceFields(this.sourceFields);
            instance.setTargetField(this.targetField);
            instance.setTargetFields(this.targetFields);
            instance.setMappingType(this.mappingType);
            instance.setSplitConfiguration(this.splitConfiguration);
            instance.setJavaFunction(this.javaFunction);
            instance.setMappingRule(this.mappingRule);
            instance.setSourceXPath(this.sourceXPath);
            instance.setTargetXPath(this.targetXPath);
            instance.setArrayMapping(this.isArrayMapping);
            instance.setArrayContextPath(this.arrayContextPath);
            instance.setNamespaceAware(this.namespaceAware);
            instance.setInputTypes(this.inputTypes);
            instance.setOutputType(this.outputType);
            instance.setDescription(this.description);
            instance.setVersion(this.version);
            instance.setFunctionName(this.functionName);
            instance.setIsActive(this.isActive);
            instance.setVisualFlowData(this.visualFlowData);
            instance.setFunctionNode(this.functionNode);
            instance.setMappingOrder(this.mappingOrder);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setCreatedBy(this.createdBy);
            instance.setUpdatedBy(this.updatedBy);
            instance.setParsedSourceFields(this.parsedSourceFields);
            instance.setParsedTargetFields(this.parsedTargetFields);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "FieldMapping{" + 
                "id=" + id + "sourceFields=" + sourceFields + "targetField=" + targetField + "targetFields=" + targetFields + "mappingType=" + mappingType + "..." + 
                '}';
    }
}
