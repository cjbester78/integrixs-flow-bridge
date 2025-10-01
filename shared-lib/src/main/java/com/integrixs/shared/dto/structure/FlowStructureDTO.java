package com.integrixs.shared.dto.structure;

import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.user.UserDTO;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
public class FlowStructureDTO {

    private String id;
    private String name;
    private String description;
    private ProcessingMode processingMode;
    private Direction direction;
    private String wsdlContent;
    private String sourceType;
    private Map<String, Object> namespace;
    private Map<String, Object> metadata;
    private Set<String> tags;
    private Integer version;
    private Boolean isActive;
    private BusinessComponentDTO businessComponent;
    private Set<FlowStructureMessageDTO> flowStructureMessages;
    private UserDTO createdBy;
    private UserDTO updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public FlowStructureDTO() {
        this.namespace = new HashMap<>();
        this.metadata = new HashMap<>();
        this.tags = new HashSet<>();
        this.flowStructureMessages = new HashSet<>();
    }

    // All args constructor
    public FlowStructureDTO(String id, String name, String description, ProcessingMode processingMode, Direction direction, String wsdlContent, String sourceType, Map<String, Object> namespace, Map<String, Object> metadata, Set<String> tags, Integer version, Boolean isActive, BusinessComponentDTO businessComponent, Set<FlowStructureMessageDTO> flowStructureMessages, UserDTO createdBy, UserDTO updatedBy, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.processingMode = processingMode;
        this.direction = direction;
        this.wsdlContent = wsdlContent;
        this.sourceType = sourceType;
        this.namespace = namespace != null ? namespace : new HashMap<>();
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.tags = tags != null ? tags : new HashSet<>();
        this.version = version;
        this.isActive = isActive;
        this.businessComponent = businessComponent;
        this.flowStructureMessages = flowStructureMessages != null ? flowStructureMessages : new HashSet<>();
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public ProcessingMode getProcessingMode() { return processingMode; }
    public Direction getDirection() { return direction; }
    public String getWsdlContent() { return wsdlContent; }
    public String getSourceType() { return sourceType; }
    public Map<String, Object> getNamespace() { return namespace; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Set<String> getTags() { return tags; }
    public Integer getVersion() { return version; }
    public Boolean isIsActive() { return isActive; }
    public BusinessComponentDTO getBusinessComponent() { return businessComponent; }
    public Set<FlowStructureMessageDTO> getFlowStructureMessages() { return flowStructureMessages; }
    public UserDTO getCreatedBy() { return createdBy; }
    public UserDTO getUpdatedBy() { return updatedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setProcessingMode(ProcessingMode processingMode) { this.processingMode = processingMode; }
    public void setDirection(Direction direction) { this.direction = direction; }
    public void setWsdlContent(String wsdlContent) { this.wsdlContent = wsdlContent; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public void setNamespace(Map<String, Object> namespace) { this.namespace = namespace; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public void setVersion(Integer version) { this.version = version; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setBusinessComponent(BusinessComponentDTO businessComponent) { this.businessComponent = businessComponent; }
    public void setFlowStructureMessages(Set<FlowStructureMessageDTO> flowStructureMessages) { this.flowStructureMessages = flowStructureMessages; }
    public void setCreatedBy(UserDTO createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(UserDTO updatedBy) { this.updatedBy = updatedBy; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static FlowStructureDTOBuilder builder() {
        return new FlowStructureDTOBuilder();
    }

    public static class FlowStructureDTOBuilder {
        private String id;
        private String name;
        private String description;
        private ProcessingMode processingMode;
        private Direction direction;
        private String wsdlContent;
        private String sourceType;
        private Map<String, Object> namespace = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        private Set<String> tags = new HashSet<>();
        private Integer version;
        private Boolean isActive;
        private BusinessComponentDTO businessComponent;
        private Set<FlowStructureMessageDTO> flowStructureMessages = new HashSet<>();
        private UserDTO createdBy;
        private UserDTO updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public FlowStructureDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public FlowStructureDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FlowStructureDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowStructureDTOBuilder processingMode(ProcessingMode processingMode) {
            this.processingMode = processingMode;
            return this;
        }

        public FlowStructureDTOBuilder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public FlowStructureDTOBuilder wsdlContent(String wsdlContent) {
            this.wsdlContent = wsdlContent;
            return this;
        }

        public FlowStructureDTOBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public FlowStructureDTOBuilder namespace(Map<String, Object> namespace) {
            this.namespace = namespace;
            return this;
        }

        public FlowStructureDTOBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public FlowStructureDTOBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public FlowStructureDTOBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public FlowStructureDTOBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FlowStructureDTOBuilder businessComponent(BusinessComponentDTO businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public FlowStructureDTOBuilder flowStructureMessages(Set<FlowStructureMessageDTO> flowStructureMessages) {
            this.flowStructureMessages = flowStructureMessages;
            return this;
        }

        public FlowStructureDTOBuilder createdBy(UserDTO createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public FlowStructureDTOBuilder updatedBy(UserDTO updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public FlowStructureDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FlowStructureDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FlowStructureDTO build() {
            return new FlowStructureDTO(id, name, description, processingMode, direction, wsdlContent, sourceType, namespace, metadata, tags, version, isActive, businessComponent, flowStructureMessages, createdBy, updatedBy, createdAt, updatedAt);
        }
    }

    /**
     * Processing mode for the flow structure
     */
    public enum ProcessingMode {
        SYNCHRONOUS,
        ASYNCHRONOUS
    }

    /**
     * Direction of the flow
     */
    public enum Direction {
        INBOUND,
        OUTBOUND,
        BIDIRECTIONAL
    }
}
