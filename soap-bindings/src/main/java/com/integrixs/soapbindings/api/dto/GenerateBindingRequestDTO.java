package com.integrixs.soapbindings.api.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for generating SOAP binding
 */
public class GenerateBindingRequestDTO {

    @NotNull(message = "Package name is required")
    private String packageName;

    private boolean autoCompile = true;

    private String outputDirectory;

    // Default constructor
    public GenerateBindingRequestDTO() {
        this.autoCompile = true;
    }

    // All args constructor
    public GenerateBindingRequestDTO(String packageName, boolean autoCompile, String outputDirectory) {
        this.packageName = packageName;
        this.autoCompile = autoCompile;
        this.outputDirectory = outputDirectory;
    }

    // Getters
    public String getPackageName() { return packageName; }
    public boolean isAutoCompile() { return autoCompile; }
    public String getOutputDirectory() { return outputDirectory; }

    // Setters
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setAutoCompile(boolean autoCompile) { this.autoCompile = autoCompile; }
    public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }

    // Builder
    public static GenerateBindingRequestDTOBuilder builder() {
        return new GenerateBindingRequestDTOBuilder();
    }

    public static class GenerateBindingRequestDTOBuilder {
        private String packageName;
        private boolean autoCompile = true;
        private String outputDirectory;

        public GenerateBindingRequestDTOBuilder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public GenerateBindingRequestDTOBuilder autoCompile(boolean autoCompile) {
            this.autoCompile = autoCompile;
            return this;
        }

        public GenerateBindingRequestDTOBuilder outputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }

        public GenerateBindingRequestDTO build() {
            return new GenerateBindingRequestDTO(packageName, autoCompile, outputDirectory);
        }
    }
}
