package com.integrixs.backend.plugin.example;

import com.integrixs.backend.plugin.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Example plugin that demonstrates how to create a custom adapter
 * This plugin connects to a weather API service
 */
public class WeatherApiPlugin implements AdapterPlugin {

    private static final Logger logger = LoggerFactory.getLogger(WeatherApiPlugin.class);

    private Map<String, Object> configuration;
    private HttpClient httpClient;
    private WeatherInboundHandler inboundHandler;
    private WeatherOutboundHandler outboundHandler;
    private boolean initialized = false;

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .id("weather - api")
                .name("Weather API")
                .version("1.0.0")
                .vendor("Example Corp")
                .description("Connect to weather API services for real - time weather data")
                .icon("cloud")
                .category("iot")
                .supportedProtocols(List.of("HTTP", "REST"))
                .supportedFormats(List.of("JSON"))
                .authenticationMethods(List.of("APIKey"))
                .capabilities(Map.of(
                    "realtime", true,
                    "historical", true,
                    "forecast", true,
                    "alerts", true
               ))
                .documentationUrl("https://example.com/weather - api - docs")
                .minPlatformVersion("1.0.0")
                .license("Apache-2.0")
                .tags(List.of("weather", "iot", "environmental", "api"))
                .build();
    }

    @Override
    public void initialize(Map<String, Object> configuration) throws PluginInitializationException {
        logger.info("Initializing Weather API plugin");

        this.configuration = configuration;

        // Validate required configuration
        String apiKey = (String) configuration.get("apiKey");
        if(apiKey == null || apiKey.trim().isEmpty()) {
            throw new PluginInitializationException("API key is required");
        }

        // Initialize HTTP client
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // Initialize handlers
        this.inboundHandler = new WeatherInboundHandler();
        this.outboundHandler = new WeatherOutboundHandler();

        this.initialized = true;
        logger.info("Weather API plugin initialized successfully");
    }

    @Override
    public void destroy() {
        logger.info("Destroying Weather API plugin");

        if(inboundHandler != null) {
            inboundHandler.stopListening();
        }

        this.initialized = false;
    }

    @Override
    public InboundHandler getInboundHandler() {
        return inboundHandler;
    }

    @Override
    public OutboundHandler getOutboundHandler() {
        return outboundHandler;
    }

    @Override
    public ConnectionTestResult testConnection(Direction direction) {
        if(!initialized) {
            return ConnectionTestResult.failure(
                "Plugin not initialized",
                "Please initialize the plugin first"
           );
        }

        try {
            String apiKey = (String) configuration.get("apiKey");
            String baseUrl = (String) configuration.getOrDefault("baseUrl", "https://api.weather.com");

            // Test API connection
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/test"))
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            long startTime = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long responseTime = System.currentTimeMillis() - startTime;

            if(response.statusCode() == 200) {
                return ConnectionTestResult.builder()
                        .successful(true)
                        .message("Successfully connected to Weather API")
                        .responseTime(Duration.ofMillis(responseTime))
                        .systemInfo(ConnectionTestResult.SystemInfo.builder()
                                .name("Weather API Service")
                                .version("2.0")
                                .vendor("Weather Corp")
                                .build())
                        .build();
            } else {
                return ConnectionTestResult.failure(
                    "Connection failed",
                    "HTTP " + response.statusCode() + ": " + response.body()
               );
            }

        } catch(Exception e) {
            return ConnectionTestResult.failure(
                "Connection test failed",
                e.getMessage()
           );
        }
    }

    @Override
    public HealthStatus checkHealth() {
        if(!initialized) {
            return HealthStatus.unhealthy("Plugin not initialized");
        }

        List<HealthStatus.ComponentHealth> components = new ArrayList<>();

        // Check HTTP client
        components.add(HealthStatus.ComponentHealth.builder()
                .name("HTTP Client")
                .state(HealthStatus.HealthState.HEALTHY)
                .message("HTTP client is operational")
                .build());

        // Check inbound handler
        if(inboundHandler != null) {
            components.add(HealthStatus.ComponentHealth.builder()
                    .name("Inbound Handler")
                    .state(inboundHandler.isListening()
                        ? HealthStatus.HealthState.HEALTHY
                        : HealthStatus.HealthState.DEGRADED)
                    .message(inboundHandler.isListening()
                        ? "Actively polling for weather data"
                        : "Not currently polling")
                    .build());
        }

        // Overall health
        boolean allHealthy = components.stream()
                .allMatch(c -> c.getState() == HealthStatus.HealthState.HEALTHY);

        return HealthStatus.builder()
                .state(allHealthy ? HealthStatus.HealthState.HEALTHY : HealthStatus.HealthState.DEGRADED)
                .message("Weather API plugin is " + (allHealthy ? "healthy" : "degraded"))
                .components(components)
                .metrics(HealthStatus.PerformanceMetrics.builder()
                        .messagesProcessed(inboundHandler != null ? inboundHandler.getMessagesProcessed() : 0L)
                        .errors(0L)
                        .successRate(100.0)
                        .build())
                .build();
    }

    @Override
    public ConfigurationSchema getConfigurationSchema() {
        return ConfigurationSchema.builder()
                .sections(List.of(
                    ConfigurationSchema.Section.builder()
                            .id("connection")
                            .title("API Connection")
                            .fields(List.of(
                                ConfigurationSchema.Field.builder()
                                        .name("apiKey")
                                        .type("password")
                                        .label("API Key")
                                        .required(true)
                                        .help("Your Weather API authentication key")
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("baseUrl")
                                        .type("text")
                                        .label("Base URL")
                                        .defaultValue("https://api.weather.com")
                                        .placeholder("https://api.weather.com")
                                        .help("Weather API base URL")
                                        .build()
                           ))
                            .build(),
                    ConfigurationSchema.Section.builder()
                            .id("inbound")
                            .title("Data Collection")
                            .fields(List.of(
                                ConfigurationSchema.Field.builder()
                                        .name("locations")
                                        .type("text")
                                        .label("Locations")
                                        .required(true)
                                        .placeholder("New York,London,Tokyo")
                                        .help("Comma - separated list of locations to monitor")
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("pollingInterval")
                                        .type("number")
                                        .label("Polling Interval(seconds)")
                                        .defaultValue(300)
                                        .validation(ConfigurationSchema.Validation.builder()
                                                .min(60)
                                                .max(3600)
                                                .message("Interval must be between 60 and 3600 seconds")
                                                .build())
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("dataTypes")
                                        .type("multiselect")
                                        .label("Data Types")
                                        .options(List.of(
                                            ConfigurationSchema.Option.builder()
                                                    .value("current")
                                                    .label("Current Weather")
                                                    .build(),
                                            ConfigurationSchema.Option.builder()
                                                    .value("forecast")
                                                    .label("Forecast")
                                                    .build(),
                                            ConfigurationSchema.Option.builder()
                                                    .value("alerts")
                                                    .label("Weather Alerts")
                                                    .build()
                                       ))
                                        .build()
                           ))
                            .build()
               ))
                .build();
    }

    /**
     * Inbound handler for receiving weather data
     */
    private class WeatherInboundHandler implements InboundHandler {

        private ScheduledExecutorService scheduler;
        private MessageCallback callback;
        private boolean listening = false;
        private long messagesProcessed = 0;

        @Override
        public void startListening(MessageCallback callback) throws PluginException {
            if(listening) {
                throw new PluginException("Already listening for weather data");
            }

            this.callback = callback;
            this.scheduler = Executors.newSingleThreadScheduledExecutor();

            int interval = ((Number) configuration.getOrDefault("pollingInterval", 300)).intValue();

            scheduler.scheduleWithFixedDelay(
                this::pollWeatherData,
                0,
                interval,
                TimeUnit.SECONDS
           );

            listening = true;
            logger.info("Started polling weather data every {} seconds", interval);
        }

        @Override
        public void stopListening() {
            if(scheduler != null) {
                scheduler.shutdown();
                try {
                    if(!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        scheduler.shutdownNow();
                    }
                } catch(InterruptedException e) {
                    scheduler.shutdownNow();
                }
            }
            listening = false;
            logger.info("Stopped listening for weather data");
        }

        @Override
        public PollingResult poll() {
            // Manual polling implementation
            List<PluginMessage> messages = fetchWeatherData();
            return PollingResult.builder()
                    .messages(messages)
                    .hasMore(false)
                    .build();
        }

        @Override
        public boolean isListening() {
            return listening;
        }

        private void pollWeatherData() {
            try {
                List<PluginMessage> messages = fetchWeatherData();
                for(PluginMessage message : messages) {
                    callback.onMessage(message);
                    messagesProcessed++;
                }
            } catch(Exception e) {
                logger.error("Error polling weather data", e);
                callback.onError(e);
            }
        }

        private List<PluginMessage> fetchWeatherData() {
            List<PluginMessage> messages = new ArrayList<>();

            String locations = (String) configuration.get("locations");
            if(locations != null) {
                for(String location : locations.split(",")) {
                    location = location.trim();

                    // Simulated weather data
                    Map<String, Object> weatherData = Map.of(
                        "location", location,
                        "temperature", 20 + (Math.random() * 10),
                        "humidity", 60 + (Math.random() * 20),
                        "timestamp", System.currentTimeMillis()
                   );

                    messages.add(PluginMessage.builder()
                            .headers(Map.of(
                                "location", location,
                                "dataType", "current"
                           ))
                            .body(weatherData)
                            .contentType("application/json")
                            .build());
                }
            }

            return messages;
        }

        public long getMessagesProcessed() {
            return messagesProcessed;
        }
    }

    /**
     * Outbound handler for querying weather data
     */
    private class WeatherOutboundHandler implements OutboundHandler {

        @Override
        public SendResult send(PluginMessage message) throws PluginException {
            try {
                Map<String, Object> body = (Map<String, Object>) message.getBody();
                String location = (String) body.get("location");

                if(location == null) {
                    throw new PluginException("Location is required in message body");
                }

                // Simulate API call
                String apiResponse = queryWeatherAPI(location);

                return SendResult.builder()
                        .successful(true)
                        .messageId(message.getId())
                        .externalMessageId(UUID.randomUUID().toString())
                        .response(apiResponse)
                        .build();

            } catch(Exception e) {
                return SendResult.failure(message.getId(), e.getMessage());
            }
        }

        @Override
        public BatchSendResult sendBatch(List<PluginMessage> messages) throws PluginException {
            List<SendResult> results = new ArrayList<>();

            for(PluginMessage message : messages) {
                results.add(send(message));
            }

            long successCount = results.stream().filter(SendResult::isSuccessful).count();

            return BatchSendResult.builder()
                    .totalMessages(messages.size())
                    .successCount((int) successCount)
                    .failureCount(messages.size() - (int) successCount)
                    .results(results)
                    .build();
        }

        @Override
        public boolean supportsBatch() {
            return true;
        }

        private String queryWeatherAPI(String location) {
            // Simulate weather API response
            return String.format(
                " {\"location\":\"%s\",\"temp\":%.1f,\"conditions\":\"Partly Cloudy\"}",
                location,
                15 + (Math.random() * 15)
           );
        }
    }
}
