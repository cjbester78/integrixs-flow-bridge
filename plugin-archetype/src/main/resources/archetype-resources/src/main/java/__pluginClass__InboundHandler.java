package ${package};

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Inbound handler for ${pluginName}
 */
@Slf4j
public class ${pluginClass}InboundHandler implements InboundHandler {

    private final Map<String, Object> configuration;
    private ScheduledExecutorService scheduler;
    private MessageCallback callback;
    private boolean listening = false;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicLong messageCounter = new AtomicLong(0);
    private final Queue<String> processedIds = new ConcurrentLinkedQueue<>();
    private static final int MAX_PROCESSED_IDS = 1000;

    public ${pluginClass}InboundHandler(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    @Override
    public void startListening(MessageCallback callback) throws PluginException {
        if (listening) {
            throw new PluginException("Already listening");
        }

        this.callback = callback;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();

        // Configure polling interval from configuration
        int pollingInterval = Integer.parseInt(
            configuration.getOrDefault("pollingInterval", "60").toString()
       );

        scheduler.scheduleWithFixedDelay(
            this::pollForData,
            0,
            pollingInterval,
            TimeUnit.SECONDS
       );

        listening = true;
        log.info("Started listening for data (polling every {} seconds)", pollingInterval);
    }

    @Override
    public void stopListening() {
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        listening = false;
        log.info("Stopped listening for data");
    }

    @Override
    public PollingResult poll() {
        // Manual polling implementation
        List<PluginMessage> messages = fetchData();
        return PollingResult.builder()
                .messages(messages)
                .hasMore(false)
                .build();
    }

    @Override
    public boolean isListening() {
        return listening;
    }

    private void pollForData() {
        try {
            List<PluginMessage> messages = fetchData();
            for (PluginMessage message : messages) {
                callback.onMessage(message);
            }
        } catch (Exception e) {
            log.error("Error polling for data", e);
            callback.onError(e);
        }
    }

    private List<PluginMessage> fetchData() {
        List<PluginMessage> messages = new ArrayList<>();

        try {
            String endpoint = (String) configuration.get("endpoint");
            String authType = (String) configuration.get("authType");
            int batchSize = Integer.parseInt(
                configuration.getOrDefault("batchSize", "100").toString()
           );
            int timeout = Integer.parseInt(
                configuration.getOrDefault("timeout", "30").toString()
           ) * 1000;

            // Build URL with parameters for polling
            URL url = new URL(endpoint + "?limit=" + batchSize + "&since=" + getLastPollTime());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configure connection
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);
            conn.setRequestProperty("Accept", "application/json");

            // Add authentication
            addAuthenticationHeaders(conn, authType);

            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
               );
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse response
                messages = parseResponse(response.toString());

                // Filter out already processed messages
                messages = filterProcessedMessages(messages);

                log.debug("Fetched {} new messages from {}", messages.size(), endpoint);
            } else {
                log.warn("Failed to fetch data from {}, response code: {}", endpoint, responseCode);
            }

            conn.disconnect();

        } catch (Exception e) {
            log.error("Error fetching data", e);
            // Don't throw exception, just return empty list to continue polling
        }

        return messages;
    }

    private List<PluginMessage> parseResponse(String jsonResponse) {
        List<PluginMessage> messages = new ArrayList<>();

        try {
            // Try to parse as array first
            List<Map<String, Object>> dataList;

            try {
                dataList = objectMapper.readValue(jsonResponse, List.class);
            } catch (Exception e) {
                // If not array, try to parse as object with data field
                Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);
                Object data = responseMap.get("data");
                if (data instanceof List) {
                    dataList = (List<Map<String, Object>>) data;
                } else {
                    // Single object response
                    dataList = List.of(responseMap);
                }
            }

            // Convert each item to PluginMessage
            for (Map<String, Object> item : dataList) {
                String id = extractId(item);

                PluginMessage message = PluginMessage.builder()
                        .id(id)
                        .headers(Map.of(
                            "source", "${pluginId}",
                            "sourceMessageId", id,
                            "receivedAt", Instant.now().toString(),
                            "messageNumber", messageCounter.incrementAndGet()
                       ))
                        .body(item)
                        .contentType("application/json")
                        .timestamp(System.currentTimeMillis())
                        .build();

                messages.add(message);
            }

        } catch (Exception e) {
            log.error("Failed to parse response: {}", jsonResponse, e);
        }

        return messages;
    }

    private String extractId(Map<String, Object> item) {
        // Try common ID fields
        Object id = item.get("id");
        if (id == null) id = item.get("_id");
        if (id == null) id = item.get("messageId");
        if (id == null) id = item.get("uuid");

        // Generate ID if none found
        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        return id.toString();
    }

    private List<PluginMessage> filterProcessedMessages(List<PluginMessage> messages) {
        List<PluginMessage> filtered = new ArrayList<>();

        for (PluginMessage message : messages) {
            String id = message.getId();
            if (!processedIds.contains(id)) {
                filtered.add(message);
                processedIds.offer(id);

                // Maintain queue size limit
                while (processedIds.size() > MAX_PROCESSED_IDS) {
                    processedIds.poll();
                }
            }
        }

        return filtered;
    }

    private String getLastPollTime() {
        // In a real implementation, this would track the last successful poll time
        // For now, return current time minus polling interval
        int pollingInterval = Integer.parseInt(
            configuration.getOrDefault("pollingInterval", "60").toString()
       );

        return Instant.now().minusSeconds(pollingInterval * 2).toString();
    }

    private void addAuthenticationHeaders(HttpURLConnection conn, String authType) {
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
}