package com.integrixs.data.model;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing XML namespaces for flow structures.
 *
 * <p>Stores namespace prefix and URI mappings for WSDL definitions.
 *
 * @author Integration Team
 * @since 2.0.0
 */
public class FlowStructureNamespace {

    /**
     * Unique identifier(UUID) for the entity
     */
    private UUID id;

    /**
     * The flow structure this namespace belongs to
     */
    @NotNull(message = "Flow structure is required")
    private FlowStructure flowStructure;

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
    public FlowStructureNamespace() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FlowStructure getFlowStructure() {
        return flowStructure;
    }

    public void setFlowStructure(FlowStructure flowStructure) {
        this.flowStructure = flowStructure;
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
    public static FlowStructureNamespaceBuilder builder() {
        return new FlowStructureNamespaceBuilder();
    }

    public static class FlowStructureNamespaceBuilder {
        private UUID id;
        private FlowStructure flowStructure;
        private String prefix;
        private String uri;
        private boolean isDefault;
        private LocalDateTime createdAt;

        public FlowStructureNamespaceBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FlowStructureNamespaceBuilder flowStructure(FlowStructure flowStructure) {
            this.flowStructure = flowStructure;
            return this;
        }

        public FlowStructureNamespaceBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public FlowStructureNamespaceBuilder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public FlowStructureNamespaceBuilder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public FlowStructureNamespaceBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FlowStructureNamespace build() {
            FlowStructureNamespace instance = new FlowStructureNamespace();
            instance.setId(this.id);
            instance.setFlowStructure(this.flowStructure);
            instance.setPrefix(this.prefix);
            instance.setUri(this.uri);
            instance.setIsDefault(this.isDefault);
            instance.setCreatedAt(this.createdAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "FlowStructureNamespace{" +
                "id=" + id + "prefix=" + prefix + "uri=" + uri + "isDefault=" + isDefault + "createdAt=" + createdAt +
                '}';
    }
}
