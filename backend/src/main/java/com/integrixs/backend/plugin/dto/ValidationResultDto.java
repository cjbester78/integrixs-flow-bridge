package com.integrixs.backend.plugin.dto;

import lombok.Builder;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for configuration validation results
 */
@Data
@Builder
public class ValidationResultDto {
    private boolean valid;
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();
    private String message;
    
    @Data
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
        private String value;
    }
    
    public static ValidationResultDto success() {
        return ValidationResultDto.builder()
                .valid(true)
                .message("Configuration is valid")
                .build();
    }
    
    public static ValidationResultDto error(String message) {
        return ValidationResultDto.builder()
                .valid(false)
                .message(message)
                .build();
    }
    
    public static ValidationResultDto withErrors(List<ValidationError> errors) {
        return ValidationResultDto.builder()
                .valid(false)
                .errors(errors)
                .message("Configuration validation failed")
                .build();
    }
}