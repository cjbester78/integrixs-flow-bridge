package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;


import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.config.HttpOutboundAdapterConfig;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.HashMap;import java.util.List;import java.util.Base64;
import java.util.HashMap;import java.util.List;import java.util.concurrent.CompletableFuture;
import java.util.HashMap;import java.util.List;import java.util.concurrent.TimeUnit;
import java.util.HashMap;import java.util.List;
import java.util.HashMap;import java.util.Map;
import java.util.HashMap;import java.util.List;/**
import java.util.HashMap; * HTTP Receiver Adapter implementation for sending data to HTTP endpoints(OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Supports various HTTP methods, authentication, and content types.
 */
public class HttpOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(HttpOutboundAdapter.class);


    private final HttpOutboundAdapterConfig config;
    private HttpClient httpClient;

    public HttpOutboundAdapter(HttpOutboundAdapterConfig config) {
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing HTTP outbound adapter(outbound) with endpoint: {}",
                maskSensitiveUrl(config.getEndpointUrl()));

        try {
            validateConfiguration();
            initializeHttpClient();

            log.info("HTTP outbound adapter initialized successfully");
            return AdapterOperationResult.success("Initialized successfully");
        } catch(AdapterException e) {
            log.error("Configuration error during initialization", e);
            return AdapterOperationResult.failure("Configuration error: " + e.getMessage());
        }
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying HTTP outbound adapter");
        // HttpClient doesn't need explicit cleanup
        httpClient = null;
        return AdapterOperationResult.success("Shutdown successfully");
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
        String endpoint = config.getTargetEndpointUrl();
        if(endpoint == null || endpoint.trim().isEmpty()) {
            return AdapterOperationResult.failure("Target endpoint URL not configured");
        }

        try {
            // Use HEAD request for connection testing
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(Math.min(config.getConnectionTimeout() / 1000, 10))); // Max 10s for test

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

    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return performSend(request.getPayload());
        } catch(Exception e) {
            return AdapterOperationResult.failure("Send failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }

    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        List<AdapterOperationResult> results = new ArrayList<>();
        for(SendRequest request : requests) {
            results.add(send(request));
        }

        long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
        long failureCount = results.size() - successCount;

        if(failureCount == 0) {
            return AdapterOperationResult.success("All " + successCount + " requests sent successfully");
        } else {
            return AdapterOperationResult.failure(
                    successCount + " sent, " + failureCount + " failed");
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }

    @Override
    public boolean supportsBatchOperations() {
        return true;
    }

    @Override
    public int getMaxBatchSize() {
        // Use configured value if available, otherwise use default
        return config.getMaxBatchSize() != null ? config.getMaxBatchSize() : 100;
    }

    protected AdapterOperationResult performSend(Object payload) throws Exception {
        String endpoint = config.getTargetEndpointUrl();

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(endpoint))
                    .timeout(Duration.ofMillis(config.getReadTimeout()));

            // Set HTTP method and body
            String method = config.getHttpMethod().name().toUpperCase();
            HttpRequest.BodyPublisher bodyPublisher = createBodyPublisher(payload);

            switch(method) {
                case "POST":
                    requestBuilder.POST(bodyPublisher);
                    break;
                case "PUT":
                    requestBuilder.PUT(bodyPublisher);
                    break;
                case "PATCH":
                    requestBuilder.method("PATCH", bodyPublisher);
                    break;
                case "DELETE":
                    if(bodyPublisher == HttpRequest.BodyPublishers.noBody()) {
                        requestBuilder.DELETE();
                    } else {
                        requestBuilder.method("DELETE", bodyPublisher);
                    }
                    break;
                default:
                    throw new AdapterException(
                            "Unsupported HTTP method for receiver: " + method);
            }

            // Add headers
            addRequestHeaders(requestBuilder);

            // Send request
            HttpRequest request = requestBuilder.build();
            log.debug("Sending HTTP {} request to: {}", method, maskSensitiveUrl(endpoint));

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return processSendResponse(response);
        } catch(Exception e) {
            log.error("HTTP send operation failed", e);
            if(e instanceof java.net.ConnectException) {
                throw new AdapterException("Connection failed", e);
            } else if(e instanceof java.net.SocketTimeoutException) {
                throw new AdapterException("Request timeout: " + e.getMessage(), e);
            } else if(e instanceof javax.net.ssl.SSLException) {
                throw new AdapterException("SSL error", e);
            }
            throw new AdapterException("HTTP request failed: " + e.getMessage(), e);
        }
    }

    private void initializeHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.getConnectionTimeout()))
                .followRedirects(config.isFollowRedirects() ?
                        HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER);

        // Configure proxy if needed
        if(config.getProxyServer() != null && !config.getProxyServer().isEmpty()) {
            // Proxy configuration would go here
        }

        httpClient = builder.build();
    }

    private HttpRequest.BodyPublisher createBodyPublisher(Object payload) throws Exception {
        if(payload == null) {
            return HttpRequest.BodyPublishers.noBody();
        }

        String contentType = config.getContentType().toLowerCase();

        if(payload instanceof byte[]) {
            return HttpRequest.BodyPublishers.ofByteArray((byte[]) payload);
        } else if(payload instanceof String) {
            return HttpRequest.BodyPublishers.ofString((String) payload);
        } else if(contentType.contains("json")) {
            // Convert to JSON string(simplified - should use proper JSON library)
            String jsonString = convertToJson(payload);
            return HttpRequest.BodyPublishers.ofString(jsonString);
        } else if(contentType.contains("xml")) {
            // Convert to XML string(simplified - should use proper XML library)
            String xmlString = convertToXml(payload);
            return HttpRequest.BodyPublishers.ofString(xmlString);
        } else if(contentType.contains("form")) {
            // Convert to form data
            String formData = convertToFormData(payload);
            return HttpRequest.BodyPublishers.ofString(formData);
        } else {
            // Default to string representation
            return HttpRequest.BodyPublishers.ofString(payload.toString());
        }
    }

    private void addRequestHeaders(HttpRequest.Builder builder) throws Exception {
        // Content - Type header
        builder.header("Content - Type", config.getContentType());

        // Accept header
        if(config.getExpectedResponseFormat() != null) {
            builder.header("Accept", config.getExpectedResponseFormat());
        }
        // Common headers
        addCommonHeaders(builder);

        // Authentication headers
        addAuthenticationHeaders(builder);

        // Custom headers
        // Parse custom headers from string format
        if(config.getRequestHeaders() != null && !config.getRequestHeaders().trim().isEmpty()) {
            String[] headers = config.getRequestHeaders().split(",");
            for(String header : headers) {
                String[] parts = header.split(":", 2);
                if(parts.length == 2) {
                    builder.header(parts[0].trim(), parts[1].trim());
                }
            }
        }
    }

    private void addCommonHeaders(HttpRequest.Builder builder) {
        builder.header("User - Agent", "Integrix - HTTP - Adapter/1.0");

        if(true) {
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

    private AdapterOperationResult processSendResponse(HttpResponse<String> response) throws Exception {
        int statusCode = response.statusCode();
        String responseBody = response.body();

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("statusCode", statusCode);
        responseData.put("headers", response.headers().map());
        responseData.put("body", responseBody);

        if(statusCode >= 200 && statusCode < 300) {
            log.debug("HTTP request successful with status: {}", statusCode);
            return AdapterOperationResult.success("HTTP request successful");
        } else if(statusCode >= 300 && statusCode < 400) {
            // Handle redirects
            String location = response.headers().firstValue("Location").orElse("");
            log.info("HTTP redirect response: {} to {}", statusCode, location);
            return AdapterOperationResult.success("HTTP redirect: " + statusCode);
        } else if(statusCode >= 400 && statusCode < 500) {
            log.warn("HTTP client error: {} - {}", statusCode, responseBody);
            return AdapterOperationResult.failure("HTTP client error: " + statusCode);
        } else {
            log.error("HTTP server error: {} - {}", statusCode, responseBody);
            return AdapterOperationResult.failure("HTTP server error: " + statusCode);
        }
    }

    private String getEffectiveEndpoint() {
        return config.getTargetEndpointUrl();
    }

    private String maskSensitiveUrl(String url) {
        if(url == null) return null;
        // Mask any credentials in URL
        return url.replaceAll("://[^:] + :[^@] + @", "://***:***@");
    }

    private String buildQueryString(Object criteria) throws Exception {
        if(criteria instanceof Map) {
            Map<?, ?> params = (Map<?, ?>) criteria;
            StringBuilder query = new StringBuilder();
            for(Map.Entry<?, ?> entry : params.entrySet()) {
                if(query.length() > 0) query.append("&");
                query.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8))
                        .append(" = ")
                        .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            }
            return query.toString();
        }
        return "";
    }

    private String convertToJson(Object payload) {
        // Simplified JSON conversion - should use proper JSON library
        if(payload instanceof Map || payload instanceof Collection) {
            return payload.toString(); // Very simplified
        }
        return "\"" + payload.toString() + "\"";
    }

    private String convertToXml(Object payload) {
        // Simplified XML conversion - should use proper XML library
        return "<data>" + payload.toString() + "</data>";
    }

    private String convertToFormData(Object payload) throws Exception {
        if(payload instanceof Map) {
            return buildQueryString(payload);
        }
        return URLEncoder.encode(payload.toString(), StandardCharsets.UTF_8);
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getTargetEndpointUrl() == null || config.getTargetEndpointUrl().trim().isEmpty()) {
            throw new AdapterException("Target URL is required", null);
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


    protected long getPollingIntervalMs() {
        // HTTP receivers typically don't poll
        return 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("HTTP Receiver(Outbound): %s %s, Auth: %s, Timeout: %dms",
                config.getHttpMethod().name(),
                maskSensitiveUrl(config.getTargetEndpointUrl()),
                config.getAuthenticationType() != null ? config.getAuthenticationType().name() : "NONE",
                config.getReadTimeout());
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.HTTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("HTTP Outbound adapter implementation")
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
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }




}
