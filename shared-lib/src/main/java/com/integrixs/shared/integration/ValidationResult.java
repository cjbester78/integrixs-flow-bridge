package com.integrixs.shared.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Result of validation operations
 */
public class ValidationResult {
    private boolean valid;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    // Default constructor
    public ValidationResult() {
    }

    // All args constructor
    public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    // Getters
    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    // Setters
    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    // Builder
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean valid;
        private List<String> errors = new ArrayList<>();
        private List<String> warnings = new ArrayList<>();

        public Builder valid(boolean valid) {
            this.valid = valid;
            return this;
        }

        public Builder errors(List<String> errors) {
            this.errors = errors;
            return this;
        }

        public Builder warnings(List<String> warnings) {
            this.warnings = warnings;
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(valid, errors, warnings);
        }
    }

    // Static factory methods
    public static ValidationResult success() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }

    public static ValidationResult failure(List<String> errors) {
        return ValidationResult.builder()
                .valid(false)
                .errors(errors)
                .build();
    }

    public static ValidationResult failure(String error) {
        return ValidationResult.builder()
                .valid(false)
                .errors(List.of(error))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationResult that = (ValidationResult) o;
        return valid == that.valid &&
               Objects.equals(errors, that.errors) &&
               Objects.equals(warnings, that.warnings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valid, errors, warnings);
    }

    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", errors=" + errors +
                ", warnings=" + warnings +
                '}';
    }
}
