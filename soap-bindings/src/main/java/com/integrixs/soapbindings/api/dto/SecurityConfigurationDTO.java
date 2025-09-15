package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * DTO for security configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityConfigurationDTO {

    private String securityType;

    @Builder.Default
    private Map<String, String> credentials = new HashMap<>();

    private String certificatePath;
    private String keystorePath;
    private String truststorePath;

    private WsSecurityConfigDTO wsSecurityConfig;
}
