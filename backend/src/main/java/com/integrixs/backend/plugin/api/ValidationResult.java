package com.integrixs.backend.plugin.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of validation operation
 */
@Data
@AllArgsConstructor
public class ValidationResult {

    private boolean valid;
    private List<String> errors;
    private List<String> warnings;

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
}
