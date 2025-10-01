package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.config.HttpInboundAdapterConfig;
import com.integrixs.adapters.config.HttpMethod;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.List;import java.util.concurrent.CompletableFuture;
import java.util.List;import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;
import java.util.List;/**
 * HTTP Sender Adapter implementation for HTTP/REST endpoint consumption(INBOUND).
 * Follows middleware convention: Inbound = receives data FROM external systems.
 * Supports various HTTP methods, authentication types, and response processing.
 */
public class HttpInboundAdapter extends AbstractAdapter implements InboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(HttpInboundAdapter.class);


    private final HttpInboundAdapterConfig config;
    private HttpClient httpClient;

    public HttpInboundAdapter(HttpInboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing HTTP inbound adapter with endpoint: {}",
                maskSensitiveUrl(config.getUrl()));

        try {
            validateConfiguration();
            httpClient = createHttpClient();
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("HTTP inbound adapter initialized successfully");
        return AdapterOperationResult.success("HTTP inbound adapter initialized successfully");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying HTTP inbound adapter");
        // HttpClient doesn't need explicit cleanup
        httpClient = null;
        return AdapterOperationResult.success("HTTP inbound adapter shutdown successfully");
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        if(config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            return AdapterOperationResult.failure("Endpoint URL not configured");
        }

        try {
            // Use HEAD request for connection testing to minimize impact
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(config.getUrl()))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofMillis(Math.min(config.getReadTimeout(), 10000))); // Max 10s for test

            addAuthenticationHeaders(requestBuilder);
            addCommonHeaders(requestBuilder);

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int statusCode = response.statusCode();
            if(statusCode >= 200 && statusCode < 400) {
                return AdapterOperationResult.success("Connection test successful, status: " + statusCode);
            } else {
                return AdapterOperationResult.failure("Connection test failed with status: " + statusCode);
            }
        } catch(Exception e) {
            log.debug("Connection test failed", e);
            return AdapterOperationResult.failure("Connection test failed: " + e.getMessage());
        }
    }

    // InboundAdapterPort implementation
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            return performFetch(request.getParameters());
        } catch(Exception e) {
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }

    // Batch operations support(not part of interface but useful for HTTP)
    public AdapterOperationResult fetchBatch(List<FetchRequest> requests) {
        List<AdapterOperationResult> results = new ArrayList<>();
        for(FetchRequest request : requests) {
            results.add(fetch(request));
        }

        long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
        long failureCount = results.size() - successCount;

        if(failureCount == 0) {
            return AdapterOperationResult.success("All " + successCount + " requests fetched successfully");
        } else {
            return AdapterOperationResult.failure(successCount + " fetched, " + failureCount + " failed");
        }
    }

    public CompletableFuture<AdapterOperationResult> fetchBatchAsync(List<FetchRequest> requests) {
        return CompletableFuture.supplyAsync(() -> fetchBatch(requests));
    }

    public boolean isBatchingEnabled() {
        return true;
    }

    public int getBatchSize() {
        // Use configured value if available, otherwise use default
        return config.getMaxBatchSize() != null ? config.getMaxBatchSize() : 100;
    }

    protected AdapterOperationResult performFetch(Object criteria) throws Exception {
        String endpointUrl = config.getUrl();

        try {
            // Build request URL with query parameters if criteria provided
            if(criteria != null && criteria instanceof Map) {
                String queryString = buildQueryString((Map<?, ?>) criteria);
                if(!queryString.isEmpty()) {
                    endpointUrl += (endpointUrl.contains("?") ? "&" : "?") + queryString;
                }
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(endpointUrl))
                    .timeout(Duration.ofMillis(config.getReadTimeout()));

            // Set HTTP method(GET for fetching)
            requestBuilder.GET();

            // Add headers
            addRequestHeaders(requestBuilder);

            // Send request
            HttpRequest request = requestBuilder.build();
            log.debug("Fetching data from HTTP endpoint: {}", maskSensitiveUrl(endpointUrl));

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return processResponse(response);
        } catch(Exception e) {
            log.error("HTTP fetch operation failed", e);
            if(e instanceof java.net.ConnectException) {
                throw new AdapterException("Connection failed", e);
            } else if(e instanceof java.net.SocketTimeoutException) {
                throw new AdapterException("Request timeout: " + e.getMessage(), e);
            }
            throw new AdapterException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    private HttpClient createHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectionTimeout()))
                .followRedirects(config.isFollowRedirects() ?
                        HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER);

        // Configure proxy if needed
        if(config.getProxyHost() != null && !config.getProxyHost().isEmpty()) {
            // Proxy configuration would go here
        }

        return builder.build();
    }

    private void addRequestHeaders(HttpRequest.Builder builder) throws Exception {
        // Accept header
        if(config.getAccept() != null) {
            builder.header("Accept", config.getAccept());
        }
        // Common headers
        addCommonHeaders(builder);

        // Authentication headers
        addAuthenticationHeaders(builder);

        // Custom headers
        if(config.getHeaders() != null) {
            for(Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                builder.header(header.getKey(), header.getValue());
            }
        }
    }

    private void addCommonHeaders(HttpRequest.Builder builder) {
        builder.header("User - Agent", "Integrix - HTTP - Adapter/1.0");

        if(config.isKeepAlive()) {
            builder.header("Connection", "keep - alive");
        }
    }

    private void addAuthenticationHeaders(HttpRequest.Builder builder) throws Exception {
        String authType = config.getAuthenticationType() != null ? config.getAuthenticationType().name() : "NONE";

        if("basic".equalsIgnoreCase(authType)) {
            String credentials = config.getBasicUsername() + ":" + config.getBasicPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(credentials.getBytes());
            builder.header("Authorization", "Basic " + encodedAuth);
        } else if("bearer".equalsIgnoreCase(authType)) {
            builder.header("Authorization", "Bearer " + config.getBearerToken());
        } else if("api - key".equalsIgnoreCase(authType)) {
            String keyHeader = config.getApiKeyHeaderName() != null ?
                    config.getApiKeyHeaderName() : "X - API - Key";
            builder.header(keyHeader, config.getApiKey());
        }
    }

    private AdapterOperationResult processResponse(HttpResponse<String> response) throws Exception {
        int statusCode = response.statusCode();
        String responseBody = response.body();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("statusCode", statusCode);
        responseData.put("headers", response.headers().map());
        responseData.put("body", responseBody);

        if(statusCode >= 200 && statusCode < 300) {
            log.debug("HTTP request successful with status: {}", statusCode);

            // Parse response based on content type
            Object parsedData = parseResponse(responseBody,
                    response.headers().firstValue("Content - Type").orElse("text/plain"));

            return AdapterOperationResult.success(parsedData);
        } else if(statusCode >= 300 && statusCode < 400) {
            // Handle redirects
            String location = response.headers().firstValue("Location").orElse("");
            log.info("HTTP redirect response: {} to {}", statusCode, location);
            return AdapterOperationResult.success(responseData);
        } else if(statusCode >= 400 && statusCode < 500) {
            log.warn("HTTP client error: {} - {}", statusCode, responseBody);
            return AdapterOperationResult.failure("HTTP client error: " + statusCode);
        } else {
            log.error("HTTP server error: {} - {}", statusCode, responseBody);
            return AdapterOperationResult.failure("HTTP server error: " + statusCode);
        }
    }

    private Object parseResponse(String responseBody, String contentType) {
        if(responseBody == null || responseBody.isEmpty()) {
            return null;
        }

        contentType = contentType.toLowerCase();

        if(contentType.contains("json")) {
            // Return as - is for JSON(would normally parse with JSON library)
            return responseBody;
        } else if(contentType.contains("xml")) {
            // Return as - is for XML(would normally parse with XML library)
            return responseBody;
        } else {
            // Plain text or other
            return responseBody;
        }
    }

    private String buildQueryString(Map<?, ?> params) throws Exception {
        StringBuilder query = new StringBuilder();
        for(Map.Entry<?, ?> entry : params.entrySet()) {
            if(query.length() > 0) query.append("&");
            query.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8))
                    .append(" = ")
                    .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return query.toString();
    }

    private String maskSensitiveUrl(String url) {
        if(url == null) return null;
        // Mask any credentials in URL
        return url.replaceAll("://[^:] + :[^@] + @", "://***:***@");
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getUrl() == null || config.getUrl().trim().isEmpty()) {
            throw new AdapterException("Endpoint URL is required", null);
        }

        if(config.getHttpMethod() == null) {
            config.setHttpMethod(HttpMethod.GET); // Default to GET for sender
        }

        // Validate authentication configuration
        String authType = config.getAuthenticationType() != null ? config.getAuthenticationType().name() : "NONE";
        if("basic".equalsIgnoreCase(authType)) {
            if(config.getBasicUsername() == null || config.getBasicPassword() == null) {
                throw new AdapterException(
                        "Username and password required for basic authentication");
            }
        }
    }

    public long getTimeout() {
        return config.getTimeout() != null ? config.getTimeout() : 60000L; // Default 60 seconds if not configured
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("HTTP Sender(Inbound): %s %s, Auth: %s, Polling: %dms",
                config.getHttpMethod() != null ? config.getHttpMethod().name() : "GET",
                maskSensitiveUrl(config.getUrl()),
                config.getAuthenticationType() != null ? config.getAuthenticationType().name() : "NONE",
                60000L);
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.HTTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("HTTP Inbound adapter - receives data via HTTP endpoints")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
    }


    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.HTTP;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.INBOUND;
    }

    @Override
    public void startListening(InboundAdapterPort.DataReceivedCallback callback) {
        // Not implemented for this adapter type
        log.debug("Push-based listening not supported by this adapter type");
    }

    @Override
    public void stopListening() {
        // Not implemented for this adapter type
    }

    @Override
    public boolean isListening() {
        return false;
    }

}
