package com.integrixs.backend.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request object for updating a communication adapter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdapterRequest {
    
    @NotBlank(message = "Adapter name is required")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;
    
    @NotBlank(message = "Adapter type is required")
    private String type;
    
    @NotBlank(message = "Adapter mode is required")
    private String mode;
    
    private String direction;
    
    @NotNull(message = "Configuration is required")
    private String configuration;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String businessComponentId;
    
    private String externalAuthId;
    
    private boolean active;
}