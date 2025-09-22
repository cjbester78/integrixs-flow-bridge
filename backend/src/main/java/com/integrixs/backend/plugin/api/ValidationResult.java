package com.integrixs.backend.plugin.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of validation operation
 */
public class ValidationResult {

    private boolean valid;
    private List<String> errors;
    private List<String> warnings;

    // Constructor
    public ValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = errors != null ? errors : new ArrayList<>();
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }

    /**
     * Create a successful validation result
     */
    public static ValidationResult success() {
        return new ValidationResult(true, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Create a successful validation result with warnings
     */
    public static ValidationResult successWithWarnings(List<String> warnings) {
        return new ValidationResult(true, new ArrayList<>(), warnings);
    }

    /**
     * Create a failed validation result
     */
    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors, new ArrayList<>());
    }

    /**
     * Create a failed validation result with single error
     */
    public static ValidationResult failure(String error) {
        List<String> errors = new ArrayList<>();
        errors.add(error);
        return new ValidationResult(false, errors, new ArrayList<>());
    }

    // Getters and Setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
