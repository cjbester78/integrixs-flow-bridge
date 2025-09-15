package com.integrixs.backend.plugin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlatformVersionService
 */
class PlatformVersionServiceTest {

    private PlatformVersionService platformVersionService;

    @BeforeEach
    void setUp() {
        platformVersionService = new PlatformVersionService();
        platformVersionService.init();
    }

    @Test
    @DisplayName("Should parse and compare semantic versions correctly")
    void testVersionComparison() {
        // Test major version differences
        assertTrue(platformVersionService.compareVersions("2.0.0", "1.0.0") > 0);
        assertTrue(platformVersionService.compareVersions("1.0.0", "2.0.0") < 0);

        // Test minor version differences
        assertTrue(platformVersionService.compareVersions("1.2.0", "1.1.0") > 0);
        assertTrue(platformVersionService.compareVersions("1.1.0", "1.2.0") < 0);

        // Test patch version differences
        assertTrue(platformVersionService.compareVersions("1.0.2", "1.0.1") > 0);
        assertTrue(platformVersionService.compareVersions("1.0.1", "1.0.2") < 0);

        // Test equal versions
        assertEquals(0, platformVersionService.compareVersions("1.0.0", "1.0.0"));

        // Test pre - release versions
        assertTrue(platformVersionService.compareVersions("1.0.0", "1.0.0 - alpha") > 0);
        assertTrue(platformVersionService.compareVersions("1.0.0 - alpha", "1.0.0") < 0);
        assertTrue(platformVersionService.compareVersions("1.0.0 - beta", "1.0.0 - alpha") > 0);
    }

    @Test
    @DisplayName("Should check plugin compatibility correctly")
    void testPluginCompatibility() {
        // Assuming platform version is 1.0.0(default)

        // No version constraints - always compatible
        assertTrue(platformVersionService.isPluginCompatible(null, null));

        // Only minimum version
        assertTrue(platformVersionService.isPluginCompatible("1.0.0", null));
        assertTrue(platformVersionService.isPluginCompatible("0.9.0", null));
        assertFalse(platformVersionService.isPluginCompatible("1.1.0", null));

        // Only maximum version
        assertTrue(platformVersionService.isPluginCompatible(null, "1.0.0"));
        assertTrue(platformVersionService.isPluginCompatible(null, "1.1.0"));
        assertFalse(platformVersionService.isPluginCompatible(null, "0.9.0"));

        // Both constraints
        assertTrue(platformVersionService.isPluginCompatible("1.0.0", "1.0.0"));
        assertTrue(platformVersionService.isPluginCompatible("0.9.0", "1.1.0"));
        assertFalse(platformVersionService.isPluginCompatible("1.1.0", "1.2.0"));
        assertFalse(platformVersionService.isPluginCompatible("0.1.0", "0.9.0"));
    }

    @Test
    @DisplayName("Should generate appropriate compatibility messages")
    void testCompatibilityMessages() {
        // No constraints
        String message = platformVersionService.getCompatibilityMessage(null, null);
        assertTrue(message.contains("Compatible with all platform versions"));

        // Only minimum version
        message = platformVersionService.getCompatibilityMessage("1.0.0", null);
        assertTrue(message.contains("Requires platform version 1.0.0 or higher"));
        assertTrue(message.contains("current:"));

        // Only maximum version
        message = platformVersionService.getCompatibilityMessage(null, "2.0.0");
        assertTrue(message.contains("Requires platform version 2.0.0 or lower"));

        // Both constraints
        message = platformVersionService.getCompatibilityMessage("0.9.0", "1.1.0");
        assertTrue(message.contains("Requires platform version 0.9.0 to 1.1.0"));

        // Incompatible version
        message = platformVersionService.getCompatibilityMessage("2.0.0", null);
        assertTrue(message.contains("INCOMPATIBLE"));
    }

    @Test
    @DisplayName("Should handle invalid version formats")
    void testInvalidVersionFormats() {
        assertThrows(IllegalArgumentException.class,
            () -> platformVersionService.compareVersions("invalid", "1.0.0"));

        assertThrows(IllegalArgumentException.class,
            () -> platformVersionService.compareVersions("1.0", "1.0.0"));

        assertThrows(IllegalArgumentException.class,
            () -> platformVersionService.compareVersions("1.0.0.0", "1.0.0"));
    }

    @Test
    @DisplayName("Should get platform version")
    void testGetPlatformVersion() {
        String version = platformVersionService.getPlatformVersion();
        assertNotNull(version);
        assertFalse(version.isEmpty());
        // Should be a valid semantic version
        assertTrue(version.matches("\\d + \\.\\d + \\.\\d + (-.*)?"));
    }
}
