package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for generated binding details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedBindingDTO {
    
    private String generationId;
    private String wsdlId;
    private String packageName;
    private String serviceName;
    private String status;
    private Set<String> generatedClasses;
    private LocalDateTime generatedAt;
    private boolean successful;
    private String errorMessage;
}