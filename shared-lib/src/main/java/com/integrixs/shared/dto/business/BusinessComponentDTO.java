package com.integrixs.shared.dto.business;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DTO for business component information.
 *
 * <p>Represents a business entity or department that owns
 * integration flows and adapters within the system.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class BusinessComponentDTO {

    private String id;
    private String name;
    private String description;
    private String contactEmail;
    private String contactPhone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default constructor
    public BusinessComponentDTO() {
    }

    // All args constructor
    public BusinessComponentDTO(String id, String name, String description, String contactEmail, String contactPhone, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getContactEmail() { return contactEmail; }
    public String getContactPhone() { return contactPhone; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static BusinessComponentDTOBuilder builder() {
        return new BusinessComponentDTOBuilder();
    }

    public static class BusinessComponentDTOBuilder {
        private String id;
        private String name;
        private String description;
        private String contactEmail;
        private String contactPhone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public BusinessComponentDTOBuilder id(String id) {
            this.id = id;
            return this;
        }

        public BusinessComponentDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BusinessComponentDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BusinessComponentDTOBuilder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public BusinessComponentDTOBuilder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public BusinessComponentDTOBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BusinessComponentDTOBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public BusinessComponentDTO build() {
            return new BusinessComponentDTO(id, name, description, contactEmail, contactPhone, createdAt, updatedAt);
        }
    }
}
