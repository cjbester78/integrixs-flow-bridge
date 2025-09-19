package com.integrixs.data.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

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
@Entity
@Table(name = "audit_trail", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_created_at", columnList = "created_at"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_business_component", columnList = "business_component_id")
})
public class AuditTrail {

    /**
     * Unique identifier(UUID) for the audit entry
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(columnDefinition = "UUID")
    private UUID id;

    /**
     * Type of entity being audited(e.g., IntegrationFlow, CommunicationAdapter)
     */
    @Column(name = "entity_type", length = 100, nullable = false)
    private String entityType;

    /**
     * ID of the entity being audited
     */
    @Column(name = "entity_id", length = 36, nullable = false)
    private String entityId;

    /**
     * Action performed
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AuditAction action;

    /**
     * JSON representation of changes(before and after values for updates)
     */
    @Column(columnDefinition = "json")
    private String changes;

    /**
     * User who performed the action
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * IP address of the user
     */
    @Column(name = "user_ip", length = 45)
    private String userIp;

    /**
     * User agent string
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * Business component context
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id")
    private BusinessComponent businessComponent;

    /**
     * Timestamp when the action occurred
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
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
