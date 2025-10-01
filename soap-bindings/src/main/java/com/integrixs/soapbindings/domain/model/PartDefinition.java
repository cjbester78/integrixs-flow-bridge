package com.integrixs.soapbindings.domain.model;

/**
 * Represents a part of a message in WSDL
 */
public class PartDefinition {

    private String name;
    private String element;
    private String type;

    // Default constructor
    public PartDefinition() {
    }

    // All args constructor
    public PartDefinition(String name, String element, String type) {
        this.name = name;
        this.element = element;
        this.type = type;
    }

    // Getters
    public String getName() { return name; }
    public String getElement() { return element; }
    public String getType() { return type; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setElement(String element) { this.element = element; }
    public void setType(String type) { this.type = type; }

    // Builder
    public static PartDefinitionBuilder builder() {
        return new PartDefinitionBuilder();
    }

    public static class PartDefinitionBuilder {
        private String name;
        private String element;
        private String type;

        public PartDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public PartDefinitionBuilder element(String element) {
            this.element = element;
            return this;
        }

        public PartDefinitionBuilder type(String type) {
            this.type = type;
            return this;
        }

        public PartDefinition build() {
            return new PartDefinition(name, element, type);
        }
    }
}