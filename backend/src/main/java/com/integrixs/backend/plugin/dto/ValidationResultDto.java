package com.integrixs.backend.plugin.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for configuration validation results
 */
public class ValidationResultDto {
    private boolean valid;
    private List<ValidationError> errors = new ArrayList<>();
    private String message;

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

    // Default constructor
    public ValidationResultDto() {
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
