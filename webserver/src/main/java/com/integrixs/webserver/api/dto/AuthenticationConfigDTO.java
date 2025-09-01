package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for authentication configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationConfigDTO {
    
    private String authType;
    
    @Builder.Default
    private Map<String, String> credentials = new HashMap<>();
}