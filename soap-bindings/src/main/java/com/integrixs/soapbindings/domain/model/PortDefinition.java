package com.integrixs.soapbindings.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a port definition in a WSDL service
 */
public class PortDefinition {

    private String name;
    private String binding;
    private String address;
    private List<OperationDefinition> operations = new ArrayList<>();

    // Default constructor
    public PortDefinition() {
        this.operations = new ArrayList<>();
    }

    // All args constructor
    public PortDefinition(String name, String binding, String address, List<OperationDefinition> operations) {
        this.name = name;
        this.binding = binding;
        this.address = address;
        this.operations = operations != null ? operations : new ArrayList<>();
    }

    // Getters
    public String getName() { return name; }
    public String getBinding() { return binding; }
    public String getAddress() { return address; }
    public List<OperationDefinition> getOperations() { return operations; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setBinding(String binding) { this.binding = binding; }
    public void setAddress(String address) { this.address = address; }
    public void setOperations(List<OperationDefinition> operations) { this.operations = operations; }

    // Additional methods
    public String getPortName() { return name; }

    // Builder
    public static PortDefinitionBuilder builder() {
        return new PortDefinitionBuilder();
    }

    public static class PortDefinitionBuilder {
        private String name;
        private String binding;
        private String address;
        private List<OperationDefinition> operations = new ArrayList<>();

        public PortDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PortDefinitionBuilder portName(String portName) {
            this.name = portName;
            return this;
        }

        public PortDefinitionBuilder binding(String binding) {
            this.binding = binding;
            return this;
        }

        public PortDefinitionBuilder address(String address) {
            this.address = address;
            return this;
        }

        public PortDefinitionBuilder operations(List<OperationDefinition> operations) {
            this.operations = operations;
            return this;
        }

        public PortDefinition build() {
            return new PortDefinition(name, binding, address, operations);
        }
    }
}