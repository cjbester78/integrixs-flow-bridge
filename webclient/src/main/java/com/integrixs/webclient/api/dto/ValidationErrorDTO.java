package com.integrixs.webclient.api.dto;


/**
 * DTO for validation error
 */
public class ValidationErrorDTO {

    private String field;
    private String message;
    private String type;

    // Getters
    public String getField() {
        return field;
    }
    public String getMessage() {
        return message;
    }
    public String getType() {
        return type;
    }

    // Setters
    public void setField(String field) {
        this.field = field;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setType(String type) {
        this.type = type;
    }

    // Builder
    public static ValidationErrorDTOBuilder builder() {
        return new ValidationErrorDTOBuilder();
    }

    public static class ValidationErrorDTOBuilder {
        private String field;
        private String message;
        private String type;

        public ValidationErrorDTOBuilder field(String field) {
            this.field = field;
            return this;
        }

        public ValidationErrorDTOBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ValidationErrorDTOBuilder type(String type) {
            this.type = type;
            return this;
        }

        public ValidationErrorDTO build() {
            ValidationErrorDTO result = new ValidationErrorDTO();
            result.field = this.field;
            result.message = this.message;
            result.type = this.type;
            return result;
        }
    }
}
