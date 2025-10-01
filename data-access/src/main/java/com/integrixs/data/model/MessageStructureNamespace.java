package com.integrixs.data.model;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing XML namespaces for message structures.
 *
 * <p>Stores namespace prefix and URI mappings for XSD schemas.
 *
 * @author Integration Team
 * @since 2.0.0
 */
public class MessageStructureNamespace {

    /**
     * Unique identifier(UUID) for the entity
     */
    private UUID id;

    /**
     * The message structure this namespace belongs to
     */
    @NotNull(message = "Message structure is required")
    private MessageStructure messageStructure;

    /**
     * Namespace prefix(can be null for default namespace)
     */
    @Size(max = 50, message = "Prefix cannot exceed 50 characters")
    private String prefix;

    /**
     * Namespace URI
     */
    @NotBlank(message = "Namespace URI is required")
    @Size(max = 500, message = "URI cannot exceed 500 characters")
    @Pattern(regexp = "^https?://.*|^urn:.*", message = "URI must be a valid HTTP(S) URL or URN")
    private String uri;

    /**
     * Whether this is the default namespace
     */
    private boolean isDefault = false;

    /**
     * Timestamp of entity creation
     */
        private LocalDateTime createdAt;

    // Default constructor
    public MessageStructureNamespace() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MessageStructure getMessageStructure() {
        return messageStructure;
    }

    public void setMessageStructure(MessageStructure messageStructure) {
        this.messageStructure = messageStructure;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isIsDefault() {
        return isDefault;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Builder
    public static MessageStructureNamespaceBuilder builder() {
        return new MessageStructureNamespaceBuilder();
    }

    public static class MessageStructureNamespaceBuilder {
        private UUID id;
        private MessageStructure messageStructure;
        private String prefix;
        private String uri;
        private boolean isDefault;
        private LocalDateTime createdAt;

        public MessageStructureNamespaceBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public MessageStructureNamespaceBuilder messageStructure(MessageStructure messageStructure) {
            this.messageStructure = messageStructure;
            return this;
        }

        public MessageStructureNamespaceBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public MessageStructureNamespaceBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public MessageStructureNamespaceBuilder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public MessageStructureNamespaceBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public MessageStructureNamespace build() {
            MessageStructureNamespace instance = new MessageStructureNamespace();
            instance.setId(this.id);
            instance.setMessageStructure(this.messageStructure);
            instance.setPrefix(this.prefix);
            instance.setUri(this.uri);
            instance.setIsDefault(this.isDefault);
            instance.setCreatedAt(this.createdAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "MessageStructureNamespace{" +
                "id=" + id + "prefix=" + prefix + "uri=" + uri + "isDefault=" + isDefault + "createdAt=" + createdAt +
                '}';
    }
}
