package com.integrixs.soapbindings.api.dto;

import java.util.Set;
import java.util.HashSet;

/**
 * DTO for WSDL details
 */
public class WsdlDetailsDTO {

    private String wsdlId;
    private String name;
    private String namespace;
    private String location;
    private String type;
    private Set<String> services;
    private String version;
    private boolean validated;

    // Default constructor
    public WsdlDetailsDTO() {
        this.services = new HashSet<>();
    }

    // All args constructor
    public WsdlDetailsDTO(String wsdlId, String name, String namespace, String location, String type, Set<String> services, String version, boolean validated) {
        this.wsdlId = wsdlId;
        this.name = name;
        this.namespace = namespace;
        this.location = location;
        this.type = type;
        this.services = services != null ? services : new HashSet<>();
        this.version = version;
        this.validated = validated;
    }

    // Getters
    public String getWsdlId() { return wsdlId; }
    public String getName() { return name; }
    public String getNamespace() { return namespace; }
    public String getLocation() { return location; }
    public String getType() { return type; }
    public Set<String> getServices() { return services; }
    public String getVersion() { return version; }
    public boolean isValidated() { return validated; }

    // Setters
    public void setWsdlId(String wsdlId) { this.wsdlId = wsdlId; }
    public void setName(String name) { this.name = name; }
    public void setNamespace(String namespace) { this.namespace = namespace; }
    public void setLocation(String location) { this.location = location; }
    public void setType(String type) { this.type = type; }
    public void setServices(Set<String> services) { this.services = services; }
    public void setVersion(String version) { this.version = version; }
    public void setValidated(boolean validated) { this.validated = validated; }

    // Builder
    public static WsdlDetailsDTOBuilder builder() {
        return new WsdlDetailsDTOBuilder();
    }

    public static class WsdlDetailsDTOBuilder {
        private String wsdlId;
        private String name;
        private String namespace;
        private String location;
        private String type;
        private Set<String> services = new HashSet<>();
        private String version;
        private boolean validated;

        public WsdlDetailsDTOBuilder wsdlId(String wsdlId) {
            this.wsdlId = wsdlId;
            return this;
        }

        public WsdlDetailsDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public WsdlDetailsDTOBuilder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public WsdlDetailsDTOBuilder location(String location) {
            this.location = location;
            return this;
        }

        public WsdlDetailsDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public WsdlDetailsDTOBuilder services(Set<String> services) {
            this.services = services;
            return this;
        }

        public WsdlDetailsDTOBuilder version(String version) {
            this.version = version;
            return this;
        }

        public WsdlDetailsDTOBuilder validated(boolean validated) {
            this.validated = validated;
            return this;
        }

        public WsdlDetailsDTO build() {
            return new WsdlDetailsDTO(wsdlId, name, namespace, location, type, services, version, validated);
        }
    }
}
