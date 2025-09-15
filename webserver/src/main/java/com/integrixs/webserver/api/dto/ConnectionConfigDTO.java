package com.integrixs.webserver.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for connection configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionConfigDTO {

    @Builder.Default
    private int connectionTimeoutSeconds = 10;

    @Builder.Default
    private int readTimeoutSeconds = 30;

    @Builder.Default
    private int maxConnections = 50;

    @Builder.Default
    private int maxConnectionsPerRoute = 10;

    @Builder.Default
    private boolean keepAlive = true;

    @Builder.Default
    private boolean followRedirects = true;
}
