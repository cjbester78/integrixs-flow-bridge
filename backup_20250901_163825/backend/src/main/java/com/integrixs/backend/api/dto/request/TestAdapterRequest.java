package com.integrixs.backend.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Request object for testing an adapter connection
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestAdapterRequest {
    
    @NotBlank(message = "Adapter ID is required")
    private String adapterId;
    
    private String testData;
    
    private Map<String, String> testParameters;
    
    @Builder.Default
    private boolean validateOnly = false;
}