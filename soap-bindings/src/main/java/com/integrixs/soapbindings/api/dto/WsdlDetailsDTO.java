package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO for WSDL details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsdlDetailsDTO {

    private String wsdlId;
    private String name;
    private String namespace;
    private String location;
    private String type;
    private Set<String> services;
    private String version;
    private boolean validated;
}
