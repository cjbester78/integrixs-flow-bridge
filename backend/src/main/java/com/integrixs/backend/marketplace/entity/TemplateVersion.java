package com.integrixs.backend.marketplace.entity;

import jakarta.persistence.*;
import com.integrixs.data.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a version of a flow template
 */
@Entity
@Table(name = "template_versions")
public class TemplateVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FlowTemplate template;

    @Column(nullable = false)
    private String version;

    @Column(name = "flow_definition", columnDefinition = "TEXT", nullable = false)
    private String flowDefinition;

    @Column(name = "release_notes", length = 5000)
    private String releaseNotes;

    @Column(name = "is_stable")
    private boolean stable = true;

    @Column(name = "is_latest")
    private boolean latest = false;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "deprecated")
    private boolean deprecated = false;

    @Column(name = "deprecation_message")
    private String deprecationMessage;

    @Column(name = "min_platform_version")
    private String minPlatformVersion;

    @Column(name = "max_platform_version")
    private String maxPlatformVersion;

    @PrePersist
    public void prePersist() {
        publishedAt = LocalDateTime.now();
    }

    // Default constructor
    public TemplateVersion() {
    }

    public FlowTemplate getTemplate() {
        return template;
    }

    public void setTemplate(FlowTemplate template) {
        this.template = template;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFlowDefinition() {
        return flowDefinition;
    }

    public void setFlowDefinition(String flowDefinition) {
        this.flowDefinition = flowDefinition;
    }

    public String getReleaseNotes() {
        return releaseNotes;
    }

    public void setReleaseNotes(String releaseNotes) {
        this.releaseNotes = releaseNotes;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    public boolean isLatest() {
        return latest;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDeprecationMessage() {
        return deprecationMessage;
    }

    public void setDeprecationMessage(String deprecationMessage) {
        this.deprecationMessage = deprecationMessage;
    }

    public String getMinPlatformVersion() {
        return minPlatformVersion;
    }

    public void setMinPlatformVersion(String minPlatformVersion) {
        this.minPlatformVersion = minPlatformVersion;
    }

    public String getMaxPlatformVersion() {
        return maxPlatformVersion;
    }

    public void setMaxPlatformVersion(String maxPlatformVersion) {
        this.maxPlatformVersion = maxPlatformVersion;
    }
}
