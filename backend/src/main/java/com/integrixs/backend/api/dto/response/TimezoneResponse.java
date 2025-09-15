package com.integrixs.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for timezone information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimezoneResponse {

    private String id;
    private String displayName;
}
