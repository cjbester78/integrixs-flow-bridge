package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity class providing common fields for all entities.
 *
 * @author Integration Team
 * @since 2.0.0
 */
public abstract class BaseEntity {

    /**
     * Unique identifier(UUID) for the entity
     */
    private UUID id;

    /**
     * Timestamp of entity creation
     */
        private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
        private LocalDateTime updatedAt;

    /**
     * User who created this entity
     */
    private User createdBy;

    /**
     * User who last updated this entity
     */
    private User updatedBy;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
}
