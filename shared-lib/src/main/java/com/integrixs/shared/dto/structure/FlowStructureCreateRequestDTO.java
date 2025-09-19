package com.integrixs.shared.dto.structure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
public class FlowStructureCreateRequestDTO {

    private String name;
    private String description;
    private FlowStructureDTO.ProcessingMode processingMode;
    private FlowStructureDTO.Direction direction;
    private Map<String, Object> namespace;
    private Map<String, Object> metadata;
    private Set<String> tags;
    private String businessComponentId;
    private Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds;
    private String wsdlContent;
    private String sourceType;

    // Default constructor
    public FlowStructureCreateRequestDTO() {
        this.namespace = new HashMap<>();
        this.metadata = new HashMap<>();
        this.tags = new HashSet<>();
        this.messageStructureIds = new HashMap<>();
    }

    // All args constructor
    public FlowStructureCreateRequestDTO(String name, String description, FlowStructureDTO.ProcessingMode processingMode, FlowStructureDTO.Direction direction, Map<String, Object> namespace, Map<String, Object> metadata, Set<String> tags, String businessComponentId, Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds, String wsdlContent, String sourceType) {
        this.name = name;
        this.description = description;
        this.processingMode = processingMode;
        this.direction = direction;
        this.namespace = namespace != null ? namespace : new HashMap<>();
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.tags = tags != null ? tags : new HashSet<>();
        this.businessComponentId = businessComponentId;
        this.messageStructureIds = messageStructureIds != null ? messageStructureIds : new HashMap<>();
        this.wsdlContent = wsdlContent;
        this.sourceType = sourceType;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public FlowStructureDTO.ProcessingMode getProcessingMode() { return processingMode; }
    public FlowStructureDTO.Direction getDirection() { return direction; }
    public Map<String, Object> getNamespace() { return namespace; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Set<String> getTags() { return tags; }
    public String getBusinessComponentId() { return businessComponentId; }
    public Map<FlowStructureMessageDTO.MessageType, String> getMessageStructureIds() { return messageStructureIds; }
    public String getWsdlContent() { return wsdlContent; }
    public String getSourceType() { return sourceType; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setProcessingMode(FlowStructureDTO.ProcessingMode processingMode) { this.processingMode = processingMode; }
    public void setDirection(FlowStructureDTO.Direction direction) { this.direction = direction; }
    public void setNamespace(Map<String, Object> namespace) { this.namespace = namespace; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    public void setMessageStructureIds(Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds) { this.messageStructureIds = messageStructureIds; }
    public void setWsdlContent(String wsdlContent) { this.wsdlContent = wsdlContent; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    // Builder
    public static FlowStructureCreateRequestDTOBuilder builder() {
        return new FlowStructureCreateRequestDTOBuilder();
    }

    public static class FlowStructureCreateRequestDTOBuilder {
        private String name;
        private String description;
        private FlowStructureDTO.ProcessingMode processingMode;
        private FlowStructureDTO.Direction direction;
        private Map<String, Object> namespace = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        private Set<String> tags = new HashSet<>();
        private String businessComponentId;
        private Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds = new HashMap<>();
        private String wsdlContent;
        private String sourceType;

        public FlowStructureCreateRequestDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder processingMode(FlowStructureDTO.ProcessingMode processingMode) {
            this.processingMode = processingMode;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder direction(FlowStructureDTO.Direction direction) {
            this.direction = direction;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder namespace(Map<String, Object> namespace) {
            this.namespace = namespace;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder messageStructureIds(Map<FlowStructureMessageDTO.MessageType, String> messageStructureIds) {
            this.messageStructureIds = messageStructureIds;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder wsdlContent(String wsdlContent) {
            this.wsdlContent = wsdlContent;
            return this;
        }

        public FlowStructureCreateRequestDTOBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public FlowStructureCreateRequestDTO build() {
            return new FlowStructureCreateRequestDTO(name, description, processingMode, direction, namespace, metadata, tags, businessComponentId, messageStructureIds, wsdlContent, sourceType);
        }
    }
}
