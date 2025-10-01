package com.integrixs.webserver.domain.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Domain model representing an outbound request to external services
 */
public class OutboundRequest {
    private String requestId;
    private RequestType requestType;
    private String targetUrl;
    private HttpMethod httpMethod;
    private Object payload;
    private String contentType;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private AuthenticationConfig authentication;
    private int timeoutSeconds;
    private RetryConfig retryConfig;
    private String flowId;
    private String adapterId;

    /**
     * Request types
     */
    public enum RequestType {
        REST_API,
        SOAP_SERVICE,
        WEBHOOK,
        GRAPHQL,
        CUSTOM
    }

    /**
     * HTTP methods
     */
    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS
    }

    // Default constructor
    public OutboundRequest() {
        this.headers = new HashMap<>();
        this.queryParams = new HashMap<>();
    }

    // All args constructor
    public OutboundRequest(String requestId, RequestType requestType, String targetUrl, HttpMethod httpMethod,
                          Object payload, String contentType, Map<String, String> headers,
                          Map<String, String> queryParams, AuthenticationConfig authentication,
                          int timeoutSeconds, RetryConfig retryConfig, String flowId, String adapterId) {
        this.requestId = requestId;
        this.requestType = requestType;
        this.targetUrl = targetUrl;
        this.httpMethod = httpMethod;
        this.payload = payload;
        this.contentType = contentType;
        this.headers = headers != null ? headers : new HashMap<>();
        this.queryParams = queryParams != null ? queryParams : new HashMap<>();
        this.authentication = authentication;
        this.timeoutSeconds = timeoutSeconds;
        this.retryConfig = retryConfig;
        this.flowId = flowId;
        this.adapterId = adapterId;
    }

    /**
     * Add header
     * @param key Header key
     * @param value Header value
     */
    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * Add query parameter
     * @param key Parameter key
     * @param value Parameter value
     */
    public void addQueryParam(String key, String value) {
        this.queryParams.put(key, value);
    }

    // Getters
    public String getRequestId() {
        return requestId;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public Object getPayload() {
        return payload;
    }

    public String getContentType() {
        return contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public AuthenticationConfig getAuthentication() {
        return authentication;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public RetryConfig getRetryConfig() {
        return retryConfig;
    }

    public String getFlowId() {
        return flowId;
    }

    public String getAdapterId() {
        return adapterId;
    }

    // Setters
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public void setAuthentication(AuthenticationConfig authentication) {
        this.authentication = authentication;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public void setRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public void setAdapterId(String adapterId) {
        this.adapterId = adapterId;
    }

    // Builder
    public static OutboundRequestBuilder builder() {
        return new OutboundRequestBuilder();
    }

    public static class OutboundRequestBuilder {
        private String requestId;
        private RequestType requestType;
        private String targetUrl;
        private HttpMethod httpMethod;
        private Object payload;
        private String contentType;
        private Map<String, String> headers = new HashMap<>();
        private Map<String, String> queryParams = new HashMap<>();
        private AuthenticationConfig authentication;
        private int timeoutSeconds = 30;
        private RetryConfig retryConfig;
        private String flowId;
        private String adapterId;

        public OutboundRequestBuilder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public OutboundRequestBuilder requestType(RequestType requestType) {
            this.requestType = requestType;
            return this;
        }

        public OutboundRequestBuilder targetUrl(String targetUrl) {
            this.targetUrl = targetUrl;
            return this;
        }

        public OutboundRequestBuilder httpMethod(HttpMethod httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public OutboundRequestBuilder payload(Object payload) {
            this.payload = payload;
            return this;
        }

        public OutboundRequestBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public OutboundRequestBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public OutboundRequestBuilder queryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public OutboundRequestBuilder authentication(AuthenticationConfig authentication) {
            this.authentication = authentication;
            return this;
        }

        public OutboundRequestBuilder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public OutboundRequestBuilder retryConfig(RetryConfig retryConfig) {
            this.retryConfig = retryConfig;
            return this;
        }

        public OutboundRequestBuilder flowId(String flowId) {
            this.flowId = flowId;
            return this;
        }

        public OutboundRequestBuilder adapterId(String adapterId) {
            this.adapterId = adapterId;
            return this;
        }

        public OutboundRequest build() {
            if (this.requestId == null) {
                this.requestId = UUID.randomUUID().toString();
            }
            return new OutboundRequest(requestId, requestType, targetUrl, httpMethod,
                    payload, contentType, headers, queryParams, authentication,
                    timeoutSeconds, retryConfig, flowId, adapterId);
        }
    }

    /**
     * Authentication configuration
     */
    public static class AuthenticationConfig {
        private AuthType authType;
        private Map<String, String> credentials;

        public enum AuthType {
            NONE,
            BASIC,
            BEARER,
            API_KEY,
            OAUTH2,
            CUSTOM
        }

        // Default constructor
        public AuthenticationConfig() {
            this.credentials = new HashMap<>();
        }

        // All args constructor
        public AuthenticationConfig(AuthType authType, Map<String, String> credentials) {
            this.authType = authType;
            this.credentials = credentials != null ? credentials : new HashMap<>();
        }

        // Getters
        public AuthType getAuthType() {
            return authType;
        }

        public Map<String, String> getCredentials() {
            return credentials;
        }

        // Setters
        public void setAuthType(AuthType authType) {
            this.authType = authType;
        }

        public void setCredentials(Map<String, String> credentials) {
            this.credentials = credentials;
        }

        // Builder
        public static AuthenticationConfigBuilder builder() {
            return new AuthenticationConfigBuilder();
        }

        public static class AuthenticationConfigBuilder {
            private AuthType authType;
            private Map<String, String> credentials = new HashMap<>();

            public AuthenticationConfigBuilder authType(AuthType authType) {
                this.authType = authType;
                return this;
            }

            public AuthenticationConfigBuilder credentials(Map<String, String> credentials) {
                this.credentials = credentials;
                return this;
            }

            public AuthenticationConfig build() {
                return new AuthenticationConfig(authType, credentials);
            }
        }
    }

    /**
     * Retry configuration
     */
    public static class RetryConfig {
        private int maxRetries;
        private long retryDelayMillis;
        private boolean exponentialBackoff;
        private double backoffMultiplier;

        // Default constructor
        public RetryConfig() {
            this.maxRetries = 3;
            this.retryDelayMillis = 1000;
            this.exponentialBackoff = true;
            this.backoffMultiplier = 2.0;
        }

        // All args constructor
        public RetryConfig(int maxRetries, long retryDelayMillis, boolean exponentialBackoff, double backoffMultiplier) {
            this.maxRetries = maxRetries;
            this.retryDelayMillis = retryDelayMillis;
            this.exponentialBackoff = exponentialBackoff;
            this.backoffMultiplier = backoffMultiplier;
        }

        // Getters
        public int getMaxRetries() {
            return maxRetries;
        }

        public long getRetryDelayMillis() {
            return retryDelayMillis;
        }

        public boolean isExponentialBackoff() {
            return exponentialBackoff;
        }

        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }

        // Setters
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public void setRetryDelayMillis(long retryDelayMillis) {
            this.retryDelayMillis = retryDelayMillis;
        }

        public void setExponentialBackoff(boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
        }

        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }

        // Builder
        public static RetryConfigBuilder builder() {
            return new RetryConfigBuilder();
        }

        public static class RetryConfigBuilder {
            private int maxRetries = 3;
            private long retryDelayMillis = 1000;
            private boolean exponentialBackoff = true;
            private double backoffMultiplier = 2.0;

            public RetryConfigBuilder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }

            public RetryConfigBuilder retryDelayMillis(long retryDelayMillis) {
                this.retryDelayMillis = retryDelayMillis;
                return this;
            }

            public RetryConfigBuilder exponentialBackoff(boolean exponentialBackoff) {
                this.exponentialBackoff = exponentialBackoff;
                return this;
            }

            public RetryConfigBuilder backoffMultiplier(double backoffMultiplier) {
                this.backoffMultiplier = backoffMultiplier;
                return this;
            }

            public RetryConfig build() {
                return new RetryConfig(maxRetries, retryDelayMillis, exponentialBackoff, backoffMultiplier);
            }
        }
    }
}