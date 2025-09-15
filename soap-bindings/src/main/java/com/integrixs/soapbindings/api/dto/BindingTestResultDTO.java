package com.integrixs.soapbindings.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for binding test result
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BindingTestResultDTO {

    private String bindingId;
    private boolean reachable;
    private LocalDateTime timestamp;
    private String message;
    private Long responseTimeMillis;
}
