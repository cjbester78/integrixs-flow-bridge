package com.integrixs.shared.dto.structure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
public class MessageStructureCreateRequestDTO {

    private String name;
    private String description;
    private String xsdContent;
    private Map<String, Object> namespace;
    private Map<String, Object> metadata;
    private Set<String> tags;
    private String businessComponentId;

    // Default constructor
    public MessageStructureCreateRequestDTO() {
        this.namespace = new HashMap<>();
        this.metadata = new HashMap<>();
        this.tags = new HashSet<>();
    }

    // All args constructor
    public MessageStructureCreateRequestDTO(String name, String description, String xsdContent, Map<String, Object> namespace, Map<String, Object> metadata, Set<String> tags, String businessComponentId) {
        this.name = name;
        this.description = description;
        this.xsdContent = xsdContent;
        this.namespace = namespace != null ? namespace : new HashMap<>();
        this.metadata = metadata != null ? metadata : new HashMap<>();
        this.tags = tags != null ? tags : new HashSet<>();
        this.businessComponentId = businessComponentId;
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getXsdContent() { return xsdContent; }
    public Map<String, Object> getNamespace() { return namespace; }
    public Map<String, Object> getMetadata() { return metadata; }
    public Set<String> getTags() { return tags; }
    public String getBusinessComponentId() { return businessComponentId; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setXsdContent(String xsdContent) { this.xsdContent = xsdContent; }
    public void setNamespace(Map<String, Object> namespace) { this.namespace = namespace; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public void setTags(Set<String> tags) { this.tags = tags; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }

    // Builder
    public static MessageStructureCreateRequestDTOBuilder builder() {
        return new MessageStructureCreateRequestDTOBuilder();
    }

    public static class MessageStructureCreateRequestDTOBuilder {
        private String name;
        private String description;
        private String xsdContent;
        private Map<String, Object> namespace = new HashMap<>();
        private Map<String, Object> metadata = new HashMap<>();
        private Set<String> tags = new HashSet<>();
        private String businessComponentId;

        public MessageStructureCreateRequestDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MessageStructureCreateRequestDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public MessageStructureCreateRequestDTOBuilder xsdContent(String xsdContent) {
            this.xsdContent = xsdContent;
            return this;
        }

        public MessageStructureCreateRequestDTOBuilder namespace(Map<String, Object> namespace) {
            this.namespace = namespace;
            return this;
        }

        public MessageStructureCreateRequestDTOBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public MessageStructureCreateRequestDTOBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public MessageStructureCreateRequestDTOBuilder businessComponentId(String businessComponentId) {
            this.businessComponentId = businessComponentId;
            return this;
        }

        public MessageStructureCreateRequestDTO build() {
            return new MessageStructureCreateRequestDTO(name, description, xsdContent, namespace, metadata, tags, businessComponentId);
        }
    }
}
