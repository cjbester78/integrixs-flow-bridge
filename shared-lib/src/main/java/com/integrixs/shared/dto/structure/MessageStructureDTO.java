package com.integrixs.shared.dto.structure;

import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
public class MessageStructureDTO {

    private String id;
    private String name;
    private String description;
    private String xsdContent;
    private Map<String, Object> namespace;
    private Map<String, Object> metadata;
    private Set<String> tags;
    private Integer version;
    private String sourceType;
    private Boolean isEditable;
    private Boolean isActive;
    private Map<String, Object> importMetadata;
    private BusinessComponentDTO businessComponent;
    private UserDTO createdBy;
    private UserDTO updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public MessageStructureDTO() {
        this.namespace = new HashMap<>();
        this.metadata = new HashMap<>();
        this.tags = new HashSet<>();
        this.importMetadata = new HashMap<>();
    }

    // All args constructor
    public MessageStructureDTO(String id, String name, String description, String xsdContent, Map<String, Object> namespace, Map<String, Object> metadata, Set<String> tags, Integer version, String sourceType, Boolean isEditable, Boolean isActive, Map<String, Object> importMetadata, BusinessComponentDTO businessComponent, UserDTO createdBy, UserDTO updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.xsdContent = xsdContent;
        this.namespace = namespace != null ? namespace : new HashMap<>();
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.tags = tags != null ? tags : new HashSet<>();
        this.version = version;
        this.sourceType = sourceType;
        this.isEditable = isEditable;
        this.isActive = isActive;
        this.importMetadata = importMetadata != null ? importMetadata : new HashMap<>();
        this.businessComponent = businessComponent;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getXsdContent() { return xsdContent; }
    public Map<String, Object> getNamespace() { return namespace; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Set<String> getTags() { return tags; }
    public Integer getVersion() { return version; }
    public String getSourceType() { return sourceType; }
    public Boolean isIsEditable() { return isEditable; }
    public Boolean isIsActive() { return isActive; }
    public Map<String, Object> getImportMetadata() { return importMetadata; }
    public BusinessComponentDTO getBusinessComponent() { return businessComponent; }
    public UserDTO getCreatedBy() { return createdBy; }
    public UserDTO getUpdatedBy() { return updatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setXsdContent(String xsdContent) { this.xsdContent = xsdContent; }
    public void setNamespace(Map<String, Object> namespace) { this.namespace = namespace; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public void setVersion(Integer version) { this.version = version; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public void setIsEditable(Boolean isEditable) { this.isEditable = isEditable; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setImportMetadata(Map<String, Object> importMetadata) { this.importMetadata = importMetadata; }
    public void setBusinessComponent(BusinessComponentDTO businessComponent) { this.businessComponent = businessComponent; }
    public void setCreatedBy(UserDTO createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(UserDTO updatedBy) { this.updatedBy = updatedBy; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static MessageStructureDTOBuilder builder() {
        return new MessageStructureDTOBuilder();
    }

    public static class MessageStructureDTOBuilder {
        private String id;
        private String name;
        private String description;
        private String xsdContent;
        private Map<String, Object> namespace = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        private Set<String> tags = new HashSet<>();
        private Integer version;
        private String sourceType;
        private Boolean isEditable;
        private Boolean isActive;
        private Map<String, Object> importMetadata = new HashMap<>();
        private BusinessComponentDTO businessComponent;
        private UserDTO createdBy;
        private UserDTO updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public MessageStructureDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public MessageStructureDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MessageStructureDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public MessageStructureDTOBuilder xsdContent(String xsdContent) {
            this.xsdContent = xsdContent;
            return this;
        }

        public MessageStructureDTOBuilder namespace(Map<String, Object> namespace) {
            this.namespace = namespace;
            return this;
        }

        public MessageStructureDTOBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public MessageStructureDTOBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public MessageStructureDTOBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public MessageStructureDTOBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public MessageStructureDTOBuilder isEditable(Boolean isEditable) {
            this.isEditable = isEditable;
            return this;
        }

        public MessageStructureDTOBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public MessageStructureDTOBuilder importMetadata(Map<String, Object> importMetadata) {
            this.importMetadata = importMetadata;
            return this;
        }

        public MessageStructureDTOBuilder businessComponent(BusinessComponentDTO businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public MessageStructureDTOBuilder createdBy(UserDTO createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public MessageStructureDTOBuilder updatedBy(UserDTO updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public MessageStructureDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MessageStructureDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public MessageStructureDTO build() {
            return new MessageStructureDTO(id, name, description, xsdContent, namespace, metadata, tags, version, sourceType, isEditable, isActive, importMetadata, businessComponent, createdBy, updatedBy, createdAt, updatedAt);
        }
    }
}
