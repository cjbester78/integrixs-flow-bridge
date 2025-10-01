package com.integrixs.soapbindings.domain.model;

import com.integrixs.soapbindings.domain.enums.WsdlType;
import com.integrixs.soapbindings.domain.enums.OperationStyle;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * Domain model representing a WSDL definition
 */
public class WsdlDefinition {

    private String wsdlId;
    private String name;
    private String namespace;
    private String location;
    private String content;
    private WsdlType type;
    private Map<String, ServiceDefinition> services = new HashMap<>();
    private Map<String, String> namespaces = new HashMap<>();
    private String version;
    private boolean validated;
    private String serviceName;
    private String serviceNamespace;
    private Map<String, PortDefinition> ports = new HashMap<>();
    private String documentation;
    private String portName;
    private String bindingName;
    private String address;
    private Map<String, OperationDefinition> operations = new HashMap<>();
    private String operationName;
    private String soapAction;
    private MessageDefinition inputMessage;
    private MessageDefinition outputMessage;
    private Set<FaultDefinition> faults;
    private OperationStyle style;
    private String messageName;
    private String partName;
    private String elementName;
    private String typeName;
    private String faultName;

    // Default constructor
    public WsdlDefinition() {
        this.faults = new HashSet<>();
    }

    // All args constructor
    public WsdlDefinition(String wsdlId, String name, String namespace, String location, String content, WsdlType type, Map<String, ServiceDefinition> services, Map<String, String> namespaces, String version, boolean validated, String serviceName, String serviceNamespace, Map<String, PortDefinition> ports, String documentation, String portName, String bindingName, String address, Map<String, OperationDefinition> operations, String operationName, String soapAction, MessageDefinition inputMessage, MessageDefinition outputMessage, Set<FaultDefinition> faults, OperationStyle style, String messageName, String partName, String elementName, String typeName, String faultName) {
        this.wsdlId = wsdlId;
        this.name = name;
        this.namespace = namespace;
        this.location = location;
        this.content = content;
        this.type = type;
        this.services = services != null ? services : new HashMap<>();
        this.namespaces = namespaces != null ? namespaces : new HashMap<>();
        this.version = version;
        this.validated = validated;
        this.serviceName = serviceName;
        this.serviceNamespace = serviceNamespace;
        this.ports = ports != null ? ports : new HashMap<>();
        this.documentation = documentation;
        this.portName = portName;
        this.bindingName = bindingName;
        this.address = address;
        this.operations = operations != null ? operations : new HashMap<>();
        this.operationName = operationName;
        this.soapAction = soapAction;
        this.inputMessage = inputMessage;
        this.outputMessage = outputMessage;
        this.faults = faults != null ? faults : new HashSet<>();
        this.style = style;
        this.messageName = messageName;
        this.partName = partName;
        this.elementName = elementName;
        this.typeName = typeName;
        this.faultName = faultName;
    }

    // Getters
    public String getWsdlId() { return wsdlId; }
    public String getName() { return name; }
    public String getNamespace() { return namespace; }
    public String getLocation() { return location; }
    public String getContent() { return content; }
    public WsdlType getType() { return type; }
    public Map<String, ServiceDefinition> getServices() { return services; }
    public Map<String, String> getNamespaces() { return namespaces; }
    public String getVersion() { return version; }
    public boolean isValidated() { return validated; }
    public String getServiceName() { return serviceName; }
    public String getServiceNamespace() { return serviceNamespace; }
    public Map<String, PortDefinition> getPorts() { return ports; }
    public String getDocumentation() { return documentation; }
    public String getPortName() { return portName; }
    public String getBindingName() { return bindingName; }
    public String getAddress() { return address; }
    public Map<String, OperationDefinition> getOperations() { return operations; }
    public String getOperationName() { return operationName; }
    public String getSoapAction() { return soapAction; }
    public MessageDefinition getInputMessage() { return inputMessage; }
    public MessageDefinition getOutputMessage() { return outputMessage; }
    public Set<FaultDefinition> getFaults() { return faults; }
    public OperationStyle getStyle() { return style; }
    public String getMessageName() { return messageName; }
    public String getPartName() { return partName; }
    public String getElementName() { return elementName; }
    public String getTypeName() { return typeName; }
    public String getFaultName() { return faultName; }

