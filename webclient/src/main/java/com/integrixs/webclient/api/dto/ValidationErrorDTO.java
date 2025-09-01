package com.integrixs.webclient.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for validation error
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationErrorDTO {
    
    private String field;
    private String message;
    private String type;
}