package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO for request using endpoint configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointRequestDTO {

    private String path;

    @NotNull(message = "HTTP method is required")
    private String method;

    private Object payload;

    private String contentType;

    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    @Builder.Default
    private Map<String, String> queryParams = new HashMap<>();

    private String flowId;

    private String adapterId;
}
