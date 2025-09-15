package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for service endpoint details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEndpointDTO {

    private String endpointId;
    private String name;
    private String baseUrl;
    private String type;
    private String description;
    private boolean active;
    private String version;
    private Map<String, String> metadata;
    private boolean requiresAuth;
}
