package com.integrixs.soapbindings.domain.model;

/**
 * Represents an operation definition in a WSDL port
 */
public class OperationDefinition {

    private String name;
    private String input;
    private String output;
    private String soapAction;
    private String documentation;

    // Default constructor
    public OperationDefinition() {
    }

    // All args constructor
    public OperationDefinition(String name, String input, String output, String soapAction, String documentation) {
        this.name = name;
        this.input = input;
        this.output = output;
        this.soapAction = soapAction;
        this.documentation = documentation;
    }

    // Getters
    public String getName() { return name; }
    public String getInput() { return input; }
    public String getOutput() { return output; }
    public String getSoapAction() { return soapAction; }
    public String getDocumentation() { return documentation; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setInput(String input) { this.input = input; }
    public void setOutput(String output) { this.output = output; }
    public void setSoapAction(String soapAction) { this.soapAction = soapAction; }
    public void setDocumentation(String documentation) { this.documentation = documentation; }

    // Additional methods
    public String getOperationName() { return name; }

    // Builder
    public static OperationDefinitionBuilder builder() {
        return new OperationDefinitionBuilder();
    }

    public static class OperationDefinitionBuilder {
        private String name;
        private String input;
        private String output;
        private String soapAction;
        private String documentation;

        public OperationDefinitionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public OperationDefinitionBuilder operationName(String operationName) {
            this.name = operationName;
            return this;
        }

        public OperationDefinitionBuilder input(String input) {
            this.input = input;
            return this;
        }

        public OperationDefinitionBuilder output(String output) {
            this.output = output;
            return this;
        }

        public OperationDefinitionBuilder soapAction(String soapAction) {
            this.soapAction = soapAction;
            return this;
        }

        public OperationDefinitionBuilder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }

        public OperationDefinition build() {
            return new OperationDefinition(name, input, output, soapAction, documentation);
        }
    }
}