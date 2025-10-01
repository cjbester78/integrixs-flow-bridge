package com.integrixs.soapbindings.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a service definition in a WSDL
 */
public class ServiceDefinition {

    private String name;
    private String namespace;
    private List<PortDefinition> ports = new ArrayList<>();

    // Default constructor
    public ServiceDefinition() {
        this.ports = new ArrayList<>();
    }

    // All args constructor
    public ServiceDefinition(String name, String namespace, List<PortDefinition> ports) {
        this.name = name;
        this.namespace = namespace;
        this.ports = ports != null ? ports : new ArrayList<>();
    }

    // Getters
    public String getName() { return name; }
    public String getNamespace() { return namespace; }
    public List<PortDefinition> getPorts() { return ports; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public void setPorts(List<PortDefinition> ports) { this.ports = ports; }

    // Builder
    public static ServiceDefinitionBuilder builder() {
        return new ServiceDefinitionBuilder();
    }

    public static class ServiceDefinitionBuilder {
        private String name;
        private String namespace;
        private List<PortDefinition> ports = new ArrayList<>();

        public ServiceDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ServiceDefinitionBuilder serviceName(String serviceName) {
            this.name = serviceName;
            return this;
        }

        public ServiceDefinitionBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public ServiceDefinitionBuilder ports(List<PortDefinition> ports) {
            this.ports = ports;
            return this;
        }

        public ServiceDefinition build() {
            return new ServiceDefinition(name, namespace, ports);
        }
    }
}