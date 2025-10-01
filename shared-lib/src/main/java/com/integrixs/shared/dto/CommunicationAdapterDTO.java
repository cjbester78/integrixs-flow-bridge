package com.integrixs.shared.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class CommunicationAdapterDTO {
    private String id;
    private String name;
    private String type;
    private String mode;
    private String description;
    private String businessComponentId;
    private boolean active;
    private Map<String, Object> configuration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    public CommunicationAdapterDTO() {
    }

    public CommunicationAdapterDTO(String id, String name, String type, String mode, String description,
                                   String businessComponentId, boolean active, Map<String, Object> configuration,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, String createdBy, String updatedBy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.mode = mode;
        this.description = description;
        this.businessComponentId = businessComponentId;
        this.active = active;
        this.configuration = configuration;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getMode() { return mode; }
    public String getDescription() { return description; }
    public String getBusinessComponentId() { return businessComponentId; }
    public boolean isActive() { return active; }
    public Map<String, Object> getConfiguration() { return configuration; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setMode(String mode) { this.mode = mode; }
    public void setDescription(String description) { this.description = description; }
    public void setBusinessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; }
    public void setActive(boolean active) { this.active = active; }
    public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public static CommunicationAdapterDTOBuilder builder() {
        return new CommunicationAdapterDTOBuilder();
    }

    public static class CommunicationAdapterDTOBuilder {
        private String id;
        private String name;
        private String type;
        private String mode;
        private String description;
        private String businessComponentId;
        private boolean active;
        private Map<String, Object> configuration;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String createdBy;
        private String updatedBy;

        public CommunicationAdapterDTOBuilder id(String id) { this.id = id; return this; }
        public CommunicationAdapterDTOBuilder name(String name) { this.name = name; return this; }
        public CommunicationAdapterDTOBuilder type(String type) { this.type = type; return this; }
        public CommunicationAdapterDTOBuilder mode(String mode) { this.mode = mode; return this; }
        public CommunicationAdapterDTOBuilder description(String description) { this.description = description; return this; }
        public CommunicationAdapterDTOBuilder businessComponentId(String businessComponentId) { this.businessComponentId = businessComponentId; return this; }
        public CommunicationAdapterDTOBuilder active(boolean active) { this.active = active; return this; }
        public CommunicationAdapterDTOBuilder configuration(Map<String, Object> configuration) { this.configuration = configuration; return this; }
        public CommunicationAdapterDTOBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public CommunicationAdapterDTOBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public CommunicationAdapterDTOBuilder createdBy(String createdBy) { this.createdBy = createdBy; return this; }
        public CommunicationAdapterDTOBuilder updatedBy(String updatedBy) { this.updatedBy = updatedBy; return this; }

        public CommunicationAdapterDTO build() {
            return new CommunicationAdapterDTO(id, name, type, mode, description, businessComponentId,
                                             active, configuration, createdAt, updatedAt, createdBy, updatedBy);
        }
    }
}
