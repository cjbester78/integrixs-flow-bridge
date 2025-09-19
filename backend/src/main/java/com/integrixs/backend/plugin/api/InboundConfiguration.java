package com.integrixs.backend.plugin.api;

import java.util.Map;

/**
 * Configuration for inbound handlers
 */
public class InboundConfiguration {

    /**
     * Polling interval in milliseconds(for polling - based adapters)
     */
    private Long pollingInterval;

    /**
     * Maximum messages per poll
     */
    private Integer maxMessagesPerPoll;

    /**
     * Whether to automatically acknowledge messages
     */
    private Boolean autoAcknowledge;

    /**
     * Connection timeout in milliseconds
     */
    private Long connectionTimeout;

    /**
     * Read timeout in milliseconds
     */
    private Long readTimeout;

    /**
     * Custom configuration properties
     */
    private Map<String, Object> properties;
}