    // Setters
    public void setWsdlId(String wsdlId) { this.wsdlId = wsdlId; }
    public void setName(String name) { this.name = name; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public void setLocation(String location) { this.location = location; }
    public void setContent(String content) { this.content = content; }
    public void setType(WsdlType type) { this.type = type; }
    public void setServices(Map<String, ServiceDefinition> services) { this.services = services; }
    public void setNamespaces(Map<String, String> namespaces) { this.namespaces = namespaces; }
    public void setVersion(String version) { this.version = version; }
    public void setValidated(boolean validated) { this.validated = validated; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setServiceNamespace(String serviceNamespace) { this.serviceNamespace = serviceNamespace; }
    public void setPorts(Map<String, PortDefinition> ports) { this.ports = ports; }
    public void setDocumentation(String documentation) { this.documentation = documentation; }
    public void setPortName(String portName) { this.portName = portName; }
    public void setBindingName(String bindingName) { this.bindingName = bindingName; }
    public void setAddress(String address) { this.address = address; }
    public void setOperations(Map<String, OperationDefinition> operations) { this.operations = operations; }
    public void setOperationName(String operationName) { this.operationName = operationName; }
    public void setSoapAction(String soapAction) { this.soapAction = soapAction; }
    public void setInputMessage(MessageDefinition inputMessage) { this.inputMessage = inputMessage; }
    public void setOutputMessage(MessageDefinition outputMessage) { this.outputMessage = outputMessage; }
    public void setFaults(Set<FaultDefinition> faults) { this.faults = faults; }
    public void setStyle(OperationStyle style) { this.style = style; }
    public void setMessageName(String messageName) { this.messageName = messageName; }
    public void setPartName(String partName) { this.partName = partName; }
    public void setElementName(String elementName) { this.elementName = elementName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    public void setFaultName(String faultName) { this.faultName = faultName; }

    // Builder
    public static WsdlDefinitionBuilder builder() {
        return new WsdlDefinitionBuilder();
    }

    public static class WsdlDefinitionBuilder {
        private String wsdlId;
        private String name;
        private String namespace;
        private String location;
        private String content;
        private WsdlType type;
        private Map<String, ServiceDefinition> services = new HashMap<>();
        private Map<String, String> namespaces = new HashMap<>();
        private String version;
        private boolean validated;
        private String serviceName;
        private String serviceNamespace;
        private Map<String, PortDefinition> ports = new HashMap<>();
        private String documentation;
        private String portName;
        private String bindingName;
        private String address;
        private Map<String, OperationDefinition> operations = new HashMap<>();
        private String operationName;
        private String soapAction;
        private MessageDefinition inputMessage;
        private MessageDefinition outputMessage;
        private Set<FaultDefinition> faults = new HashSet<>();
        private OperationStyle style;
        private String messageName;
        private String partName;
        private String elementName;
        private String typeName;
        private String faultName;

        public WsdlDefinitionBuilder wsdlId(String wsdlId) {
            this.wsdlId = wsdlId;
            return this;
        }

        public WsdlDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public WsdlDefinitionBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public WsdlDefinitionBuilder location(String location) {
            this.location = location;
            return this;
        }

        public WsdlDefinitionBuilder content(String content) {
            this.content = content;
            return this;
        }

        public WsdlDefinitionBuilder type(WsdlType type) {
            this.type = type;
            return this;
        }

        public WsdlDefinitionBuilder services(Map<String, ServiceDefinition> services) {
            this.services = services;
            return this;
        }

        public WsdlDefinitionBuilder namespaces(Map<String, String> namespaces) {
            this.namespaces = namespaces;
            return this;
        }

        public WsdlDefinitionBuilder version(String version) {
            this.version = version;
            return this;
        }

        public WsdlDefinitionBuilder validated(boolean validated) {
            this.validated = validated;
            return this;
        }

        public WsdlDefinitionBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public WsdlDefinitionBuilder serviceNamespace(String serviceNamespace) {
            this.serviceNamespace = serviceNamespace;
            return this;
        }

        public WsdlDefinitionBuilder ports(Map<String, PortDefinition> ports) {
            this.ports = ports;
            return this;
        }

        public WsdlDefinitionBuilder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }

        public WsdlDefinitionBuilder portName(String portName) {
            this.portName = portName;
            return this;
        }

        public WsdlDefinitionBuilder bindingName(String bindingName) {
            this.bindingName = bindingName;
            return this;
        }

        public WsdlDefinitionBuilder address(String address) {
            this.address = address;
            return this;
        }

        public WsdlDefinitionBuilder operations(Map<String, OperationDefinition> operations) {
            this.operations = operations;
            return this;
        }

        public WsdlDefinitionBuilder operationName(String operationName) {
            this.operationName = operationName;
            return this;
        }

        public WsdlDefinitionBuilder soapAction(String soapAction) {
            this.soapAction = soapAction;
            return this;
        }

        public WsdlDefinitionBuilder inputMessage(MessageDefinition inputMessage) {
            this.inputMessage = inputMessage;
            return this;
        }

        public WsdlDefinitionBuilder outputMessage(MessageDefinition outputMessage) {
            this.outputMessage = outputMessage;
            return this;
        }

        public WsdlDefinitionBuilder faults(Set<FaultDefinition> faults) {
            this.faults = faults;
            return this;
        }

        public WsdlDefinitionBuilder style(OperationStyle style) {
            this.style = style;
            return this;
        }

        public WsdlDefinitionBuilder messageName(String messageName) {
            this.messageName = messageName;
            return this;
        }

        public WsdlDefinitionBuilder partName(String partName) {
            this.partName = partName;
            return this;
        }

        public WsdlDefinitionBuilder elementName(String elementName) {
            this.elementName = elementName;
            return this;
        }

        public WsdlDefinitionBuilder typeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        public WsdlDefinitionBuilder faultName(String faultName) {
            this.faultName = faultName;
            return this;
        }

        public WsdlDefinition build() {
            return new WsdlDefinition(wsdlId, name, namespace, location, content, type, services, namespaces, version, validated, serviceName, serviceNamespace, ports, documentation, portName, bindingName, address, operations, operationName, soapAction, inputMessage, outputMessage, faults, style, messageName, partName, elementName, typeName, faultName);
        }
    }

    // Additional methods
    public Set<String> getServiceNames() {
        return services != null ? services.keySet() : new HashSet<>();
    }

    public ServiceDefinition getService(String serviceName) {
        return services != null ? services.get(serviceName) : null;
    }

    public void addService(ServiceDefinition service) {
        if (services == null) {
            services = new HashMap<>();
        }
        services.put(service.getName(), service);
    }
}
