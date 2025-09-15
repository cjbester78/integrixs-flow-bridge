package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for system configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponse {

    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String dateTimeFormat;
    private Map<String, String> allConfigurations;
}
