package com.integrixs.webclient.domain.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain model representing validation result for inbound messages
 */
@Data
@Builder
public class ValidationResult {
    private boolean valid;
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * Validation error
     */
    @Data
    @Builder
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
     */
    public void addError(String field, String message, ValidationError.ErrorType type) {
        this.valid = false;
        this.errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .type(type)
                .build());
    }

    /**
     * Add warning
     * @param warning Warning message
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    /**
     * Get all error messages
     * @return Error messages
     */
    public String getErrorMessage() {
        if(errors.isEmpty()) {
            return "";
        }

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
}
