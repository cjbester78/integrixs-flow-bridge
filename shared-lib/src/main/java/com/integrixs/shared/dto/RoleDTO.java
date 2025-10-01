package com.integrixs.shared.dto;

import java.time.LocalDateTime;
public class RoleDTO {

    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    // Default constructor
    public RoleDTO() {
    }

    // All args constructor
    public RoleDTO(String id, String name, String description, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Builder
    public static RoleDTOBuilder builder() {
        return new RoleDTOBuilder();
    }

    public static class RoleDTOBuilder {
        private String id;
        private String name;
        private String description;
        private LocalDateTime createdAt;

        public RoleDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public RoleDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public RoleDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public RoleDTO build() {
            return new RoleDTO(id, name, description, createdAt);
        }
    }
}
