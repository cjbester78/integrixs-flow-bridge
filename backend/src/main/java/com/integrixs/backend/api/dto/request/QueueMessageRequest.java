package com.integrixs.backend.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;

/**
 * Request object for queuing a new message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMessageRequest {

    @NotBlank(message = "Flow ID is required")
    private String flowId;

    @NotNull(message = "Payload is required")
    private String payload;

    private Map<String, String> headers;

    @Min(0)
    @Max(10)
    @Builder.Default
    private int priority = 5;

    private String correlationId;

    private Map<String, Object> metadata;

    @Builder.Default
    private boolean async = true;
}
