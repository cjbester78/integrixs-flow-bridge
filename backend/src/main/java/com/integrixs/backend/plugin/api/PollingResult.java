package com.integrixs.backend.plugin.api;

import java.util.List;

/**
 * Result of a polling operation
 */
public class PollingResult {

    /**
     * Messages retrieved during polling
     */
    private List<PluginMessage> messages;

    /**
     * Whether more messages are available
     */
    private boolean hasMore;

    /**
     * Token/marker for next poll(for pagination)
     */
    private String nextToken;

    // Default constructor
    public PollingResult() {}

    // All-args constructor
    public PollingResult(List<PluginMessage> messages, boolean hasMore, String nextToken) {
        this.messages = messages;
        this.hasMore = hasMore;
        this.nextToken = nextToken;
    }

    /**
     * Number of messages retrieved
     */
    public int getMessageCount() {
        return messages != null ? messages.size() : 0;
    }

    /**
     * Create empty result
     */
    public static PollingResult empty() {
        return PollingResult.builder()
                .messages(List.of())
                .hasMore(false)
                .build();
    }

    // Getters and Setters
    public List<PluginMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<PluginMessage> messages) {
        this.messages = messages;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getNextToken() {
        return nextToken;
    }

    public void setNextToken(String nextToken) {
        this.nextToken = nextToken;
    }

    // Builder pattern
    public static PollingResultBuilder builder() {
        return new PollingResultBuilder();
    }

    public static class PollingResultBuilder {
        private List<PluginMessage> messages;
        private boolean hasMore;
        private String nextToken;

        public PollingResultBuilder messages(List<PluginMessage> messages) {
            this.messages = messages;
            return this;
        }

        public PollingResultBuilder hasMore(boolean hasMore) {
            this.hasMore = hasMore;
            return this;
        }

        public PollingResultBuilder nextToken(String nextToken) {
            this.nextToken = nextToken;
            return this;
        }

        public PollingResult build() {
            return new PollingResult(messages, hasMore, nextToken);
        }
    }
}
