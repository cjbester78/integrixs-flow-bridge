package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for updating service endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEndpointDTO {

    private String baseUrl;
    private String description;
    private AuthenticationConfigDTO defaultAuth;
    private Map<String, String> defaultHeaders;
    private ConnectionConfigDTO connectionConfig;
    private Boolean active;
    private String version;
    private Map<String, String> metadata;
}
