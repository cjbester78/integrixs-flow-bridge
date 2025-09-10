package com.integrixs.backend.marketplace.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.integrixs.data.model.BaseEntity;
import com.integrixs.backend.auth.entity.User;
import com.integrixs.data.model.IntegrationFlow;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Entity representing a flow template in the marketplace
 */
@Entity
@Table(name = "flow_templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FlowTemplate extends BaseEntity {
    
    @Column(nullable = false, unique = true)
    private String slug;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 5000)
    private String detailedDescription;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateCategory category;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateVisibility visibility = TemplateVisibility.PUBLIC;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;
    
    @Column(nullable = false)
    private String version = "1.0.0";
    
    @Column(name = "flow_definition", columnDefinition = "TEXT", nullable = false)
    private String flowDefinition;
    
    @Column(name = "configuration_schema", columnDefinition = "TEXT")
    private String configurationSchema;
    
    @ElementCollection
    @CollectionTable(name = "template_tags", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "template_screenshots", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "screenshot_url")
    private Set<String> screenshots = new HashSet<>();
    
    @Column(name = "icon_url")
    private String iconUrl;
    
    @Column(name = "documentation_url")
    private String documentationUrl;
    
    @Column(name = "source_repository_url")
    private String sourceRepositoryUrl;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TemplateVersion> versions = new HashSet<>();
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TemplateRating> ratings = new HashSet<>();
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TemplateComment> comments = new HashSet<>();
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TemplateInstallation> installations = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "template_dependencies",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "dependency_id")
    )
    private Set<FlowTemplate> dependencies = new HashSet<>();
    
    @ElementCollection
    @CollectionTable(name = "template_requirements", joinColumns = @JoinColumn(name = "template_id"))
    @Column(name = "requirement")
    private Set<String> requirements = new HashSet<>();
    
    @Column(name = "min_platform_version")
    private String minPlatformVersion;
    
    @Column(name = "max_platform_version")
    private String maxPlatformVersion;
    
    @Column(name = "download_count")
    private Long downloadCount = 0L;
    
    @Column(name = "install_count")
    private Long installCount = 0L;
    
    @Column(name = "average_rating")
    private Double averageRating = 0.0;
    
    @Column(name = "rating_count")
    private Long ratingCount = 0L;
    
    @Column(name = "is_certified")
    private boolean certified = false;
    
    @Column(name = "certified_at")
    private LocalDateTime certifiedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certified_by")
    private User certifiedBy;
    
    @Column(name = "is_featured")
    private boolean featured = false;
    
    @Column(name = "featured_until")
    private LocalDateTime featuredUntil;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(name = "deactivation_reason")
    private String deactivationReason;
    
    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (slug == null && name != null) {
            slug = generateSlug(name);
        }
        publishedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        updatedAt = LocalDateTime.now();
    }
    
    private String generateSlug(String name) {
        return name.toLowerCase()
            .replaceAll("[^a-z0-9\\s-]", "")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }
    
    public enum TemplateCategory {
        DATA_INTEGRATION,
        API_INTEGRATION,
        FILE_PROCESSING,
        MESSAGE_PROCESSING,
        DATABASE_SYNC,
        CLOUD_INTEGRATION,
        IOT_INTEGRATION,
        SECURITY,
        MONITORING,
        TRANSFORMATION,
        ORCHESTRATION,
        UTILITY,
        OTHER
    }
    
    public enum TemplateType {
        FLOW,              // Complete flow template
        PATTERN,           // Reusable pattern
        CONNECTOR,         // Adapter configuration
        TRANSFORMATION,    // Transformation logic
        ORCHESTRATION,     // Orchestration template
        SNIPPET            // Code snippet
    }
    
    public enum TemplateVisibility {
        PUBLIC,            // Available to all
        PRIVATE,           // Only visible to author
        ORGANIZATION,      // Only visible to organization members
        UNLISTED          // Accessible via direct link only
    }
}