package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response object for adapter test results
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterTestResponse {
    
    private boolean success;
    private String message;
    private String errorDetails;
    
    private Long responseTimeMs;
    private LocalDateTime testedAt;
    
    private Map<String, Object> testResults;
    private Map<String, String> connectionDetails;
    
    @Builder.Default
    private boolean connectionValid = false;
    
    @Builder.Default
    private boolean authenticationValid = false;
}