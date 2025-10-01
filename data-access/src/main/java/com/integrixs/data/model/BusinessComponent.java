package com.integrixs.data.model;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a business component.
 *
 * <p>A business component represents an organizational unit or department
 * that owns and manages integration flows and adapters.
 *
 * @author Integration Team
 * @since 1.0.0
 */
public class BusinessComponent {

    /**
     * Unique identifier(UUID) for the entity
     */
    private UUID id;

    /**
     * Name of the business component
     */
    @NotBlank(message = "Business component name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /**
     * Detailed description of the component
     */
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Primary contact email address
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    /**
     * Contact phone number
     */
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String contactPhone;

    /**
     * Timestamp of entity creation
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
    private LocalDateTime updatedAt;

    /**
     * Status of the business component(ACTIVE, INACTIVE)
     */
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    private String status = "ACTIVE";

    /**
     * Department or division name
     */
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;


    /**
     * Lifecycle callback to ensure timestamps are set
     */

    // Default constructor
    public BusinessComponent() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }


    // Builder
    public static BusinessComponentBuilder builder() {
        return new BusinessComponentBuilder();
    }

    public static class BusinessComponentBuilder {
        private UUID id;
        private String name;
        private String description;
        private String contactEmail;
        private String contactPhone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String status;
        private String department;

        public BusinessComponentBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public BusinessComponentBuilder name(String name) {
            this.name = name;
            return this;
        }

        public BusinessComponentBuilder description(String description) {
            this.description = description;
            return this;
        }

        public BusinessComponentBuilder contactEmail(String contactEmail) {
            this.contactEmail = contactEmail;
            return this;
        }

        public BusinessComponentBuilder contactPhone(String contactPhone) {
            this.contactPhone = contactPhone;
            return this;
        }

        public BusinessComponentBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public BusinessComponentBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public BusinessComponentBuilder status(String status) {
            this.status = status;
            return this;
        }

        public BusinessComponentBuilder department(String department) {
            this.department = department;
            return this;
        }


        public BusinessComponent build() {
            BusinessComponent instance = new BusinessComponent();
            instance.setId(this.id);
            instance.setName(this.name);
            instance.setDescription(this.description);
            instance.setContactEmail(this.contactEmail);
            instance.setContactPhone(this.contactPhone);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setStatus(this.status);
            instance.setDepartment(this.department);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "BusinessComponent{" +
                "id=" + id + "name=" + name + "description=" + description + "contactEmail=" + contactEmail + "contactPhone=" + contactPhone + "..." +
                '}';
    }
}
