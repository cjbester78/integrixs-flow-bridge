package com.integrixs.shared.dto;


/**
 * DTO representing a function parameter with type information
 */
public class FunctionParameterDTO {

    private String name;
    private String type;
    private boolean required;
    private String description;

    // Default constructor
    public FunctionParameterDTO() {
    }

    // All args constructor
    public FunctionParameterDTO(String name, String type, boolean required, String description) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.description = description;
    }

    // Getters
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isRequired() { return required; }
    public String getDescription() { return description; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setRequired(boolean required) { this.required = required; }
    public void setDescription(String description) { this.description = description; }

    // Builder
    public static FunctionParameterDTOBuilder builder() {
        return new FunctionParameterDTOBuilder();
    }

    public static class FunctionParameterDTOBuilder {
        private String name;
        private String type;
        private boolean required;
        private String description;

        public FunctionParameterDTOBuilder name(String name) {
            this.name = name;
            return this;
        }

        public FunctionParameterDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public FunctionParameterDTOBuilder required(boolean required) {
            this.required = required;
            return this;
        }

        public FunctionParameterDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public FunctionParameterDTO build() {
            return new FunctionParameterDTO(name, type, required, description);
        }
    }
}
