package com.integrixs.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an integration flow.
 *
 * <p>An integration flow defines how data moves from a source adapter
 * to a target adapter, including all transformations and mappings.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Entity
@Table(name = "integration_flows",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_flow_name", columnNames = "name")
    },
    indexes = {
        @Index(name = "idx_flow_name", columnList = "name"),
        @Index(name = "idx_flow_status", columnList = "status"),
        @Index(name = "idx_flow_active", columnList = "is_active"),
        @Index(name = "idx_flow_source", columnList = "inbound_adapter_id"),
        @Index(name = "idx_flow_target", columnList = "outbound_adapter_id")
    }
)
public class IntegrationFlow {

    /**
     * Unique identifier(UUID) for the entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Name of the integration flow
     */
    @Column(nullable = false, length = 100)
    @NotBlank(message = "Flow name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    private String name;

    /**
     * Detailed description of the flow's purpose
     */
    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
    
    /**
     * Tenant ID for multi-tenancy support
     */
    @Column(name = "tenant_id")
    private UUID tenantId;

    /**
     * Source adapter ID(sender - receives data FROM external systems)
     */
    @Column(name = "inbound_adapter_id", nullable = false)
    @NotNull(message = "Source adapter is required")
    private UUID inboundAdapterId;

    /**
     * Target adapter ID(receiver - sends data TO external systems)
     */
    @Column(name = "outbound_adapter_id", nullable = false)
    @NotNull(message = "Target adapter is required")
    private UUID outboundAdapterId;

    /**
     * Source flow structure ID(for structured data flows)
     */
    @Column(name = "source_flow_structure_id")
    private UUID sourceFlowStructureId;

    /**
     * Target flow structure ID(for structured data flows)
     */
    @Column(name = "target_flow_structure_id")
    private UUID targetFlowStructureId;


    /**
     * Current flow status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @NotNull(message = "Status is required")
    private FlowStatus status = FlowStatus.DRAFT;

    /**
     * Whether the flow is currently active
     */
    @Column(name = "is_active")
    @NotNull(message = "Active status is required")
    private boolean isActive = true;

    /**
     * Mapping mode for the flow
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_mode", length = 50, nullable = false)
    @NotNull(message = "Mapping mode is required")
    private MappingMode mappingMode = MappingMode.WITH_MAPPING;

    /**
     * Skip XML conversion for direct file passthrough
     * When true, files are transferred directly without any format conversion
     */
    @Column(name = "skip_xml_conversion")
    private boolean skipXmlConversion = false;

    /**
     * Version of the flow
     */
    @Column(name = "version", length = 50)
    private String version = "1.0";

    /**
     * Flow type - either DIRECT_MAPPING or ORCHESTRATION
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "flow_type", columnDefinition = "ENUM('DIRECT_MAPPING', 'ORCHESTRATION')")
    private FlowType flowType = FlowType.DIRECT_MAPPING;

    /**
     * Timestamp when flow was deployed
     */
    @Column(name = "deployed_at")
    private LocalDateTime deployedAt;

    /**
     * User who deployed the flow
     */
    @Column(name = "deployed_by")
    private UUID deployedBy;

    /**
     * Deployment endpoint URL
     */
    @Column(name = "deployment_endpoint", length = 500)
    @Size(max = 500, message = "Deployment endpoint cannot exceed 500 characters")
    private String deploymentEndpoint;

    /**
     * Deployment metadata in JSON format(stored as TEXT)
     */
    @Column(name = "deployment_metadata", columnDefinition = "TEXT")
    private String deploymentMetadata;

    /**
     * Timestamp of entity creation
     */
    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Timestamp of last entity update
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * User who created the flow
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @NotNull(message = "Created by is required")
    private User createdBy;

    /**
     * Timestamp of last execution
     */
    @Column(name = "last_execution_at")
    private LocalDateTime lastExecutionAt;

    /**
     * Total number of executions
     */
    @Column(name = "execution_count")
    @Min(value = 0, message = "Execution count cannot be negative")
    private int executionCount = 0;

    /**
     * Number of successful executions
     */
    @Column(name = "success_count")
    @Min(value = 0, message = "Success count cannot be negative")
    private int successCount = 0;

    /**
     * Number of failed executions
     */
    @Column(name = "error_count")
    @Min(value = 0, message = "Error count cannot be negative")
    private int errorCount = 0;

    /**
     * Transformations associated with this flow
     */
    @OneToMany(mappedBy = "flow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlowTransformation> transformations = new ArrayList<>();

    /**
     * Orchestration targets for this flow(when flow type is ORCHESTRATION)
     */
    @OneToMany(mappedBy = "flow", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("executionOrder ASC")
    private List<OrchestrationTarget> orchestrationTargets = new ArrayList<>();

    /**
     * Business component that owns this flow
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_component_id")
    private BusinessComponent businessComponent;

    /**
     * User who last updated this flow
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    /**
     * Lifecycle callback to ensure timestamps are set
     */
    @PrePersist
    protected void onCreate() {
        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if(updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Lifecycle callback to update timestamp
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Increments execution statistics
     *
     * @param success whether the execution was successful
     */
    public void recordExecution(boolean success) {
        this.executionCount++;
        if(success) {
            this.successCount++;
        } else {
            this.errorCount++;
        }
        this.lastExecutionAt = LocalDateTime.now();
    }

    /**
     * Adds a transformation to this flow
     *
     * @param transformation the transformation to add
     */
    public void addTransformation(FlowTransformation transformation) {
        if(transformations == null) {
            transformations = new ArrayList<>();
        }
        transformations.add(transformation);
        transformation.setFlow(this);
    }

    /**
     * Removes a transformation from this flow
     *
     * @param transformation the transformation to remove
     */
    public void removeTransformation(FlowTransformation transformation) {
        if(transformations != null) {
            transformations.remove(transformation);
            transformation.setFlow(null);
        }
    }

    // Default constructor
    public IntegrationFlow() {
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public UUID getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getInboundAdapterId() {
        return inboundAdapterId;
    }

    public void setInboundAdapterId(UUID inboundAdapterId) {
        this.inboundAdapterId = inboundAdapterId;
    }

    public UUID getOutboundAdapterId() {
        return outboundAdapterId;
    }

    public void setOutboundAdapterId(UUID outboundAdapterId) {
        this.outboundAdapterId = outboundAdapterId;
    }

    public UUID getSourceFlowStructureId() {
        return sourceFlowStructureId;
    }

    public void setSourceFlowStructureId(UUID sourceFlowStructureId) {
        this.sourceFlowStructureId = sourceFlowStructureId;
    }

    public UUID getTargetFlowStructureId() {
        return targetFlowStructureId;
    }

    public void setTargetFlowStructureId(UUID targetFlowStructureId) {
        this.targetFlowStructureId = targetFlowStructureId;
    }

    public FlowStatus getStatus() {
        return status;
    }

    public void setStatus(FlowStatus status) {
        this.status = status;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public MappingMode getMappingMode() {
        return mappingMode;
    }

    public void setMappingMode(MappingMode mappingMode) {
        this.mappingMode = mappingMode;
    }

    public boolean isSkipXmlConversion() {
        return skipXmlConversion;
    }

    public void setSkipXmlConversion(boolean skipXmlConversion) {
        this.skipXmlConversion = skipXmlConversion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public FlowType getFlowType() {
        return flowType;
    }

    public void setFlowType(FlowType flowType) {
        this.flowType = flowType;
    }

    public LocalDateTime getDeployedAt() {
        return deployedAt;
    }

    public void setDeployedAt(LocalDateTime deployedAt) {
        this.deployedAt = deployedAt;
    }

    public UUID getDeployedBy() {
        return deployedBy;
    }

    public void setDeployedBy(UUID deployedBy) {
        this.deployedBy = deployedBy;
    }

    public String getDeploymentEndpoint() {
        return deploymentEndpoint;
    }

    public void setDeploymentEndpoint(String deploymentEndpoint) {
        this.deploymentEndpoint = deploymentEndpoint;
    }

    public String getDeploymentMetadata() {
        return deploymentMetadata;
    }

    public void setDeploymentMetadata(String deploymentMetadata) {
        this.deploymentMetadata = deploymentMetadata;
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

    public LocalDateTime getLastExecutionAt() {
        return lastExecutionAt;
    }

    public void setLastExecutionAt(LocalDateTime lastExecutionAt) {
        this.lastExecutionAt = lastExecutionAt;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public List<FlowTransformation> getTransformations() {
        return transformations;
    }

    public void setTransformations(List<FlowTransformation> transformations) {
        this.transformations = transformations;
    }

    public List<OrchestrationTarget> getOrchestrationTargets() {
        return orchestrationTargets;
    }

    public void setOrchestrationTargets(List<OrchestrationTarget> orchestrationTargets) {
        this.orchestrationTargets = orchestrationTargets;
    }

    public BusinessComponent getBusinessComponent() {
        return businessComponent;
    }

    public void setBusinessComponent(BusinessComponent businessComponent) {
        this.businessComponent = businessComponent;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    // Builder
    public static IntegrationFlowBuilder builder() {
        return new IntegrationFlowBuilder();
    }

    public static class IntegrationFlowBuilder {
        private UUID id;
        private String name;
        private String description;
        private UUID inboundAdapterId;
        private UUID outboundAdapterId;
        private UUID sourceFlowStructureId;
        private UUID targetFlowStructureId;
        private FlowStatus status;
        private boolean isActive;
        private MappingMode mappingMode;
        private boolean skipXmlConversion;
        private String version;
        private FlowType flowType;
        private LocalDateTime deployedAt;
        private UUID deployedBy;
        private String deploymentEndpoint;
        private String deploymentMetadata;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private User createdBy;
        private LocalDateTime lastExecutionAt;
        private int executionCount;
        private int successCount;
        private int errorCount;
        private List<FlowTransformation> transformations;
        private List<OrchestrationTarget> orchestrationTargets;
        private BusinessComponent businessComponent;
        private User updatedBy;

        public IntegrationFlowBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public IntegrationFlowBuilder name(String name) {
            this.name = name;
            return this;
        }

        public IntegrationFlowBuilder description(String description) {
            this.description = description;
            return this;
        }

        public IntegrationFlowBuilder inboundAdapterId(UUID inboundAdapterId) {
            this.inboundAdapterId = inboundAdapterId;
            return this;
        }

        public IntegrationFlowBuilder outboundAdapterId(UUID outboundAdapterId) {
            this.outboundAdapterId = outboundAdapterId;
            return this;
        }

        public IntegrationFlowBuilder sourceFlowStructureId(UUID sourceFlowStructureId) {
            this.sourceFlowStructureId = sourceFlowStructureId;
            return this;
        }

        public IntegrationFlowBuilder targetFlowStructureId(UUID targetFlowStructureId) {
            this.targetFlowStructureId = targetFlowStructureId;
            return this;
        }

        public IntegrationFlowBuilder status(FlowStatus status) {
            this.status = status;
            return this;
        }

        public IntegrationFlowBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public IntegrationFlowBuilder mappingMode(MappingMode mappingMode) {
            this.mappingMode = mappingMode;
            return this;
        }

        public IntegrationFlowBuilder skipXmlConversion(boolean skipXmlConversion) {
            this.skipXmlConversion = skipXmlConversion;
            return this;
        }

        public IntegrationFlowBuilder version(String version) {
            this.version = version;
            return this;
        }

        public IntegrationFlowBuilder flowType(FlowType flowType) {
            this.flowType = flowType;
            return this;
        }

        public IntegrationFlowBuilder deployedAt(LocalDateTime deployedAt) {
            this.deployedAt = deployedAt;
            return this;
        }

        public IntegrationFlowBuilder deployedBy(UUID deployedBy) {
            this.deployedBy = deployedBy;
            return this;
        }

        public IntegrationFlowBuilder deploymentEndpoint(String deploymentEndpoint) {
            this.deploymentEndpoint = deploymentEndpoint;
            return this;
        }

        public IntegrationFlowBuilder deploymentMetadata(String deploymentMetadata) {
            this.deploymentMetadata = deploymentMetadata;
            return this;
        }

        public IntegrationFlowBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public IntegrationFlowBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public IntegrationFlowBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public IntegrationFlowBuilder lastExecutionAt(LocalDateTime lastExecutionAt) {
            this.lastExecutionAt = lastExecutionAt;
            return this;
        }

        public IntegrationFlowBuilder executionCount(int executionCount) {
            this.executionCount = executionCount;
            return this;
        }

        public IntegrationFlowBuilder successCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public IntegrationFlowBuilder errorCount(int errorCount) {
            this.errorCount = errorCount;
            return this;
        }

        public IntegrationFlowBuilder transformations(List<FlowTransformation> transformations) {
            this.transformations = transformations;
            return this;
        }

        public IntegrationFlowBuilder orchestrationTargets(List<OrchestrationTarget> orchestrationTargets) {
            this.orchestrationTargets = orchestrationTargets;
            return this;
        }

        public IntegrationFlowBuilder businessComponent(BusinessComponent businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public IntegrationFlowBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public IntegrationFlow build() {
            IntegrationFlow instance = new IntegrationFlow();
            instance.setId(this.id);
            instance.setName(this.name);
            instance.setDescription(this.description);
            instance.setInboundAdapterId(this.inboundAdapterId);
            instance.setOutboundAdapterId(this.outboundAdapterId);
            instance.setSourceFlowStructureId(this.sourceFlowStructureId);
            instance.setTargetFlowStructureId(this.targetFlowStructureId);
            instance.setStatus(this.status);
            instance.setActive(this.isActive);
            instance.setMappingMode(this.mappingMode);
            instance.setSkipXmlConversion(this.skipXmlConversion);
            instance.setVersion(this.version != null ? this.version : "1.0");
            instance.setFlowType(this.flowType);
            instance.setDeployedAt(this.deployedAt);
            instance.setDeployedBy(this.deployedBy);
            instance.setDeploymentEndpoint(this.deploymentEndpoint);
            instance.setDeploymentMetadata(this.deploymentMetadata);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            instance.setCreatedBy(this.createdBy);
            instance.setLastExecutionAt(this.lastExecutionAt);
            instance.setExecutionCount(this.executionCount);
            instance.setSuccessCount(this.successCount);
            instance.setErrorCount(this.errorCount);
            instance.setTransformations(this.transformations);
            instance.setOrchestrationTargets(this.orchestrationTargets);
            instance.setBusinessComponent(this.businessComponent);
            instance.setUpdatedBy(this.updatedBy);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "IntegrationFlow{" + 
                "id=" + id + "name=" + name + "description=" + description + "inboundAdapterId=" + inboundAdapterId + "outboundAdapterId=" + outboundAdapterId + "..." + 
                '}';
    }
}
