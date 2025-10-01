package com.integrixs.data.model;
import com.integrixs.shared.dto.flow.Direction;
import com.integrixs.shared.dto.flow.ProcessingMode;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class FlowStructure {

    private UUID id;

    private String name;

    private String description;

    private ProcessingMode processingMode;

    private Direction direction;

    private String wsdlContent;

    private String sourceType = "INTERNAL";

    // Namespace, metadata, and tags removed - use related tables instead

    private Integer version = 1;

    private Boolean isActive = true;

    private BusinessComponent businessComponent;

    private Set<FlowStructureMessage> flowStructureMessages;

    // One - to - many relationship with namespaces
    private List<FlowStructureNamespace> namespaces = new ArrayList<>();

    // One - to - many relationship with operations
    private List<FlowStructureOperation> operations = new ArrayList<>();

    private User createdBy;

    private User updatedBy;

        private LocalDateTime createdAt;

        private LocalDateTime updatedAt;

    public enum ProcessingMode {
        SYNC,
        ASYNC
    }

    public enum Direction {
        SOURCE,
        TARGET
    }

    protected void onCreate() {
        if(version == null) {
            version = 1;
        }
        if(isActive == null) {
            isActive = true;
        }
    }

    /**
     * Get the structured name with appropriate suffix based on direction
     */
    public String getStructuredName() {
        return name + (direction == Direction.SOURCE ? "_Out" : "_In");
    }

    // Default constructor
    public FlowStructure() {
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

    public ProcessingMode getProcessingMode() {
        return processingMode;
    }

    public void setProcessingMode(ProcessingMode processingMode) {
        this.processingMode = processingMode;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getWsdlContent() {
        return wsdlContent;
    }

    public void setWsdlContent(String wsdlContent) {
        this.wsdlContent = wsdlContent;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public BusinessComponent getBusinessComponent() {
        return businessComponent;
    }

    public void setBusinessComponent(BusinessComponent businessComponent) {
        this.businessComponent = businessComponent;
    }

    public Set<FlowStructureMessage> getFlowStructureMessages() {
        return flowStructureMessages;
    }

    public void setFlowStructureMessages(Set<FlowStructureMessage> flowStructureMessages) {
        this.flowStructureMessages = flowStructureMessages;
    }

    public List<FlowStructureNamespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<FlowStructureNamespace> namespaces) {
        this.namespaces = namespaces;
    }

    public List<FlowStructureOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<FlowStructureOperation> operations) {
        this.operations = operations;
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

    // Builder
    public static FlowStructureBuilder builder() {
        return new FlowStructureBuilder();
    }

    public static class FlowStructureBuilder {
        private UUID id;
        private String name;
        private String description;
        private ProcessingMode processingMode;
        private Direction direction;
        private String wsdlContent;
        private String sourceType;
        private Integer version;
        private Boolean isActive;
        private BusinessComponent businessComponent;
        private Set<FlowStructureMessage> flowStructureMessages;
        private List<FlowStructureNamespace> namespaces;
        private List<FlowStructureOperation> operations;
        private User createdBy;
        private User updatedBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public FlowStructureBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FlowStructureBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FlowStructureBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FlowStructureBuilder processingMode(ProcessingMode processingMode) {
            this.processingMode = processingMode;
            return this;
        }

        public FlowStructureBuilder direction(Direction direction) {
            this.direction = direction;
            return this;
        }

        public FlowStructureBuilder wsdlContent(String wsdlContent) {
            this.wsdlContent = wsdlContent;
            return this;
        }

        public FlowStructureBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public FlowStructureBuilder version(Integer version) {
            this.version = version;
            return this;
        }

        public FlowStructureBuilder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public FlowStructureBuilder businessComponent(BusinessComponent businessComponent) {
            this.businessComponent = businessComponent;
            return this;
        }

        public FlowStructureBuilder flowStructureMessages(Set<FlowStructureMessage> flowStructureMessages) {
            this.flowStructureMessages = flowStructureMessages;
            return this;
        }

        public FlowStructureBuilder namespaces(List<FlowStructureNamespace> namespaces) {
            this.namespaces = namespaces;
            return this;
        }

        public FlowStructureBuilder operations(List<FlowStructureOperation> operations) {
            this.operations = operations;
            return this;
        }

        public FlowStructureBuilder createdBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public FlowStructureBuilder updatedBy(User updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public FlowStructureBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FlowStructureBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public FlowStructure build() {
            FlowStructure instance = new FlowStructure();
            instance.setId(this.id);
            instance.setName(this.name);
            instance.setDescription(this.description);
            instance.setProcessingMode(this.processingMode);
            instance.setDirection(this.direction);
            instance.setWsdlContent(this.wsdlContent);
            instance.setSourceType(this.sourceType);
            instance.setVersion(this.version);
            instance.setIsActive(this.isActive);
            instance.setBusinessComponent(this.businessComponent);
            instance.setFlowStructureMessages(this.flowStructureMessages);
            instance.setNamespaces(this.namespaces);
            instance.setOperations(this.operations);
            instance.setCreatedBy(this.createdBy);
            instance.setUpdatedBy(this.updatedBy);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "FlowStructure{" +
                "id=" + id + "name=" + name + "description=" + description + "processingMode=" + processingMode + "direction=" + direction + "..." +
                '}';
    }
}
