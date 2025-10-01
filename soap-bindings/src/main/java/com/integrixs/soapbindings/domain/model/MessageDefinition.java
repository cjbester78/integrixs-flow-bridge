package com.integrixs.soapbindings.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a message definition in a WSDL
 */
public class MessageDefinition {

    private String name;
    private List<PartDefinition> parts = new ArrayList<>();

    // Default constructor
    public MessageDefinition() {
        this.parts = new ArrayList<>();
    }

    // All args constructor
    public MessageDefinition(String name, List<PartDefinition> parts) {
        this.name = name;
        this.parts = parts != null ? parts : new ArrayList<>();
    }

    // Getters
    public String getName() { return name; }
    public List<PartDefinition> getParts() { return parts; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setParts(List<PartDefinition> parts) { this.parts = parts; }

    // Builder
    public static MessageDefinitionBuilder builder() {
        return new MessageDefinitionBuilder();
    }

    public static class MessageDefinitionBuilder {
        private String name;
        private List<PartDefinition> parts = new ArrayList<>();

        public MessageDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MessageDefinitionBuilder parts(List<PartDefinition> parts) {
            this.parts = parts;
            return this;
        }

        public MessageDefinition build() {
            return new MessageDefinition(name, parts);
        }
    }
}