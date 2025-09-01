package com.integrixs.data.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.adapters.domain.model.AdapterConfiguration;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "communication_adapters")
/**
 * Entity representing CommunicationAdapter.
 * This maps to the corresponding table in the database.
 */
public class CommunicationAdapter {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    /** Unique identifier (UUID) for the entity */
    private UUID id;

    @Column(nullable = false)
    /** Name of the component */
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdapterType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AdapterConfiguration.AdapterModeEnum mode; // INBOUND or OUTBOUND
    
    @Column(length = 20)
    private String direction; // INBOUND, OUTBOUND, BIDIRECTIONAL

    @Column(columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configuration;

    @Column(name = "is_active")
    private boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    /** Detailed description of the component */
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id")
    private BusinessComponent businessComponent;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_auth_id")
    private ExternalAuthentication externalAuthentication;
    
    @Column(name = "is_healthy")
    private boolean healthy = true;
    
    @Column(name = "last_health_check")
    private LocalDateTime lastHealthCheck;

    // === Getters and Setters ===

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

    public AdapterType getType() {
        return type;
    }

    public void setType(AdapterType type) {
        this.type = type;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AdapterConfiguration.AdapterModeEnum getMode() {
        return mode;
    }

    public void setMode(AdapterConfiguration.AdapterModeEnum mode) {
        this.mode = mode;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public void setDirection(String direction) {
        this.direction = direction;
    }

    public UUID getBusinessComponentId() {
        return businessComponent != null ? businessComponent.getId() : null;
    }

    public void setBusinessComponentId(UUID businessComponentId) {
        // This setter is kept for backward compatibility
        // In production code, use setBusinessComponent() instead
    }
    
    public BusinessComponent getBusinessComponent() {
        return businessComponent;
    }
    
    public void setBusinessComponent(BusinessComponent businessComponent) {
        this.businessComponent = businessComponent;
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
    
    public ExternalAuthentication getExternalAuthentication() {
        return externalAuthentication;
    }
    
    public void setExternalAuthentication(ExternalAuthentication externalAuthentication) {
        this.externalAuthentication = externalAuthentication;
    }

    // === Generic Config Deserialization Method ===

    public <T> T getConfig(Class<T> configClass) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.configuration, configClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse adapter configuration for " + this.name, e);
        }
    }
    
    public boolean isHealthy() {
        return healthy;
    }
    
    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
    
    public LocalDateTime getLastHealthCheck() {
        return lastHealthCheck;
    }
    
    public void setLastHealthCheck(LocalDateTime lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }
    
    // Direction enum for adapter types
    public enum Direction {
        INBOUND,    // Receives from external systems (outbound)
        OUTBOUND   // Sends to external systems (inbound)
    }
}
