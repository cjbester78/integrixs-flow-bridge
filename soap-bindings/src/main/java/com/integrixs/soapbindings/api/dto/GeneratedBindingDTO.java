package com.integrixs.soapbindings.api.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * DTO for generated binding details
 */
public class GeneratedBindingDTO {

    private String generationId;
    private String wsdlId;
    private String packageName;
    private String serviceName;
    private String status;
    private Set<String> generatedClasses;
    private LocalDateTime generatedAt;
    private boolean successful;
    private String errorMessage;

    // Default constructor
    public GeneratedBindingDTO() {
        this.generatedClasses = new HashSet<>();
    }

    // All args constructor
    public GeneratedBindingDTO(String generationId, String wsdlId, String packageName, String serviceName, String status, Set<String> generatedClasses, LocalDateTime generatedAt, boolean successful, String errorMessage) {
        this.generationId = generationId;
        this.wsdlId = wsdlId;
        this.packageName = packageName;
        this.serviceName = serviceName;
        this.status = status;
        this.generatedClasses = generatedClasses != null ? generatedClasses : new HashSet<>();
        this.generatedAt = generatedAt;
        this.successful = successful;
        this.errorMessage = errorMessage;
    }

    // Getters
    public String getGenerationId() { return generationId; }
    public String getWsdlId() { return wsdlId; }
    public String getPackageName() { return packageName; }
    public String getServiceName() { return serviceName; }
    public String getStatus() { return status; }
    public Set<String> getGeneratedClasses() { return generatedClasses; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public boolean isSuccessful() { return successful; }
    public String getErrorMessage() { return errorMessage; }

    // Setters
    public void setGenerationId(String generationId) { this.generationId = generationId; }
    public void setWsdlId(String wsdlId) { this.wsdlId = wsdlId; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setStatus(String status) { this.status = status; }
    public void setGeneratedClasses(Set<String> generatedClasses) { this.generatedClasses = generatedClasses; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public void setSuccessful(boolean successful) { this.successful = successful; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Builder
    public static GeneratedBindingDTOBuilder builder() {
        return new GeneratedBindingDTOBuilder();
    }

    public static class GeneratedBindingDTOBuilder {
        private String generationId;
        private String wsdlId;
        private String packageName;
        private String serviceName;
        private String status;
        private Set<String> generatedClasses = new HashSet<>();
        private LocalDateTime generatedAt;
        private boolean successful;
        private String errorMessage;

        public GeneratedBindingDTOBuilder generationId(String generationId) {
            this.generationId = generationId;
            return this;
        }

        public GeneratedBindingDTOBuilder wsdlId(String wsdlId) {
            this.wsdlId = wsdlId;
            return this;
        }

        public GeneratedBindingDTOBuilder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public GeneratedBindingDTOBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public GeneratedBindingDTOBuilder status(String status) {
            this.status = status;
            return this;
        }

        public GeneratedBindingDTOBuilder generatedClasses(Set<String> generatedClasses) {
            this.generatedClasses = generatedClasses;
            return this;
        }

        public GeneratedBindingDTOBuilder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public GeneratedBindingDTOBuilder successful(boolean successful) {
            this.successful = successful;
            return this;
        }

        public GeneratedBindingDTOBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public GeneratedBindingDTO build() {
            return new GeneratedBindingDTO(generationId, wsdlId, packageName, serviceName, status, generatedClasses, generatedAt, successful, errorMessage);
        }
    }
}
