package ${package};

import com.integrixs.backend.plugin.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ${pluginName}
 */
public class ${pluginClass}Test {

    private ${pluginClass} plugin;

    @BeforeEach
    public void setup() {
        plugin = new ${pluginClass}();
    }

    @Test
    public void testMetadata() {
        AdapterMetadata metadata = plugin.getMetadata();

        assertNotNull(metadata);
        assertEquals("${pluginId}", metadata.getId());
        assertEquals("${pluginName}", metadata.getName());
        assertEquals("${version}", metadata.getVersion());
        assertEquals("${pluginVendor}", metadata.getVendor());
    }

    @Test
    public void testInitialization() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "https://api.example.com");

        assertDoesNotThrow(() -> plugin.initialize(config));

        assertNotNull(plugin.getInboundHandler());
        assertNotNull(plugin.getOutboundHandler());
    }

    @Test
    public void testConfigurationSchema() {
        ConfigurationSchema schema = plugin.getConfigurationSchema();

        assertNotNull(schema);
        assertNotNull(schema.getSections());
        assertFalse(schema.getSections().isEmpty());
    }

    @Test
    public void testConnectionTest() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "https://api.example.com");

        plugin.initialize(config);

        ConnectionTestResult result = plugin.testConnection(Direction.OUTBOUND);

        assertNotNull(result);
        // TODO: Update assertions based on your implementation
    }

    @Test
    public void testHealthCheck() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "https://api.example.com");

        plugin.initialize(config);

        HealthStatus health = plugin.checkHealth();

        assertNotNull(health);
        assertEquals(HealthStatus.HealthState.HEALTHY, health.getState());
    }

    @Test
    public void testDestroy() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", "https://api.example.com");

        plugin.initialize(config);

        assertDoesNotThrow(() -> plugin.destroy());
    }
}