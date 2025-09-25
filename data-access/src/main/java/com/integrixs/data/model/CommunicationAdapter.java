package com.integrixs.data.model;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.shared.enums.AdapterType;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing CommunicationAdapter.
 * This maps to the corresponding table in the database.
 */
public class CommunicationAdapter {

    /** Unique identifier(UUID) for the entity */
    private UUID id;

    /** Name of the component */
    private String name;

    private AdapterType type;

    private AdapterConfiguration.AdapterModeEnum mode; // INBOUND or OUTBOUND

    private String direction; // INBOUND, OUTBOUND, BIDIRECTIONAL

    private String configuration;

    private boolean isActive = true;

    /** Detailed description of the component */
    private String description;

    private BusinessComponent businessComponent;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

    private User createdBy;

    private User updatedBy;

    private ExternalAuthentication externalAuthentication;

    private boolean healthy = true;

    private LocalDateTime lastHealthCheck;

    private UUID tenantId;

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
        } catch(Exception e) {
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

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    // Direction enum for adapter types
    public enum Direction {
        INBOUND,   // Receives from external systems(outbound)
        OUTBOUND   // Sends to external systems(inbound)
    }
}
