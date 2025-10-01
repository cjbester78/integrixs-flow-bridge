package ${package};

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Outbound handler for ${pluginName}
 */
@Slf4j
public class ${pluginClass}OutboundHandler implements OutboundHandler {

    private final Map<String, Object> configuration;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);
    private final Semaphore rateLimiter;

    public ${pluginClass}OutboundHandler(Map<String, Object> configuration) {
        this.configuration = configuration;

        // Initialize rate limiter based on configuration
        int rateLimit = Integer.parseInt(
            configuration.getOrDefault("rateLimit", "100").toString()
       );
        this.rateLimiter = new Semaphore(rateLimit);

        // Start rate limit reset scheduler
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            rateLimiter.release(rateLimit - rateLimiter.availablePermits());
        }, 1, 1, TimeUnit.MINUTES);
    }

    @Override
    public SendResult send(PluginMessage message) throws PluginException {
        try {
            log.debug("Sending message: {}", message.getId());

            // Acquire rate limit permit
            if (!rateLimiter.tryAcquire(5, TimeUnit.SECONDS)) {
                throw new PluginException("Rate limit exceeded, please retry later");
            }

            // Prepare message data
            Map<String, Object> payload = preparePayload(message);

            // Send to external system with retry logic
            SendResponse response = sendWithRetry(payload, message.getId());

            return SendResult.builder()
                    .successful(response.isSuccess())
                    .messageId(message.getId())
                    .externalMessageId(response.getExternalId())
                    .response(response.getMessage())
                    .metadata(Map.of(
                        "statusCode", response.getStatusCode(),
                        "responseTime", response.getResponseTime()
                   ))
                    .build();

        } catch (Exception e) {
            log.error("Failed to send message", e);
            return SendResult.failure(message.getId(), e.getMessage());
        }
    }

    @Override
    public BatchSendResult sendBatch(List<PluginMessage> messages) throws PluginException {
        if (supportsBatch()) {
            // Send as actual batch
            return sendBatchOptimized(messages);
        } else {
            // Send messages in parallel with thread pool
            List<CompletableFuture<SendResult>> futures = messages.stream()
                    .map(msg -> CompletableFuture.supplyAsync(
                        () -> send(msg), executor
                   ))
                    .collect(Collectors.toList());

            // Wait for all to complete
            List<SendResult> results = futures.stream()
                    .map(future -> {
                        try {
                            return future.get(30, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            return SendResult.failure(
                                "unknown",
                                "Timeout or error: " + e.getMessage()
                           );
                        }
                    })
                    .collect(Collectors.toList());

            long successCount = results.stream()
                    .filter(SendResult::isSuccessful)
                    .count();

            return BatchSendResult.builder()
                    .totalMessages(messages.size())
                    .successCount((int) successCount)
                    .failureCount(messages.size() - (int) successCount)
                    .results(results)
                    .build();
        }
    }

    @Override
    public boolean supportsBatch() {
        // Check configuration for batch support
        return Boolean.parseBoolean(
            configuration.getOrDefault("supportsBatch", "false").toString()
       );
    }

    private Map<String, Object> preparePayload(PluginMessage message) {
        Map<String, Object> payload = new HashMap<>();

        // Add message body
        payload.put("data", message.getBody());

        // Add metadata
        payload.put("metadata", Map.of(
            "sourceId", message.getId(),
            "timestamp", message.getTimestamp(),
            "source", "${pluginId}"
       ));

        // Add any custom headers from message
        if (message.getHeaders() != null) {
            payload.put("headers", message.getHeaders());
        }

        return payload;
    }

    private SendResponse sendWithRetry(Map<String, Object> payload, String messageId) throws PluginException {
        int maxRetries = Integer.parseInt(
            configuration.getOrDefault("retryAttempts", "3").toString()
       );

        Exception lastException = null;

        for (int i = 0; i <= maxRetries; i++) {
            try {
                return doSend(payload, messageId);
            } catch (Exception e) {
                lastException = e;
                log.warn("Send attempt {} failed for message {}: {}",
                    i + 1, messageId, e.getMessage());

                if (i < maxRetries) {
                    // Exponential backoff
                    try {
                        Thread.sleep((long) Math.pow(2, i) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new PluginException("Send interrupted", ie);
                    }
                }
            }
        }

        throw new PluginException("Failed to send after " + (maxRetries + 1) + " attempts", lastException);
    }

    private SendResponse doSend(Map<String, Object> payload, String messageId) throws Exception {
        String endpoint = (String) configuration.get("endpoint");
        int timeout = Integer.parseInt(
            configuration.getOrDefault("timeout", "30").toString()
       ) * 1000;

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            // Configure connection
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // Add authentication
            addAuthenticationHeaders(conn);

            // Add custom headers
            addCustomHeaders(conn, messageId);

            // Write payload
            String jsonPayload = objectMapper.writeValueAsString(payload);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            // Get response
            long startTime = System.currentTimeMillis();
            int responseCode = conn.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;

            // Read response
            String response = readResponse(conn);

            // Parse response for external ID
            String externalId = extractExternalId(response);

            if (responseCode >= 200 && responseCode < 300) {
                log.info("Successfully sent message {} to {}, external ID: {}",
                    messageId, endpoint, externalId);

                return new SendResponse(
                    true,
                    externalId,
                    "Message sent successfully",
                    responseCode,
                    responseTime
               );
            } else {
                return new SendResponse(
                    false,
                    null,
                    "Server returned status " + responseCode + ": " + response,
                    responseCode,
                    responseTime
               );
            }

        } finally {
            conn.disconnect();
        }
    }

    private BatchSendResult sendBatchOptimized(List<PluginMessage> messages) {
        try {
            // Prepare batch payload
            List<Map<String, Object>> batchPayload = messages.stream()
                    .map(this::preparePayload)
                    .collect(Collectors.toList());

            String endpoint = (String) configuration.get("endpoint") + "/batch";
            int timeout = Integer.parseInt(
                configuration.getOrDefault("timeout", "30").toString()
           ) * 1000;

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            try {
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(timeout);
                conn.setReadTimeout(timeout);
                conn.setRequestProperty("Content-Type", "application/json");

                addAuthenticationHeaders(conn);

                String jsonPayload = objectMapper.writeValueAsString(Map.of(
                    "batch", batchPayload,
                    "batchId", UUID.randomUUID().toString()
               ));

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                String response = readResponse(conn);

                if (responseCode >= 200 && responseCode < 300) {
                    // Parse batch response
                    Map<String, Object> batchResponse = objectMapper.readValue(response, Map.class);
                    List<Map<String, Object>> results = (List<Map<String, Object>>)
                        batchResponse.get("results");

                    List<SendResult> sendResults = new ArrayList<>();
                    AtomicInteger successCount = new AtomicInteger(0);

                    for (int i = 0; i < messages.size() && i < results.size(); i++) {
                        Map<String, Object> result = results.get(i);
                        boolean success = (Boolean) result.getOrDefault("success", false);

                        if (success) successCount.incrementAndGet();

                        sendResults.add(SendResult.builder()
                                .successful(success)
                                .messageId(messages.get(i).getId())
                                .externalMessageId((String) result.get("id"))
                                .response((String) result.get("message"))
                                .build());
                    }

                    return BatchSendResult.builder()
                            .totalMessages(messages.size())
                            .successCount(successCount.get())
                            .failureCount(messages.size() - successCount.get())
                            .results(sendResults)
                            .build();
                } else {
                    // Batch failed, return all as failed
                    return createFailedBatchResult(messages,
                        "Batch request failed with status " + responseCode);
                }

            } finally {
                conn.disconnect();
            }

        } catch (Exception e) {
            log.error("Batch send failed", e);
            return createFailedBatchResult(messages, e.getMessage());
        }
    }

    private BatchSendResult createFailedBatchResult(List<PluginMessage> messages, String reason) {
        List<SendResult> results = messages.stream()
                .map(msg -> SendResult.failure(msg.getId(), reason))
                .collect(Collectors.toList());

        return BatchSendResult.builder()
                .totalMessages(messages.size())
                .successCount(0)
                .failureCount(messages.size())
                .results(results)
                .build();
    }

    private void addAuthenticationHeaders(HttpURLConnection conn) {
        String authType = (String) configuration.get("authType");

        if ("apiKey".equals(authType)) {
            String apiKey = (String) configuration.get("apiKey");
            String apiKeyHeader = (String) configuration.getOrDefault("apiKeyHeader", "X-API-Key");
            conn.setRequestProperty(apiKeyHeader, apiKey);
        } else if ("basic".equals(authType)) {
            String username = (String) configuration.get("username");
            String password = (String) configuration.get("password");
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        } else if ("oauth2".equals(authType)) {
            String accessToken = (String) configuration.get("accessToken");
            if (accessToken != null) {
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
            }
        }
    }

    private void addCustomHeaders(HttpURLConnection conn, String messageId) {
        conn.setRequestProperty("X-Message-Id", messageId);
        conn.setRequestProperty("X-Plugin-Id", "${pluginId}");
        conn.setRequestProperty("X-Plugin-Version", "${version}");

        // Add any custom headers from configuration
        Map<String, String> customHeaders = (Map<String, String>)
            configuration.get("customHeaders");
        if (customHeaders != null) {
            customHeaders.forEach(conn::setRequestProperty);
        }
    }

    private String readResponse(HttpURLConnection conn) throws Exception {
        Scanner scanner;

        if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
            scanner = new Scanner(conn.getInputStream(), "UTF-8");
        } else {
            scanner = new Scanner(conn.getErrorStream(), "UTF-8");
        }

        scanner.useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";
        scanner.close();

        return response;
    }

    private String extractExternalId(String response) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);

            // Try common ID fields
            Object id = responseMap.get("id");
            if (id == null) id = responseMap.get("_id");
            if (id == null) id = responseMap.get("messageId");
            if (id == null) id = responseMap.get("externalId");

            return id != null ? id.toString() : UUID.randomUUID().toString();

        } catch (Exception e) {
            // If response is not JSON or doesn't contain ID, generate one
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Internal class for send response
     */
    private static class SendResponse {
        private final boolean success;
        private final String externalId;
        private final String message;
        private final int statusCode;
        private final long responseTime;

        public SendResponse(boolean success, String externalId, String message,
                          int statusCode, long responseTime) {
            this.success = success;
            this.externalId = externalId;
            this.message = message;
            this.statusCode = statusCode;
            this.responseTime = responseTime;
        }

        public boolean isSuccess() { return success; }
        public String getExternalId() { return externalId; }
        public String getMessage() { return message; }
        public int getStatusCode() { return statusCode; }
        public long getResponseTime() { return responseTime; }
    }
}