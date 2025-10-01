package com.integrixs.data.model;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing XML field mappings using XPath expressions.
 *
 * <p>This replaces the JSON - based field mappings with proper XML/XPath support.
 *
 * @author Integration Team
 * @since 2.0.0
 */
public class XmlFieldMapping {

    /**
     * Unique identifier(UUID) for the entity
     */
    private UUID id;

    /**
     * The transformation this mapping belongs to
     */
    @NotNull(message = "Transformation is required")
    private FlowTransformation transformation;

    /**
     * Source XPath expression
     */
    @NotBlank(message = "Source XPath is required")
    @Size(max = 2000, message = "Source XPath cannot exceed 2000 characters")
    private String sourceXPath;

    /**
     * Target XPath expression
     */
    @NotBlank(message = "Target XPath is required")
    @Size(max = 2000, message = "Target XPath cannot exceed 2000 characters")
    private String targetXPath;

    /**
     * Type of mapping
     */
    @NotNull(message = "Mapping type is required")
    private MappingType mappingType = MappingType.TEXT;

    /**
     * Whether this mapping handles repeating elements
     */
    private boolean isRepeating = false;

    /**
     * XPath context for repeating elements
     */
    @Size(max = 1000, message = "Repeat context XPath cannot exceed 1000 characters")
    private String repeatContextXPath;

    /**
     * Optional transformation function(JavaScript or Java)
     */
    private String transformFunction;

    /**
     * Order of this mapping within the transformation
     */
    private Integer mappingOrder = 0;

    /**
     * Description of the mapping
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Timestamp of entity creation
     */
        private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
        private LocalDateTime updatedAt;

    /**
     * User who created this field mapping
     */
    private User createdBy;

    /**
     * User who last updated this field mapping
     */
    private User updatedBy;

    /**
     * Enum defining the type of XML mapping
     */
    public enum MappingType {
        /**
         * Map entire element including child elements
         */
        ELEMENT,

        /**
         * Map attribute value only
         */
        ATTRIBUTE,

        /**
         * Map text content only(default)
         */
        TEXT,

        /**
         * Define structure without value mapping
         */
        STRUCTURE
    }

    // Default constructor
    public XmlFieldMapping() {
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

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public boolean isIsRepeating() {
        return isRepeating;
    }

    public void setIsRepeating(boolean isRepeating) {
        this.isRepeating = isRepeating;
    }

    public String getRepeatContextXPath() {
        return repeatContextXPath;
    }

    public void setRepeatContextXPath(String repeatContextXPath) {
        this.repeatContextXPath = repeatContextXPath;
    }

    public String getTransformFunction() {
        return transformFunction;
    }

    public void setTransformFunction(String transformFunction) {
        this.transformFunction = transformFunction;
    }

    public Integer getMappingOrder() {
        return mappingOrder;
    }

    public void setMappingOrder(Integer mappingOrder) {
        this.mappingOrder = mappingOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    // Builder
    public static XmlFieldMappingBuilder builder() {
        return new XmlFieldMappingBuilder();
    }

    public static class XmlFieldMappingBuilder {
        private UUID id;
        private FlowTransformation transformation;
        private String sourceXPath;
        private String targetXPath;
        private MappingType mappingType;
        private boolean isRepeating;
        private String repeatContextXPath;
        private String transformFunction;
        private Integer mappingOrder;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private User createdBy;
        private User updatedBy;

        public XmlFieldMappingBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public XmlFieldMappingBuilder transformation(FlowTransformation transformation) {
            this.transformation = transformation;
            return this;
        }

        public XmlFieldMappingBuilder sourceXPath(String sourceXPath) {
            this.sourceXPath = sourceXPath;
            return this;
        }

        public XmlFieldMappingBuilder targetXPath(String targetXPath) {
            this.targetXPath = targetXPath;
            return this;
        }

        public XmlFieldMappingBuilder mappingType(MappingType mappingType) {
            this.mappingType = mappingType;
            return this;
        }

        public XmlFieldMappingBuilder isRepeating(boolean isRepeating) {
            this.isRepeating = isRepeating;
            return this;
        }

        public XmlFieldMappingBuilder repeatContextXPath(String repeatContextXPath) {
            this.repeatContextXPath = repeatContextXPath;
            return this;
        }

        public XmlFieldMappingBuilder transformFunction(String transformFunction) {
            this.transformFunction = transformFunction;
            return this;
        }

        public XmlFieldMappingBuilder mappingOrder(Integer mappingOrder) {
            this.mappingOrder = mappingOrder;
            return this;
        }

        public XmlFieldMappingBuilder description(String description) {
            this.description = description;
            return this;
        }

        public XmlFieldMappingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public XmlFieldMappingBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public XmlFieldMappingBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public XmlFieldMappingBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public XmlFieldMapping build() {
            XmlFieldMapping instance = new XmlFieldMapping();
            instance.setId(this.id);
            instance.setTransformation(this.transformation);
            instance.setSourceXPath(this.sourceXPath);
            instance.setTargetXPath(this.targetXPath);
            instance.setMappingType(this.mappingType);
            instance.setIsRepeating(this.isRepeating);
            instance.setRepeatContextXPath(this.repeatContextXPath);
            instance.setTransformFunction(this.transformFunction);
            instance.setMappingOrder(this.mappingOrder);
            instance.setDescription(this.description);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setCreatedBy(this.createdBy);
            instance.setUpdatedBy(this.updatedBy);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "XmlFieldMapping{" +
                "id=" + id + "sourceXPath=" + sourceXPath + "targetXPath=" + targetXPath + "mappingType=" + mappingType + "isRepeating=" + isRepeating + "..." +
                '}';
    }
}
