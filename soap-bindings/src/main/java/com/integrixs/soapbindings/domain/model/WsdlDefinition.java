package com.integrixs.soapbindings.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Domain model representing a WSDL definition
 */
@Data
@Builder
public class WsdlDefinition {
    private String wsdlId;
    private String name;
    private String namespace;
    private String location;
    private String content;
    private WsdlType type;
    @Builder.Default
    private Map<String, ServiceDefinition> services = new HashMap<>();
    @Builder.Default
    private Map<String, String> namespaces = new HashMap<>();
    private String version;
    private boolean validated;

    /**
     * WSDL types
     */
    public enum WsdlType {
        DOCUMENT_LITERAL,
        RPC_LITERAL,
        DOCUMENT_WRAPPED,
        RPC_ENCODED
    }

    /**
     * Service definition within WSDL
     */
    @Data
    @Builder
    public static class ServiceDefinition {
        private String serviceName;
        private String serviceNamespace;
        @Builder.Default
        private Map<String, PortDefinition> ports = new HashMap<>();
        private String documentation;
    }

    /**
     * Port definition within service
     */
    @Data
    @Builder
    public static class PortDefinition {
        private String portName;
        private String bindingName;
        private String address;
        @Builder.Default
        private Map<String, OperationDefinition> operations = new HashMap<>();
    }

    /**
     * Operation definition within port
     */
    @Data
    @Builder
    public static class OperationDefinition {
        private String operationName;
        private String soapAction;
        private MessageDefinition inputMessage;
        private MessageDefinition outputMessage;
        private Set<FaultDefinition> faults;
        private OperationStyle style;

        public enum OperationStyle {
            ONE_WAY,
            REQUEST_RESPONSE,
            SOLICIT_RESPONSE,
            NOTIFICATION
        }
    }

    /**
     * Message definition
     */
    @Data
    @Builder
    public static class MessageDefinition {
        private String messageName;
        private String partName;
        private String elementName;
        private String typeName;
    }

    /**
     * Fault definition
     */
    @Data
    @Builder
    public static class FaultDefinition {
        private String faultName;
        private String messageName;
    }

    /**
     * Add service definition
     * @param service Service definition
     */
    public void addService(ServiceDefinition service) {
        this.services.put(service.getServiceName(), service);
    }

    /**
     * Get all service names
     * @return Set of service names
     */
    public Set<String> getServiceNames() {
        return services.keySet();
    }

    /**
     * Get service by name
     * @param serviceName Service name
     * @return Service definition
     */
    public ServiceDefinition getService(String serviceName) {
        return services.get(serviceName);
    }
}
