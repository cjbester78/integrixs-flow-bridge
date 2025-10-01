package com.integrixs.data.model;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an audit trail entry.
 *
 * <p>Tracks all CRUD operations and significant system events.
 *
 * @author Integration Team
 * @since 1.0.0
 */

    public class AuditTrail {

    /**
     * Unique identifier(UUID) for the audit entry
     */
        private UUID id;

    /**
     * Type of entity being audited(e.g., IntegrationFlow, CommunicationAdapter)
     */
    private String entityType;

    /**
     * ID of the entity being audited
     */
    private String entityId;

    /**
     * Action performed
     */
    private AuditAction action;

    /**
     * JSON representation of changes(before and after values for updates)
     */
    private String changes;

    /**
     * User who performed the action
     */
    private User user;

    /**
     * IP address of the user
     */
    private String userIp;

    /**
     * User agent string
     */
    private String userAgent;

    /**
     * Business component context
     */
    private BusinessComponent businessComponent;

    /**
     * Timestamp when the action occurred
     */
        private LocalDateTime createdAt;

    /**
     * Audit actions
     */
    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        DEPLOY,
        ACTIVATE,
        DEACTIVATE,
        EXECUTE,
        LOGIN,
        LOGOUT
    }

    // Default constructor
    public AuditTrail() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getChanges() {
        return changes;
    }

    public void setChanges(String changes) {
        this.changes = changes;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
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

    // Builder
    public static AuditTrailBuilder builder() {
        return new AuditTrailBuilder();
    }

    public static class AuditTrailBuilder {
        private UUID id;
        private String entityType;
        private String entityId;
        private AuditAction action;
        private String changes;
        private User user;
        private String userIp;
        private String userAgent;
        private BusinessComponent businessComponent;
        private LocalDateTime createdAt;

        public AuditTrailBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public AuditTrailBuilder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }

        public AuditTrailBuilder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public AuditTrailBuilder action(AuditAction action) {
            this.action = action;
            return this;
        }

        public AuditTrailBuilder changes(String changes) {
            this.changes = changes;
            return this;
        }

        public AuditTrailBuilder user(User user) {
            this.user = user;
            return this;
        }

        public AuditTrailBuilder userIp(String userIp) {
            this.userIp = userIp;
            return this;
        }

        public AuditTrailBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public AuditTrailBuilder businessComponent(BusinessComponent businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public AuditTrailBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public AuditTrail build() {
            AuditTrail instance = new AuditTrail();
            instance.setId(this.id);
            instance.setEntityType(this.entityType);
            instance.setEntityId(this.entityId);
            instance.setAction(this.action);
            instance.setChanges(this.changes);
            instance.setUser(this.user);
            instance.setUserIp(this.userIp);
            instance.setUserAgent(this.userAgent);
            instance.setBusinessComponent(this.businessComponent);
            instance.setCreatedAt(this.createdAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "AuditTrail{" +
                "id=" + id + "entityType=" + entityType + "entityId=" + entityId + "action=" + action + "changes=" + changes + "..." +
                '}';
    }
}
