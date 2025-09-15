package com.integrixs.backend.marketplace.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.integrixs.data.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a version of a flow template
 */
@Entity
@Table(name = "template_versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
        super.prePersist();
        publishedAt = LocalDateTime.now();
    }
}
