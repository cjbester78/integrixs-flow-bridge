package com.integrixs.soapbindings.domain.model;

/**
 * Represents a fault definition in WSDL
 */
public class FaultDefinition {

    private String name;
    private String message;

    // Default constructor
    public FaultDefinition() {
    }

    // All args constructor
    public FaultDefinition(String name, String message) {
        this.name = name;
        this.message = message;
    }

    // Getters
    public String getName() { return name; }
    public String getMessage() { return message; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setMessage(String message) { this.message = message; }

    // Builder
    public static FaultDefinitionBuilder builder() {
        return new FaultDefinitionBuilder();
    }

    public static class FaultDefinitionBuilder {
        private String name;
        private String message;

        public FaultDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FaultDefinitionBuilder message(String message) {
            this.message = message;
            return this;
        }

        public FaultDefinition build() {
            return new FaultDefinition(name, message);
        }
    }
}