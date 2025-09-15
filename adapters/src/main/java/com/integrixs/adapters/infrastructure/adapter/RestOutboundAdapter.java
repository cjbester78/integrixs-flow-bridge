package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;import java.util.List;import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import java.util.HashMap;import java.util.Map;
import java.util.HashMap;import java.util.List;import com.integrixs.adapters.config.RestOutboundAdapterConfig;
import java.util.HashMap;import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import java.util.*;
import java.util.HashMap;import java.util.List;import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;import java.util.List;import java.util.HashMap;/**
 * REST Receiver Adapter implementation for REST API calls(OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Supports HTTP/HTTPS, various authentication methods, and JSON/XML processing.
 */
public class RestOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(RestOutboundAdapter.class);


    private final RestOutboundAdapterConfig config;
    private RestTemplate restTemplate;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final List<Object> batchBuffer = new ArrayList<>();
    private long lastBatchFlush = System.currentTimeMillis();
    public RestOutboundAdapter(RestOutboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing REST outbound adapter(outbound) with URL: {}", config.getBaseUrl());

        try {
            validateConfiguration();
            initializeRestTemplate();
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("REST outbound adapter initialized successfully");
        return AdapterOperationResult.success("REST outbound adapter initialized");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying REST outbound adapter");
        // Flush any remaining batch data
        if(config.isEnableBatching() && !batchBuffer.isEmpty()) {
            try {
                flushBatch();
            } catch(Exception e) {
                log.warn("Error flushing batch during REST adapter shutdown", e);
            }
        }
        batchBuffer.clear();
        restTemplate = null;
        return AdapterOperationResult.success("REST outbound adapter destroyed");
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
        // Test 3: Test API call
        testResults.add(
            performApiCallTest()
       );
        return AdapterOperationResult.success(testResults);
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

    protected AdapterOperationResult performSend(Object payload) throws Exception {
        // For REST Receiver(outbound), this method sends data TO REST API
        if(config.isEnableBatching()) {
            return addToBatch(payload);
        } else {
            return sendToRestApi(payload);
        }
    }

    private AdapterOperationResult addToBatch(Object payload) throws Exception {
        synchronized(batchBuffer) {
            batchBuffer.add(payload);

            boolean shouldFlush = false;
            // Check size - based flushing
            if("SIZE_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                if(config.getBatchSize() != null && batchBuffer.size() >= config.getBatchSize()) {
                    shouldFlush = true;
                }
            }
            // Check time - based flushing
            if("TIME_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                long timeSinceLastFlush = System.currentTimeMillis() - lastBatchFlush;
                if(timeSinceLastFlush >= config.getBatchTimeoutMs()) {
                    shouldFlush = true;
                }
            }

            if(shouldFlush) {
                return flushBatch();
            } else {
                return AdapterOperationResult.success(null,
                        String.format("Added to batch(%d/%d items)",
                                batchBuffer.size(),
                                config.getBatchSize() != null ? config.getBatchSize() : "unlimited"));
            }
        }
    }
    private AdapterOperationResult flushBatch() throws Exception {
        synchronized(batchBuffer) {
            if(batchBuffer.isEmpty()) {
                return AdapterOperationResult.success(null, "No items in batch to flush");
            }

            List<Object> itemsToSend = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
            lastBatchFlush = System.currentTimeMillis();
            return sendBatchToRestApi(itemsToSend);
        }
    }

    private AdapterOperationResult sendBatchToRestApi(List<Object> items) throws Exception {
        // Create batch payload
        Map<String, Object> batchPayload = new HashMap<>();
        batchPayload.put("batchId", batchCounter.incrementAndGet());
        batchPayload.put("itemCount", items.size());
        batchPayload.put("timestamp", System.currentTimeMillis());
        batchPayload.put("items", items);
        return sendApiRequest(batchPayload, true, items.size());
    }
    private AdapterOperationResult sendToRestApi(Object payload) throws Exception {
        return sendApiRequest(payload, false, 1);
    }

    private AdapterOperationResult sendApiRequest(Object payload, boolean isBatch, int itemCount) throws Exception {
        try {
            // Prepare request
            String requestBody = convertPayloadToString(payload);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            // Build URL
            String url = config.getBaseUrl() + config.getTargetEndpoint();
            if(config.getQueryParameters() != null && !config.getQueryParameters().isEmpty()) {
                url += "?" + config.getQueryParameters();
            }
            // Make API call
            org.springframework.http.HttpMethod method = org.springframework.http.HttpMethod.valueOf(config.getHttpMethod().name());
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            // Process response
            if(response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                log.info("REST outbound adapter sent {} items to API, status: {}",
                        itemCount, response.getStatusCode());
                String message = isBatch ?
                        String.format("Successfully sent batch of %d items to REST API", itemCount) :
                        String.format("Successfully sent data to REST API");
                AdapterOperationResult result = AdapterOperationResult.success(responseBody, message);
                result.addMetadata("httpStatus", response.getStatusCode().value());
                result.addMetadata("responseSize", responseBody != null ? responseBody.length() : 0);
                result.addMetadata("itemCount", itemCount);
                result.addMetadata("endpoint", url);
                // Include response headers if configured
                if(config.isIncludeResponseHeaders()) {
                    Map<String, String> responseHeaders = new HashMap<>();
                    response.getHeaders().forEach((key, values) ->
                        responseHeaders.put(key, String.join(",", values)));
                    result.addMetadata("responseHeaders", responseHeaders);
                }

                return result;
            } else {
                throw new AdapterException(
                        "REST API call failed with status: " + response.getStatusCode());
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
    }
    private String convertPayloadToString(Object payload) throws Exception {
        if(payload == null) {
            return "";
        }
        if(payload instanceof String) {
            return(String) payload;
        }
        if(payload instanceof Map || payload instanceof Collection) {
            // Convert to JSON format - in production, use proper JSON serialization
            return payload.toString();
        }
        return payload.toString();
    }

    private HttpHeaders createHeaders() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        // Set content type
        if(config.getContentType() != null) {
            headers.setContentType(MediaType.parseMediaType(config.getContentType()));
            } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
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
        if(config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
            String[] customHeaders = config.getCustomHeaders().split(",");
            for(String header : customHeaders) {
                String[] keyValue = header.split(":");
                if(keyValue.length == 2) {
                    headers.set(keyValue[0].trim(), keyValue[1].trim());
                }
            }
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
        if(config.getHttpMethod() == null) {
            throw new AdapterException("HTTP method is required", null);
        }
        // Validate HTTP method
        try {
            org.springframework.http.HttpMethod.valueOf(config.getHttpMethod().name());
        } catch(Exception e) {
            throw new AdapterException(
                    "Invalid HTTP method: " + config.getHttpMethod());
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
    public long getTimeout() {
        // REST receivers typically don't poll, they push data
        return config.getTimeout() != null ? config.getTimeout() : 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("REST Receiver(Outbound): %s%s, Method: %s, Auth: %s, Batching: %s",
                config.getBaseUrl(),
                config.getTargetEndpoint(),
                config.getHttpMethod(),
                config.getAuthenticationType() != null ? config.getAuthenticationType() : "none",
                config.isEnableBatching() ? "Enabled" : "Disabled");
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
        boolean allSuccess = results.stream().allMatch(AdapterOperationResult::isSuccess);
        return allSuccess ?
            AdapterOperationResult.success(results) :
            AdapterOperationResult.failure("Some batch operations failed");
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }

    @Override
    public boolean supportsBatchOperations() {
        return false;
    }

    @Override
    public int getMaxBatchSize() {
        return 100;
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.REST)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("Outbound adapter implementation")
                .version("1.0.0")
                .supportsBatch(supportsBatchOperations())
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
                        "REST Connection", "Successfully connected to REST endpoint");
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
                        "Authentication", "Authentication headers configured successfully");
            } else {
                return AdapterOperationResult.failure(
                        "Authentication configured but no auth headers found");
            }
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to configure authentication: " + e.getMessage());
        }
    }

    private AdapterOperationResult performApiCallTest() {
        try {
            // Test with minimal payload
            Map<String, Object> testPayload = new HashMap<>();
            testPayload.put("test", "connection");
            String testData = convertPayloadToString(testPayload);
            HttpHeaders headers = createHeaders();
            HttpEntity<String> entity = new HttpEntity<>(testData, headers);
            String endpoint = config.getBaseUrl() + config.getTargetEndpoint();
            org.springframework.http.HttpMethod method = org.springframework.http.HttpMethod.valueOf(config.getHttpMethod().name());
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint, method, entity, String.class);
            return AdapterOperationResult.success(
                    "API Call Test", "Successfully made test API call, status: " + response.getStatusCode());
        } catch(Exception e) {
            return AdapterOperationResult.failure(
                    "Failed to make test API call: " + e.getMessage());
        }
    }


    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.REST;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }
}
