package com.integrixs.adapters.social.base;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents a response from a social media platform API
 */
public class SocialMediaResponse {
    public SocialMediaResponse() {
    }



    private String id;
    private String platform;
    private ResponseStatus status;
    private String message;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
    private String postId;
    private String postUrl;
    private Map<String, Object> metrics;
    private RateLimitInfo rateLimitInfo;

    // Direct fields for backward compatibility
    private int remaining;
    private int limit;
    private long resetTime;
    private int retryAfter;

    public enum ResponseStatus {
        SUCCESS,
        FAILED,
        RATE_LIMITED,
        UNAUTHORIZED,
        NOT_FOUND,
        VALIDATION_ERROR,
        PLATFORM_ERROR
    }

                    public static class RateLimitInfo {
        private int remaining;
        private int limit;
        private long resetTime;
        private int retryAfter;
    }
    // Getters and Setters
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getPlatform() {
        return platform;
    }
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    public ResponseStatus getStatus() {
        return status;
    }
    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Map<String, Object> getData() {
        return data;
    }
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public String getPostUrl() {
        return postUrl;
    }
    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }
    public Map<String, Object> getMetrics() {
        return metrics;
    }
    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }
    public RateLimitInfo getRateLimitInfo() {
        return rateLimitInfo;
    }
    public void setRateLimitInfo(RateLimitInfo rateLimitInfo) {
        this.rateLimitInfo = rateLimitInfo;
    }
    public int getRemaining() {
        return remaining;
    }
    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
    public int getLimit() {
        return limit;
    }
    public void setLimit(int limit) {
        this.limit = limit;
    }
    public long getResetTime() {
        return resetTime;
    }
    public void setResetTime(long resetTime) {
        this.resetTime = resetTime;
    }
    public int getRetryAfter() {
        return retryAfter;
    }
    public void setRetryAfter(int retryAfter) {
        this.retryAfter = retryAfter;
    }
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String platform;
        private ResponseStatus status;
        private String message;
        private Map<String, Object> data;
        private LocalDateTime timestamp;
        private String postId;
        private String postUrl;
        private Map<String, Object> metrics;
        private RateLimitInfo rateLimitInfo;
        private int remaining;
        private int limit;
        private long resetTime;
        private int retryAfter;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder status(ResponseStatus status) {
            this.status = status;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder data(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder postId(String postId) {
            this.postId = postId;
            return this;
        }

        public Builder postUrl(String postUrl) {
            this.postUrl = postUrl;
            return this;
        }

        public Builder metrics(Map<String, Object> metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder rateLimitInfo(RateLimitInfo rateLimitInfo) {
            this.rateLimitInfo = rateLimitInfo;
            return this;
        }

        public Builder remaining(int remaining) {
            this.remaining = remaining;
            return this;
        }

        public Builder limit(int limit) {
            this.limit = limit;
            return this;
        }

        public Builder resetTime(long resetTime) {
            this.resetTime = resetTime;
            return this;
        }

        public Builder retryAfter(int retryAfter) {
            this.retryAfter = retryAfter;
            return this;
        }

        public SocialMediaResponse build() {
            SocialMediaResponse obj = new SocialMediaResponse();
            obj.id = this.id;
            obj.platform = this.platform;
            obj.status = this.status;
            obj.message = this.message;
            obj.data = this.data;
            obj.timestamp = this.timestamp;
            obj.postId = this.postId;
            obj.postUrl = this.postUrl;
            obj.metrics = this.metrics;
            obj.rateLimitInfo = this.rateLimitInfo;
            obj.remaining = this.remaining;
            obj.limit = this.limit;
            obj.resetTime = this.resetTime;
            obj.retryAfter = this.retryAfter;
            return obj;
        }
    }
}
