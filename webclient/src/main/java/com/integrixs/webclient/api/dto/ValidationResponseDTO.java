package com.integrixs.webclient.api.dto;


import java.util.ArrayList;
import java.util.List;

/**
 * DTO for validation response
 */
public class ValidationResponseDTO {

    private boolean valid;

    private List<ValidationErrorDTO> errors = new ArrayList<>();

    private List<String> warnings = new ArrayList<>();

    // Getters
    public boolean isValid() {
        return valid;
    }
    public List<ValidationErrorDTO> getErrors() {
        return errors;
    }
    public List<String> getWarnings() {
        return warnings;
    }

    // Setters
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    public void setErrors(List<ValidationErrorDTO> errors) {
        this.errors = errors;
    }
    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    // Builder
    public static ValidationResponseDTOBuilder builder() {
        return new ValidationResponseDTOBuilder();
    }

    public static class ValidationResponseDTOBuilder {
        private boolean valid;
        private List<ValidationErrorDTO> errors;
        private List<String> warnings;

        public ValidationResponseDTOBuilder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public ValidationResponseDTOBuilder errors(List<ValidationErrorDTO> errors) {
            this.errors = errors;
            return this;
        }

        public ValidationResponseDTOBuilder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public ValidationResponseDTO build() {
            ValidationResponseDTO result = new ValidationResponseDTO();
            result.valid = this.valid;
            result.errors = this.errors;
            result.warnings = this.warnings;
            return result;
        }
    }
}
