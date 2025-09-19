package com.integrixs.data.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Entity
@Table(name = "business_components", indexes = {
    @Index(name = "idx_bc_name", columnList = "name"),
    @Index(name = "idx_bc_email", columnList = "contact_email")
})
public class BusinessComponent {

    /**
     * Unique identifier(UUID) for the entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Name of the business component
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Business component name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /**
     * Detailed description of the component
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Primary contact email address
     */
    @Column(name = "contact_email", length = 255)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Contact email is required")
    private String contactEmail;

    /**
     * Contact phone number
     */
    @Column(name = "contact_phone", length = 20)
    @Pattern(regexp = "^\\ + ?[1-9]\\d {1,14}$", message = "Invalid phone number format")
    private String contactPhone;

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
     * Status of the business component(ACTIVE, INACTIVE)
     */
    @Column(length = 20)
    @Pattern(regexp = "^(ACTIVE|INACTIVE)$", message = "Status must be ACTIVE or INACTIVE")
    private String status = "ACTIVE";

    /**
     * Department or division name
     */
    @Column(length = 100)
    @Size(max = 100, message = "Department cannot exceed 100 characters")
    private String department;

    /**
     * Communication adapters owned by this component
     */
    @OneToMany(mappedBy = "businessComponent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommunicationAdapter> communicationAdapters = new ArrayList<>();

    /**
     * Integration flows owned by this component
     */
    @OneToMany(mappedBy = "businessComponent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IntegrationFlow> integrationFlows = new ArrayList<>();

    /**
     * Lifecycle callback to ensure timestamps are set
     */
    @PrePersist
    protected void onCreate() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if(updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Lifecycle callback to update timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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

    public List<CommunicationAdapter> getCommunicationAdapters() {
        return communicationAdapters;
    }

    public void setCommunicationAdapters(List<CommunicationAdapter> communicationAdapters) {
        this.communicationAdapters = communicationAdapters;
    }

    public List<IntegrationFlow> getIntegrationFlows() {
        return integrationFlows;
    }

    public void setIntegrationFlows(List<IntegrationFlow> integrationFlows) {
        this.integrationFlows = integrationFlows;
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
        private List<CommunicationAdapter> communicationAdapters;
        private List<IntegrationFlow> integrationFlows;

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

        public BusinessComponentBuilder communicationAdapters(List<CommunicationAdapter> communicationAdapters) {
            this.communicationAdapters = communicationAdapters;
            return this;
        }

        public BusinessComponentBuilder integrationFlows(List<IntegrationFlow> integrationFlows) {
            this.integrationFlows = integrationFlows;
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
            instance.setCommunicationAdapters(this.communicationAdapters);
            instance.setIntegrationFlows(this.integrationFlows);
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
