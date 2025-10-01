package com.integrixs.data.model;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing WSDL operations for flow structures.
 *
 * <p>Stores operation details extracted from WSDL definitions.
 *
 * @author Integration Team
 * @since 2.0.0
 */
public class FlowStructureOperation {

    /**
     * Unique identifier(UUID) for the entity
     */
    private UUID id;

    /**
     * The flow structure this operation belongs to
     */
    @NotNull(message = "Flow structure is required")
    private FlowStructure flowStructure;

    /**
     * Operation name
     */
    @NotBlank(message = "Operation name is required")
    @Size(max = 255, message = "Operation name cannot exceed 255 characters")
    private String operationName;

    /**
     * SOAP action header value
     */
    @Size(max = 500, message = "SOAP action cannot exceed 500 characters")
    private String soapAction;

    /**
     * Input element name
     */
    @Size(max = 255, message = "Input element name cannot exceed 255 characters")
    private String inputElementName;

    /**
     * Input element namespace URI
     */
    @Size(max = 500, message = "Input element namespace cannot exceed 500 characters")
    private String inputElementNamespace;

    /**
     * Output element name
     */
    @Size(max = 255, message = "Output element name cannot exceed 255 characters")
    private String outputElementName;

    /**
     * Output element namespace URI
     */
    @Size(max = 500, message = "Output element namespace cannot exceed 500 characters")
    private String outputElementNamespace;

    /**
     * Timestamp of entity creation
     */
        private LocalDateTime createdAt;

    // Default constructor
    public FlowStructureOperation() {
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

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getSoapAction() {
        return soapAction;
    }

    public void setSoapAction(String soapAction) {
        this.soapAction = soapAction;
    }

    public String getInputElementName() {
        return inputElementName;
    }

    public void setInputElementName(String inputElementName) {
        this.inputElementName = inputElementName;
    }

    public String getInputElementNamespace() {
        return inputElementNamespace;
    }

    public void setInputElementNamespace(String inputElementNamespace) {
        this.inputElementNamespace = inputElementNamespace;
    }

    public String getOutputElementName() {
        return outputElementName;
    }

    public void setOutputElementName(String outputElementName) {
        this.outputElementName = outputElementName;
    }

    public String getOutputElementNamespace() {
        return outputElementNamespace;
    }

    public void setOutputElementNamespace(String outputElementNamespace) {
        this.outputElementNamespace = outputElementNamespace;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Builder
    public static FlowStructureOperationBuilder builder() {
        return new FlowStructureOperationBuilder();
    }

    public static class FlowStructureOperationBuilder {
        private UUID id;
        private FlowStructure flowStructure;
        private String operationName;
        private String soapAction;
        private String inputElementName;
        private String inputElementNamespace;
        private String outputElementName;
        private String outputElementNamespace;
        private LocalDateTime createdAt;

        public FlowStructureOperationBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public FlowStructureOperationBuilder flowStructure(FlowStructure flowStructure) {
            this.flowStructure = flowStructure;
            return this;
        }

        public FlowStructureOperationBuilder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public FlowStructureOperationBuilder soapAction(String soapAction) {
            this.soapAction = soapAction;
            return this;
        }

        public FlowStructureOperationBuilder inputElementName(String inputElementName) {
            this.inputElementName = inputElementName;
            return this;
        }

        public FlowStructureOperationBuilder inputElementNamespace(String inputElementNamespace) {
            this.inputElementNamespace = inputElementNamespace;
            return this;
        }

        public FlowStructureOperationBuilder outputElementName(String outputElementName) {
            this.outputElementName = outputElementName;
            return this;
        }

        public FlowStructureOperationBuilder outputElementNamespace(String outputElementNamespace) {
            this.outputElementNamespace = outputElementNamespace;
            return this;
        }

        public FlowStructureOperationBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FlowStructureOperation build() {
            FlowStructureOperation instance = new FlowStructureOperation();
            instance.setId(this.id);
            instance.setFlowStructure(this.flowStructure);
            instance.setOperationName(this.operationName);
            instance.setSoapAction(this.soapAction);
            instance.setInputElementName(this.inputElementName);
            instance.setInputElementNamespace(this.inputElementNamespace);
            instance.setOutputElementName(this.outputElementName);
            instance.setOutputElementNamespace(this.outputElementNamespace);
            instance.setCreatedAt(this.createdAt);
            return instance;
        }
    }

    @Override
    public String toString() {
        return "FlowStructureOperation{" +
                "id=" + id + "operationName=" + operationName + "soapAction=" + soapAction + "inputElementName=" + inputElementName + "inputElementNamespace=" + inputElementNamespace + "..." +
                '}';
    }
}
