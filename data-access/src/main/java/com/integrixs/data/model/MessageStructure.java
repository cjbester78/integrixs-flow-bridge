package com.integrixs.data.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "message_structures")
public class MessageStructure {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "xsd_content", columnDefinition = "TEXT", nullable = false)
    private String xsdContent;

    // Namespace, metadata, and tags removed - use related tables instead

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "source_type", length = 20)
    private String sourceType = "INTERNAL";

    @Column(name = "is_editable")
    private Boolean isEditable = true;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Import metadata removed - stored in separate table if needed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id", nullable = false)
    private BusinessComponent businessComponent;

    // One - to - many relationship with namespaces
    @OneToMany(mappedBy = "messageStructure", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageStructureNamespace> namespaces = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if(version == null) {
            version = 1;
        }
        if(isActive == null) {
            isActive = true;
        }
    }

    // Default constructor
    public MessageStructure() {
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

    public String getXsdContent() {
        return xsdContent;
    }

    public void setXsdContent(String xsdContent) {
        this.xsdContent = xsdContent;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Boolean getIsEditable() {
        return isEditable;
    }

    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public BusinessComponent getBusinessComponent() {
        return businessComponent;
    }

    public void setBusinessComponent(BusinessComponent businessComponent) {
        this.businessComponent = businessComponent;
    }

    public List<MessageStructureNamespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<MessageStructureNamespace> namespaces) {
        this.namespaces = namespaces;
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

    // Builder
    public static MessageStructureBuilder builder() {
        return new MessageStructureBuilder();
    }

    public static class MessageStructureBuilder {
        private UUID id;
        private String name;
        private String description;
        private String xsdContent;
        private Integer version;
        private String sourceType;
        private Boolean isEditable;
        private Boolean isActive;
        private BusinessComponent businessComponent;
        private List<MessageStructureNamespace> namespaces;
        private User createdBy;
        private User updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public MessageStructureBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public MessageStructureBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MessageStructureBuilder description(String description) {
            this.description = description;
            return this;
        }

        public MessageStructureBuilder xsdContent(String xsdContent) {
            this.xsdContent = xsdContent;
            return this;
        }

        public MessageStructureBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public MessageStructureBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public MessageStructureBuilder isEditable(Boolean isEditable) {
            this.isEditable = isEditable;
            return this;
        }

        public MessageStructureBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public MessageStructureBuilder businessComponent(BusinessComponent businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public MessageStructureBuilder namespaces(List<MessageStructureNamespace> namespaces) {
            this.namespaces = namespaces;
            return this;
        }

        public MessageStructureBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public MessageStructureBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public MessageStructureBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MessageStructureBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public MessageStructure build() {
            MessageStructure instance = new MessageStructure();
            instance.setId(this.id);
            instance.setName(this.name);
            instance.setDescription(this.description);
            instance.setXsdContent(this.xsdContent);
            instance.setVersion(this.version);
            instance.setSourceType(this.sourceType);
            instance.setIsEditable(this.isEditable);
            instance.setIsActive(this.isActive);
            instance.setBusinessComponent(this.businessComponent);
            instance.setNamespaces(this.namespaces);
            instance.setCreatedBy(this.createdBy);
            instance.setUpdatedBy(this.updatedBy);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "MessageStructure{" + 
                "id=" + id + "name=" + name + "description=" + description + "xsdContent=" + xsdContent + "version=" + version + "..." + 
                '}';
    }
}
