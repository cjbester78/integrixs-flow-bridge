package com.integrixs.soapbindings.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for uploading WSDL
 */
public class UploadWsdlRequestDTO {

    private String name;
    private String wsdlContent;
    private String location;
    private String description;

    // Default constructor
    public UploadWsdlRequestDTO() {
    }

    // All args constructor
    public UploadWsdlRequestDTO(String name, String wsdlContent, String location, String description) {
        this.name = name;
        this.wsdlContent = wsdlContent;
        this.location = location;
        this.description = description;
    }

    // Getters
    public String getName() { return name; }
    public String getWsdlContent() { return wsdlContent; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setWsdlContent(String wsdlContent) { this.wsdlContent = wsdlContent; }
    public void setLocation(String location) { this.location = location; }
    public void setDescription(String description) { this.description = description; }

    // Builder
    public static UploadWsdlRequestDTOBuilder builder() {
        return new UploadWsdlRequestDTOBuilder();
    }

    public static class UploadWsdlRequestDTOBuilder {
        private String name;
        private String wsdlContent;
        private String location;
        private String description;

        public UploadWsdlRequestDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UploadWsdlRequestDTOBuilder wsdlContent(String wsdlContent) {
            this.wsdlContent = wsdlContent;
            return this;
        }

        public UploadWsdlRequestDTOBuilder location(String location) {
            this.location = location;
            return this;
        }

        public UploadWsdlRequestDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public UploadWsdlRequestDTO build() {
            return new UploadWsdlRequestDTO(name, wsdlContent, location, description);
        }
    }
}
