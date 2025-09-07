package com.integrixs.backend.plugin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for plugin initialization results
 */
@Data
@Builder
public class InitializationResultDto {
    private boolean successful;
    private String pluginId;
    private String message;
    private String error;
    
    public static InitializationResultDto success(String pluginId) {
        return InitializationResultDto.builder()
                .successful(true)
                .pluginId(pluginId)
                .message("Plugin initialized successfully")
                .build();
    }
    
    public static InitializationResultDto failure(String pluginId, String error) {
        return InitializationResultDto.builder()
                .successful(false)
                .pluginId(pluginId)
                .error(error)
                .build();
    }
}