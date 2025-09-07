package ${package};

import com.integrixs.backend.plugin.api.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

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
        
        // TODO: Validate required configuration
        // Example:
        // String apiKey = (String) configuration.get("apiKey");
        // if (apiKey == null || apiKey.trim().isEmpty()) {
        //     throw new PluginInitializationException("API key is required");
        // }
        
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
            // TODO: Implement connection test logic
            boolean success = performConnectionTest();
            
            if (success) {
                return ConnectionTestResult.builder()
                        .successful(true)
                        .message("Connection successful")
                        .responseTime(Duration.ofMillis(100))
                        .build();
            } else {
                return ConnectionTestResult.failure(
                    "Connection failed",
                    "Unable to connect to the service"
                );
            }
            
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
        
        // TODO: Add health checks for your components
        components.add(HealthStatus.ComponentHealth.builder()
                .name("Configuration")
                .state(HealthStatus.HealthState.HEALTHY)
                .message("Configuration is valid")
                .build());
        
        return HealthStatus.builder()
                .state(HealthStatus.HealthState.HEALTHY)
                .message("${pluginName} is healthy")
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
                                // TODO: Add your configuration fields
                                ConfigurationSchema.Field.builder()
                                        .name("endpoint")
                                        .type("text")
                                        .label("Endpoint URL")
                                        .required(true)
                                        .placeholder("https://api.example.com")
                                        .help("The API endpoint URL")
                                        .build()
                            ))
                            .build()
                ))
                .build();
    }
    
    private boolean performConnectionTest() {
        // TODO: Implement actual connection test
        return true;
    }
}