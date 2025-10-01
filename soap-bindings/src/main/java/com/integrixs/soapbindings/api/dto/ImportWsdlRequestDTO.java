package com.integrixs.soapbindings.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for importing WSDL from URL
 */
public class ImportWsdlRequestDTO {

    @NotNull(message = "WSDL name is required")
    private String name;

    @NotNull(message = "WSDL URL is required")
    private String wsdlUrl;

    private String description;

    public ImportWsdlRequestDTO() {
    }

    public ImportWsdlRequestDTO(String name, String wsdlUrl, String description) {
        this.name = name;
        this.wsdlUrl = wsdlUrl;
        this.description = description;
    }

    // Getters
    public String getName() { return name; }
    public String getWsdlUrl() { return wsdlUrl; }
    public String getDescription() { return description; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setWsdlUrl(String wsdlUrl) { this.wsdlUrl = wsdlUrl; }
    public void setDescription(String description) { this.description = description; }

    public static ImportWsdlRequestDTOBuilder builder() {
        return new ImportWsdlRequestDTOBuilder();
    }

    public static class ImportWsdlRequestDTOBuilder {
        private String name;
        private String wsdlUrl;
        private String description;

        public ImportWsdlRequestDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ImportWsdlRequestDTOBuilder wsdlUrl(String wsdlUrl) {
            this.wsdlUrl = wsdlUrl;
            return this;
        }

        public ImportWsdlRequestDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ImportWsdlRequestDTO build() {
            return new ImportWsdlRequestDTO(name, wsdlUrl, description);
        }
    }
}
