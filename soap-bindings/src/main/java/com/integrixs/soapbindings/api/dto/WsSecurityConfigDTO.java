package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for WS - Security configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsSecurityConfigDTO {

    private boolean enableTimestamp;
    private boolean enableSignature;
    private boolean enableEncryption;
    private String usernameToken;
    private String passwordType;
    private String signatureAlgorithm;
    private String encryptionAlgorithm;
    private int timestampTTL;
}
