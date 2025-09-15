package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationSchemaDTO {
    private String direction;
    private Map<String, Object> schema;
    private boolean hasAdvancedOptions;
    private boolean requiresAuthentication;
    private String[] supportedAuthMethods;
}
