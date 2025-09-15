package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for SOAP binding details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoapBindingDTO {

    private String bindingId;
    private String bindingName;
    private String wsdlId;
    private String serviceName;
    private String portName;
    private String endpointUrl;
    private String bindingStyle;
    private String transport;
    private boolean active;
    private boolean requiresAuth;
    private boolean secureTransport;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
