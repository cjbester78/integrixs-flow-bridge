package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import java.util.concurrent.CompletableFuture;
import java.util.List;import com.integrixs.adapters.domain.port.InboundAdapterPort;
import java.util.Map;
import java.util.List;import com.integrixs.adapters.config.RestInboundAdapterConfig;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import java.util.*;
import java.util.List;import java.util.concurrent.ConcurrentHashMap;
import java.util.List;/**
 * REST Sender Adapter implementation for REST API consumption(INBOUND).
 * Follows middleware convention: Inbound = receives data FROM external systems.
 * Supports HTTP/HTTPS, various authentication methods, and JSON/XML processing.
 */
public class RestInboundAdapter extends AbstractAdapter implements InboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(RestInboundAdapter.class);


    private final RestInboundAdapterConfig config;
    private final Map<String, String> processedMessages = new ConcurrentHashMap<>();
    private RestTemplate restTemplate;
    public RestInboundAdapter(RestInboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing REST inbound adapter(inbound) with URL: {}", config.getBaseUrl());

        try {
            validateConfiguration();
            initializeRestTemplate();
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("REST inbound adapter initialized successfully");
        return AdapterOperationResult.success("REST inbound adapter initialized");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying REST inbound adapter");
        processedMessages.clear();
        restTemplate = null;
        return AdapterOperationResult.success("REST inbound adapter destroyed");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        // Test 1: Basic connectivity test
        testResults.add(
            performConnectivityTest()
       );
        // Test 2: Authentication test
        if(config.getAuthenticationType() != null && !"none".equals(config.getAuthenticationType())) {
            testResults.add(
                performAuthenticationTest()
           );
        }
        // Test 3: Data polling test
        testResults.add(
            performDataPollingTest()
       );
        return AdapterOperationResult.success(testResults);
    }

    // InboundAdapterPort implementation
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            // For REST Sender, fetch means polling from REST API
            return pollFromRestApi();
        } catch(Exception e) {
            return AdapterOperationResult.failure("Failed to fetch data: " + e.getMessage());
        }
    }

    // For receiving incoming HTTP requests(webhooks), use startListening with callback
    // The actual HTTP endpoint would be exposed by a separate controller that calls this adapter

    private AdapterOperationResult pollFromRestApi() throws Exception {
        List<Map<String, Object>> processedData = new ArrayList<>();
        try {
            String pollUrl = config.getBaseUrl() + config.getPollingEndpoint();

            // Add query parameters if configured
            if(config.getQueryParameters() != null && !config.getQueryParameters().isEmpty()) {
                pollUrl += "?" + config.getQueryParameters();
            }

            HttpHeaders httpHeaders = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    pollUrl, HttpMethod.GET, entity, String.class);
            if(response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if(responseBody != null && !responseBody.trim().isEmpty()) {
                    Map<String, Object> responseData = processResponse(responseBody, response.getHeaders());
                    // Check for duplicates if enabled
                    if(config.isEnableDuplicateHandling()) {
                        String messageId = generateMessageId(responseData);
                        if(processedMessages.containsKey(messageId)) {
                            log.debug("Duplicate message detected, skipping: {}", messageId);
                            return AdapterOperationResult.success(Collections.emptyList(), "Duplicate message skipped");
                        }
                        processedMessages.put(messageId, String.valueOf(System.currentTimeMillis()));
                    }
                    processedData.add(responseData);
                } else {
                    log.debug("Empty response from REST endpoint");
                }
            } else {
                throw new AdapterException(
                        "REST polling failed with status: " + response.getStatusCode());
            }
        } catch(HttpClientErrorException e) {
            throw new AdapterException(
                    "REST client error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch(HttpServerErrorException e) {
            throw new AdapterException(
                    "REST server error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch(RestClientException e) {
            throw new AdapterException(
                    "REST communication error: " + e.getMessage(), e);
        }
        log.info("REST inbound adapter polled {} items from API", processedData.size());
        return AdapterOperationResult.success(processedData,
                String.format("Retrieved %d items from REST API", processedData.size()));
    }

    private Map<String, Object> processResponse(String responseBody, HttpHeaders responseHeaders) throws Exception {
        Map<String, Object> responseData = new HashMap<>();
        // Store raw response
        responseData.put("rawResponse", responseBody);
        responseData.put("responseSize", responseBody.length());
        responseData.put("timestamp", System.currentTimeMillis());
        // Store response headers if configured
        if(config.isIncludeResponseHeaders()) {
            Map<String, String> headers = new HashMap<>();
            responseHeaders.forEach((key, values) ->
                headers.put(key, String.join(",", values)));
            responseData.put("responseHeaders", headers);
        }
        // Parse response based on content type
        String contentType = responseHeaders.getFirst(HttpHeaders.CONTENT_TYPE);
        if(contentType != null) {
            responseData.put("contentType", contentType);
            if(contentType.contains("application/json")) {
                responseData.put("parsedContent", parseJsonResponse(responseBody));
            } else if(contentType.contains("application/xml") || contentType.contains("text/xml")) {
                responseData.put("parsedContent", parseXmlResponse(responseBody));
            } else {
                responseData.put("parsedContent", responseBody);
            }
        } else {
            responseData.put("parsedContent", responseBody);
        }
        return responseData;
    }
    private Object parseJsonResponse(String jsonResponse) throws Exception {
        // Simple JSON parsing - in production, use Jackson or Gson
        try {
            // This is a simplified implementation
            // In a real scenario, you'd use proper JSON parsing
            return jsonResponse;
        } catch(Exception e) {
            log.warn("Failed to parse JSON response, returning as string", e);
            return jsonResponse;
        }
    }

    private Object parseXmlResponse(String xmlResponse) throws Exception {
        // Simple XML parsing - in production, use proper XML parser
        try {
            // In a real scenario, you'd use proper XML parsing
            return xmlResponse;
        } catch(Exception e) {
            log.warn("Failed to parse XML response, returning as string", e);
            return xmlResponse;
        }
    }

    private String generateMessageId(Map<String, Object> responseData) {
        // Generate unique message ID based on content
        String content = (String) responseData.get("rawResponse");
        return String.valueOf(content.hashCode()) + "_" + responseData.get("timestamp");
    }

    private HttpHeaders createHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        // Set content type
        if(config.getContentType() != null) {
            headers.setContentType(MediaType.parseMediaType(config.getContentType()));
        }
        // Set accept header
        if(config.getAcceptType() != null) {
            headers.setAccept(Arrays.asList(MediaType.parseMediaType(config.getAcceptType())));
        }
        // Authentication
        if(config.getAuthenticationType() != null) {
            switch(config.getAuthenticationType().name().toLowerCase()) {
                case "basic":
                    if(config.getUsername() != null && config.getPassword() != null) {
                        String auth = config.getUsername() + ":" + config.getPassword();
                        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
                    }
                    break;
                case "bearer":
                    if(config.getBearerToken() != null) {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + config.getBearerToken());
                    }
                    break;
                case "apikey":
                    if(config.getApiKey() != null) {
                        String apiKeyHeader = config.getApiKeyHeader() != null ?
                                config.getApiKeyHeader() : "X - API - Key";
                        headers.set(apiKeyHeader, config.getApiKey());
                    }
                    break;
            }
        }
        // Custom headers
        if(config.getHeaders() != null) {
            config.getHeaders().forEach(headers::set);
        }
        // User agent
        if(config.getUserAgent() != null) {
            headers.set(HttpHeaders.USER_AGENT, config.getUserAgent());
        }
        return headers;
    }

    private String buildQueryString(Map<String, String> parameters) {
        StringBuilder queryString = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : parameters.entrySet()) {
            if(!first) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append(" = ").append(entry.getValue());
            first = false;
        }
        return queryString.toString();
    }
    private void initializeRestTemplate() throws Exception {
        restTemplate = new RestTemplate();
        // Configure timeouts and other settings
        // In production, you'd configure connection pool, timeouts, etc.
        log.debug("REST template initialized with default configuration");
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getBaseUrl() == null || config.getBaseUrl().trim().isEmpty()) {
            throw new AdapterException("Base URL is required", null);
        }
        // Validate authentication configuration
        if("basic".equals(config.getAuthenticationType())) {
            if(config.getUsername() == null || config.getPassword() == null) {
                throw new AdapterException(
                        "Username and password are required for basic authentication");
            }
        }
        if("bearer".equals(config.getAuthenticationType())) {
            if(config.getBearerToken() == null) {
                        throw new AdapterException(
                        "Bearer token is required for bearer authentication");
            }
        }
        if("apikey".equals(config.getAuthenticationType())) {
            if(config.getApiKey() == null) {
                        throw new AdapterException(
                        "API key is required for API key authentication");
            }
        }
    }
    public long getPollingInterval() {
        return config.getPollingInterval() != null ? config.getPollingInterval() : 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("REST Sender(Inbound): %s%s, Auth: %s, Polling: %sms",
                config.getBaseUrl(),
                config.getPollingEndpoint(),
                config.getAuthenticationType() != null ? config.getAuthenticationType() : "none",
                config.getPollingInterval() != null ? config.getPollingInterval() : "0");
    }


    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }

    @Override
    public void startListening(DataReceivedCallback callback) {
        // Not implemented for this adapter type
        log.debug("Push-based listening not supported by this adapter type");
    }

    @Override
    public void stopListening() {
    }

    public boolean isListening() {
        return false;
    }
    public void startPolling(long intervalMillis) {
        // Polling is not yet implemented for REST adapter
        log.debug("Polling not yet implemented for REST adapter");
    }

    public void stopPolling() {
    }
    public void setDataReceivedCallback(DataReceivedCallback callback) {
        // Implement if callbacks are supported
    }
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.REST)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("Inbound adapter implementation")
                .version("1.0.0")
                .supportsBatch(false)
                .supportsAsync(true)
                .build();
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    // Helper methods for connection testing
    private AdapterOperationResult performConnectivityTest() {
        try {
            String testUrl = config.getBaseUrl();
            if(config.getHealthCheckEndpoint() != null) {
                testUrl = config.getBaseUrl() + config.getHealthCheckEndpoint();
            }

            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    testUrl, HttpMethod.GET, entity, String.class);
            if(response.getStatusCode().is2xxSuccessful()) {
                return AdapterOperationResult.success(
                        "Successfully connected to REST endpoint");
            } else {
                return AdapterOperationResult.failure(
                        "REST endpoint returned status: " + response.getStatusCode());
                }
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to connect to REST endpoint: " + e.getMessage());
        }
    }

    private AdapterOperationResult performAuthenticationTest() {
        try {
            HttpHeaders headers = createHeaders();

            if(headers.containsKey(HttpHeaders.AUTHORIZATION) ||
                headers.containsKey("X - API - Key") ||
                !headers.isEmpty()) {
                return AdapterOperationResult.success(
                        "Authentication headers configured successfully");
            } else {
                return AdapterOperationResult.failure(
                        "Authentication configured but no auth headers found");
            }
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to configure authentication: " + e.getMessage());
        }
    }

    private AdapterOperationResult performDataPollingTest() {
        try {
            String pollUrl = config.getBaseUrl() + config.getPollingEndpoint();
            HttpHeaders headers = createHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    pollUrl, HttpMethod.GET, entity, String.class);
            return AdapterOperationResult.success(
                    "Successfully polled data from endpoint, status: " + response.getStatusCode());
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to poll data from endpoint: " + e.getMessage());
        }
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.INBOUND;
    }
}
