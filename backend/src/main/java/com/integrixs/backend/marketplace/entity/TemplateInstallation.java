package com.integrixs.backend.marketplace.entity;

import jakarta.persistence.*;
import com.integrixs.data.model.BaseEntity;
import com.integrixs.backend.auth.entity.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing an installation of a template
 */
@Entity
@Table(name = "template_installations")
public class TemplateInstallation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private FlowTemplate template;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private TemplateVersion version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "flow_id")
    private UUID flowId; // The created flow from this template

    @Column(name = "installed_at", nullable = false)
    private LocalDateTime installedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InstallationStatus status = InstallationStatus.ACTIVE;

    @ElementCollection
    @CollectionTable(name = "installation_configuration", joinColumns = @JoinColumn(name = "installation_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> configuration = new HashMap<>();

    @Column(name = "is_auto_update_enabled")
    private boolean autoUpdateEnabled = false;

    @Column(name = "uninstalled_at")
    private LocalDateTime uninstalledAt;

    @Column(name = "uninstall_reason")
    private String uninstallReason;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        installedAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        super.preUpdate();
        updatedAt = LocalDateTime.now();
    }

    public enum InstallationStatus {
        ACTIVE,
        INACTIVE,
        UNINSTALLED,
        FAILED,
        UPDATING
    }

    // Default constructor
    public TemplateInstallation() {
    }

    public FlowTemplate getTemplate() {
        return template;
    }

    public void setTemplate(FlowTemplate template) {
        this.template = template;
    }

    public TemplateVersion getVersion() {
        return version;
    }

    public void setVersion(TemplateVersion version) {
        this.version = version;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Organization getOrganization() {
        return organization;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    public UUID getFlowId() {
        return flowId;
    }

    public void setFlowId(UUID flowId) {
        this.flowId = flowId;
    }

    public LocalDateTime getInstalledAt() {
        return installedAt;
    }

    public void setInstalledAt(LocalDateTime installedAt) {
        this.installedAt = installedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public InstallationStatus getStatus() {
        return status;
    }

    public void setStatus(InstallationStatus status) {
        this.status = status;
    }

    public boolean isAutoUpdateEnabled() {
        return autoUpdateEnabled;
    }

    public void setAutoUpdateEnabled(boolean autoUpdateEnabled) {
        this.autoUpdateEnabled = autoUpdateEnabled;
    }

    public LocalDateTime getUninstalledAt() {
        return uninstalledAt;
    }

    public void setUninstalledAt(LocalDateTime uninstalledAt) {
        this.uninstalledAt = uninstalledAt;
    }

    public String getUninstallReason() {
        return uninstallReason;
    }

    public void setUninstallReason(String uninstallReason) {
        this.uninstallReason = uninstallReason;
    }
}
