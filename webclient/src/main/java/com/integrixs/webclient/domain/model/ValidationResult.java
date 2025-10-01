package com.integrixs.webclient.domain.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing validation result for inbound messages
 */
public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    /**
     * Validation error
     */
    public static class ValidationError {
        private String field;
        private String message;
        private ErrorType type;

        public enum ErrorType {
            MISSING_FIELD,
            INVALID_FORMAT,
            INVALID_VALUE,
            SIZE_LIMIT_EXCEEDED,
            UNAUTHORIZED,
            SCHEMA_VALIDATION_FAILED
        }

        // Getters
        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        public ErrorType getType() {
            return type;
        }

        // Setters
        public void setField(String field) {
            this.field = field;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setType(ErrorType type) {
            this.type = type;
        }

        // Builder
        public static ValidationErrorBuilder builder() {
            return new ValidationErrorBuilder();
        }

        public static class ValidationErrorBuilder {
            private String field;
            private String message;
            private ErrorType type;

            public ValidationErrorBuilder field(String field) {
                this.field = field;
                return this;
            }

            public ValidationErrorBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ValidationErrorBuilder type(ErrorType type) {
                this.type = type;
                return this;
            }

            public ValidationError build() {
                ValidationError error = new ValidationError();
                error.field = this.field;
                error.message = this.message;
                error.type = this.type;
                return error;
            }
        }
    }

    /**
     * Create valid result
     * @return Valid result
     */
    public static ValidationResult valid() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }

    /**
     * Create invalid result with single error
     * @param error Error message
     * @return Invalid result
     */
    public static ValidationResult invalid(String error) {
        return ValidationResult.builder()
                .valid(false)
                .errors(List.of(ValidationError.builder()
                        .message(error)
                        .type(ValidationError.ErrorType.INVALID_VALUE)
                        .build()))
                .build();
    }

    /**
     * Create invalid result with field error
     * @param field Field name
     * @param message Error message
     * @param type Error type
     * @return Invalid result
     */
    public static ValidationResult invalidField(String field, String message, ValidationError.ErrorType type) {
        return ValidationResult.builder()
                .valid(false)
                .errors(List.of(ValidationError.builder()
                        .field(field)
                        .message(message)
                        .type(type)
                        .build()))
                .build();
    }

    /**
     * Add error
     * @param field Field name
     * @param message Error message
     * @param type Error type
     * @return This result
     */
    public ValidationResult withError(String field, String message, ValidationError.ErrorType type) {
        this.valid = false;
        this.errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .type(type)
                .build());
        return this;
    }

    /**
     * Add warning
     * @param warning Warning message
     * @return This result
     */
    public ValidationResult withWarning(String warning) {
        this.warnings.add(warning);
        return this;
    }

    /**
     * Merge with another validation result
     * @param other Other result
     * @return This result
     */
    public ValidationResult merge(ValidationResult other) {
        if(!other.valid) {
            this.valid = false;
        }
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
        return this;
    }

    /**
     * Get errors as formatted string
     * @return Formatted errors
     */
    public String getErrorsAsString() {
        StringBuilder sb = new StringBuilder();
        for(ValidationError error : errors) {
            if(sb.length() > 0) sb.append("; ");
            if(error.getField() != null) {
                sb.append(error.getField()).append(": ");
            }
            sb.append(error.getMessage());
        }
        return sb.toString();
    }

    // Getters
    public boolean isValid() {
        return valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    // Setters
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    // Builder
    public static ValidationResultBuilder builder() {
        return new ValidationResultBuilder();
    }

    public static class ValidationResultBuilder {
        private boolean valid;
        private List<ValidationError> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public ValidationResultBuilder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public ValidationResultBuilder errors(List<ValidationError> errors) {
            this.errors = errors != null ? errors : new ArrayList<>();
            return this;
        }

        public ValidationResultBuilder warnings(List<String> warnings) {
            this.warnings = warnings != null ? warnings : new ArrayList<>();
            return this;
        }

        public ValidationResult build() {
            ValidationResult result = new ValidationResult();
            result.valid = this.valid;
            result.errors = this.errors;
            result.warnings = this.warnings;
            return result;
        }
    }
}