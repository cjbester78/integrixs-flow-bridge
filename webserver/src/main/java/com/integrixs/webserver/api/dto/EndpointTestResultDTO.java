package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for endpoint test result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointTestResultDTO {

    private String endpointId;
    private boolean reachable;
    private LocalDateTime timestamp;
    private String message;
    private Long responseTimeMillis;
}
