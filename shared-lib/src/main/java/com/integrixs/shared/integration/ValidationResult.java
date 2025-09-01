package com.integrixs.shared.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of validation operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    private boolean valid;
    @Builder.Default
    private List<String> errors = new ArrayList<>();
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
    
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
}