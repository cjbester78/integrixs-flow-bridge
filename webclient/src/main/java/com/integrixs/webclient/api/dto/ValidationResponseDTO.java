package com.integrixs.webclient.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for validation response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponseDTO {
    
    private boolean valid;
    
    @Builder.Default
    private List<ValidationErrorDTO> errors = new ArrayList<>();
    
    @Builder.Default
    private List<String> warnings = new ArrayList<>();
}