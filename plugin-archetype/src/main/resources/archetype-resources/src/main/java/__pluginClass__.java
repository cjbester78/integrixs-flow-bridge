package ${package};

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * ${pluginDescription}
 */
@Slf4j
public class ${pluginClass} implements AdapterPlugin {

    private Map<String, Object> configuration;
    private ${pluginClass}InboundHandler inboundHandler;
    private ${pluginClass}OutboundHandler outboundHandler;
    private boolean initialized = false;

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .id("${pluginId}")
                .name("${pluginName}")
                .version("${version}")
                .vendor("${pluginVendor}")
                .description("${pluginDescription}")
                .icon("${pluginIcon}")
                .category("${pluginCategory}")
                .supportedProtocols(List.of("${pluginProtocol}"))
                .supportedFormats(List.of("JSON"))
                .authenticationMethods(List.of("${pluginAuth}"))
                .capabilities(Map.of(
                    "streaming", false,
                    "batch", false
               ))
                .documentationUrl("${pluginDocUrl}")
                .minPlatformVersion("1.0.0")
                .license("${pluginLicense}")
                .tags(List.of("${pluginTag}"))
                .build();
    }

    @Override
    public void initialize(Map<String, Object> configuration) throws PluginInitializationException {
        log.info("Initializing ${pluginName}");

        this.configuration = configuration;

        // Validate required configuration
        validateConfiguration(configuration);

        // Initialize handlers
        this.inboundHandler = new ${pluginClass}InboundHandler(configuration);
        this.outboundHandler = new ${pluginClass}OutboundHandler(configuration);

        this.initialized = true;
        log.info("${pluginName} initialized successfully");
    }

    @Override
    public void destroy() {
        log.info("Destroying ${pluginName}");

        if (inboundHandler != null) {
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
        if (!initialized) {
            return ConnectionTestResult.failure(
                "Plugin not initialized",
                "Please initialize the plugin first"
           );
        }

        try {
            long startTime = System.currentTimeMillis();

            // Perform actual connection test based on direction
            ConnectionTestResult result = direction == Direction.INBOUND ?
                testInboundConnection() : testOutboundConnection();

            long responseTime = System.currentTimeMillis() - startTime;

            return ConnectionTestResult.builder()
                    .successful(result.isSuccessful())
                    .message(result.getMessage())
                    .details(result.getDetails())
                    .responseTime(Duration.ofMillis(responseTime))
                    .build();

        } catch (Exception e) {
            return ConnectionTestResult.failure(
                "Connection test failed",
                e.getMessage()
           );
        }
    }

    @Override
    public HealthStatus checkHealth() {
        if (!initialized) {
            return HealthStatus.unhealthy("Plugin not initialized");
        }

        List<HealthStatus.ComponentHealth> components = new ArrayList<>();

        // Check configuration health
        try {
            validateConfiguration(configuration);
            components.add(HealthStatus.ComponentHealth.builder()
                    .name("Configuration")
                    .state(HealthStatus.HealthState.HEALTHY)
                    .message("Configuration is valid")
                    .build());
        } catch (Exception e) {
            components.add(HealthStatus.ComponentHealth.builder()
                    .name("Configuration")
                    .state(HealthStatus.HealthState.UNHEALTHY)
                    .message("Invalid configuration: " + e.getMessage())
                    .build());
        }

        // Check connection health
        ConnectionTestResult connectionTest = testConnection(Direction.OUTBOUND);
        components.add(HealthStatus.ComponentHealth.builder()
                .name("Connection")
                .state(connectionTest.isSuccessful() ?
                    HealthStatus.HealthState.HEALTHY : HealthStatus.HealthState.UNHEALTHY)
                .message(connectionTest.getMessage())
                .build());

        // Check handler health
        if (inboundHandler != null) {
            components.add(HealthStatus.ComponentHealth.builder()
                    .name("Inbound Handler")
                    .state(inboundHandler.isListening() ?
                        HealthStatus.HealthState.HEALTHY : HealthStatus.HealthState.DEGRADED)
                    .message(inboundHandler.isListening() ? "Listening" : "Not listening")
                    .build());
        }

        // Determine overall health
        HealthStatus.HealthState overallState = components.stream()
                .anyMatch(c -> c.getState() == HealthStatus.HealthState.UNHEALTHY) ?
                HealthStatus.HealthState.UNHEALTHY :
                (components.stream().anyMatch(c -> c.getState() == HealthStatus.HealthState.DEGRADED) ?
                    HealthStatus.HealthState.DEGRADED : HealthStatus.HealthState.HEALTHY);

        return HealthStatus.builder()
                .state(overallState)
                .message(overallState == HealthStatus.HealthState.HEALTHY ?
                    "${pluginName} is healthy" : "${pluginName} has issues")
                .components(components)
                .build();
    }

    @Override
    public ConfigurationSchema getConfigurationSchema() {
        return ConfigurationSchema.builder()
                .sections(List.of(
                    ConfigurationSchema.Section.builder()
                            .id("connection")
                            .title("Connection Settings")
                            .fields(List.of(
                                ConfigurationSchema.Field.builder()
                                        .name("endpoint")
                                        .type("url")
                                        .label("Endpoint URL")
                                        .required(true)
                                        .placeholder("https://api.example.com")
                                        .help("The API endpoint URL")
                                        .validation(ConfigurationSchema.Validation.builder()
                                                .pattern("^https?://.*")
                                                .message("Must be a valid HTTP(S) URL")
                                                .build())
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("timeout")
                                        .type("number")
                                        .label("Connection Timeout (seconds)")
                                        .required(false)
                                        .defaultValue("30")
                                        .help("Connection timeout in seconds")
                                        .validation(ConfigurationSchema.Validation.builder()
                                                .min(1.0)
                                                .max(300.0)
                                                .message("Timeout must be between 1 and 300 seconds")
                                                .build())
                                        .build()
                           ))
                            .build(),
                    ConfigurationSchema.Section.builder()
                            .id("authentication")
                            .title("Authentication")
                            .fields(List.of(
                                ConfigurationSchema.Field.builder()
                                        .name("authType")
                                        .type("select")
                                        .label("Authentication Type")
                                        .required(true)
                                        .options(List.of(
                                            ConfigurationSchema.Option.builder()
                                                    .value("none")
                                                    .label("None")
                                                    .build(),
                                            ConfigurationSchema.Option.builder()
                                                    .value("basic")
                                                    .label("Basic Authentication")
                                                    .build(),
                                            ConfigurationSchema.Option.builder()
                                                    .value("apiKey")
                                                    .label("API Key")
                                                    .build(),
                                            ConfigurationSchema.Option.builder()
                                                    .value("oauth2")
                                                    .label("OAuth 2.0")
                                                    .build()
                                       ))
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("apiKey")
                                        .type("password")
                                        .label("API Key")
                                        .required(false)
                                        .conditionalOn("authType", "apiKey")
                                        .help("Your API key for authentication")
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("username")
                                        .type("text")
                                        .label("Username")
                                        .required(false)
                                        .conditionalOn("authType", "basic")
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("password")
                                        .type("password")
                                        .label("Password")
                                        .required(false)
                                        .conditionalOn("authType", "basic")
                                        .build()
                           ))
                            .build(),
                    ConfigurationSchema.Section.builder()
                            .id("advanced")
                            .title("Advanced Settings")
                            .fields(List.of(
                                ConfigurationSchema.Field.builder()
                                        .name("retryAttempts")
                                        .type("number")
                                        .label("Retry Attempts")
                                        .required(false)
                                        .defaultValue("3")
                                        .help("Number of retry attempts on failure")
                                        .validation(ConfigurationSchema.Validation.builder()
                                                .min(0.0)
                                                .max(10.0)
                                                .build())
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("pollingInterval")
                                        .type("number")
                                        .label("Polling Interval (seconds)")
                                        .required(false)
                                        .defaultValue("60")
                                        .help("How often to poll for new data (inbound only)")
                                        .validation(ConfigurationSchema.Validation.builder()
                                                .min(10.0)
                                                .max(3600.0)
                                                .build())
                                        .build(),
                                ConfigurationSchema.Field.builder()
                                        .name("batchSize")
                                        .type("number")
                                        .label("Batch Size")
                                        .required(false)
                                        .defaultValue("100")
                                        .help("Maximum number of messages to process in a batch")
                                        .validation(ConfigurationSchema.Validation.builder()
                                                .min(1.0)
                                                .max(1000.0)
                                                .build())
                                        .build()
                           ))
                            .build()
               ))
                .build();
    }

    private void validateConfiguration(Map<String, Object> config) throws PluginInitializationException {
        // Validate required fields
        String endpoint = (String) config.get("endpoint");
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new PluginInitializationException("Endpoint URL is required");
        }

        // Validate URL format
        try {
            new URL(endpoint);
        } catch (Exception e) {
            throw new PluginInitializationException("Invalid endpoint URL: " + e.getMessage());
        }

        // Validate auth configuration
        String authType = (String) config.get("authType");
        if ("apiKey".equals(authType)) {
            String apiKey = (String) config.get("apiKey");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new PluginInitializationException("API Key is required when using API Key authentication");
            }
        } else if ("basic".equals(authType)) {
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
                throw new PluginInitializationException("Username and password are required for Basic authentication");
            }
        }
    }

    private ConnectionTestResult testInboundConnection() {
        try {
            // Test if we can connect and poll data
            PollingResult result = inboundHandler.poll();

            if (result != null && result.getMessages() != null) {
                return ConnectionTestResult.builder()
                        .successful(true)
                        .message("Successfully connected and polled data")
                        .details(Map.of(
                            "messagesFound", result.getMessages().size(),
                            "hasMore", result.isHasMore()
                       ))
                        .build();
            } else {
                return ConnectionTestResult.builder()
                        .successful(true)
                        .message("Connected successfully, no data available")
                        .build();
            }
        } catch (Exception e) {
            return ConnectionTestResult.failure(
                "Inbound connection test failed",
                e.getMessage()
           );
        }
    }

    private ConnectionTestResult testOutboundConnection() {
        try {
            String endpoint = (String) configuration.get("endpoint");
            int timeout = Integer.parseInt(configuration.getOrDefault("timeout", "30").toString()) * 1000;

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);

            // Add authentication headers
            addAuthenticationHeaders(conn);

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            if (responseCode >= 200 && responseCode < 300) {
                return ConnectionTestResult.builder()
                        .successful(true)
                        .message("Successfully connected to " + endpoint)
                        .details(Map.of(
                            "responseCode", responseCode,
                            "endpoint", endpoint
                       ))
                        .build();
            } else {
                return ConnectionTestResult.builder()
                        .successful(false)
                        .message("Connection returned status " + responseCode)
                        .details(Map.of(
                            "responseCode", responseCode,
                            "endpoint", endpoint
                       ))
                        .build();
            }
        } catch (Exception e) {
            return ConnectionTestResult.failure(
                "Outbound connection test failed",
                e.getMessage()
           );
        }
    }

    private void addAuthenticationHeaders(HttpURLConnection conn) {
        String authType = (String) configuration.get("authType");

        if ("apiKey".equals(authType)) {
            String apiKey = (String) configuration.get("apiKey");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        } else if ("basic".equals(authType)) {
            String username = (String) configuration.get("username");
            String password = (String) configuration.get("password");
            String auth = username + ":" + password;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        }
    }
}