package com.integrixs.shared.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Result of adapter execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdapterResult {
    private boolean success;
    private Map<String, Object> outputData;
    private String errorMessage;
    private String errorCode;
    private Long processingTime;
    private Map<String, Object> metadata;
    
    public static AdapterResult success(Map<String, Object> outputData) {
        return AdapterResult.builder()
                .success(true)
                .outputData(outputData)
                .build();
    }
    
    public static AdapterResult failure(String errorMessage, String errorCode) {
        return AdapterResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }
}