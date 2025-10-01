package com.integrixs.backend.plugin.api;

import java.util.Map;

/**
 * Request to subscribe to events or topics
 */
public class SubscriptionRequest {

    /**
     * Subscription ID(optional, will be generated if not provided)
     */
    private String subscriptionId;

    /**
     * Topic or event type to subscribe to
     */
    private String topic;

    /**
     * Filter criteria for the subscription
     */
    private Map<String, Object> filters;

    /**
     * Additional subscription options
     */
    private Map<String, Object> options;

    /**
     * Callback to invoke when events are received
     */
    private MessageCallback callback;
}
