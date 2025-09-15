package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for updating SOAP binding
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBindingRequestDTO {

    private String endpointUrl;
    private Map<String, String> soapHeaders;
    private SecurityConfigurationDTO security;
    private Boolean active;
}
