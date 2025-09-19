package com.integrixs.backend.plugin.api;

import java.util.Map;

/**
 * Configuration for outbound handlers
 */
public class OutboundConfiguration {

    /**
     * Maximum retry attempts
     */
    private Integer maxRetries;

    /**
     * Retry delay in milliseconds
     */
    private Long retryDelay;

    /**
     * Connection timeout in milliseconds
     */
    private Long connectionTimeout;

    /**
     * Write timeout in milliseconds
     */
    private Long writeTimeout;

    /**
     * Whether to use async sending
     */
    private Boolean asyncSending;

    /**
     * Maximum batch size
     */
    private Integer maxBatchSize;

    /**
     * Custom configuration properties
     */
    private Map<String, Object> properties;
}
