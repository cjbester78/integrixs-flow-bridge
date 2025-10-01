package com.integrixs.backend.plugin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing platform version information
 */
@Service
public class PlatformVersionService {

    private static final Logger log = LoggerFactory.getLogger(PlatformVersionService.class);


    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:-(.+))?$");

    @Value("${platform.version:}")
    private String configuredVersion;

    private String platformVersion;

    @PostConstruct
    public void init() {
        // Try to get version from configuration first
        if(configuredVersion != null && !configuredVersion.trim().isEmpty()) {
            platformVersion = configuredVersion;
            log.info("Platform version from configuration: {}", platformVersion);
            return;
        }

        // Try to get version from Maven properties
        try {
            Properties properties = new Properties();
            InputStream stream = getClass().getResourceAsStream("/META-INF/maven/com.integrixs/integrix-flow-bridge/pom.properties");
            if(stream != null) {
                properties.load(stream);
                String version = properties.getProperty("version");
                if(version != null) {
                    // Remove -SNAPSHOT suffix if present
                    platformVersion = version.replace("-SNAPSHOT", "");
                    log.info("Platform version from Maven: {}", platformVersion);
                    return;
                }
            }
        } catch(IOException e) {
            log.debug("Could not read Maven properties: {}", e.getMessage());
        }

        // Default version if nothing else is available
        platformVersion = "1.0.0";
        log.info("Using default platform version: {}", platformVersion);
    }

    /**
     * Get the current platform version
     */
    public String getPlatformVersion() {
        return platformVersion;
    }

    /**
     * Check if a plugin is compatible with the current platform
     * @param minVersion Minimum required platform version(null means no minimum)
     * @param maxVersion Maximum supported platform version(null means no maximum)
     * @return true if compatible, false otherwise
     */
    public boolean isPluginCompatible(String minVersion, String maxVersion) {
        if(minVersion == null && maxVersion == null) {
            return true; // No version constraints
        }

        try {
            // Check minimum version
            if(minVersion != null && compareVersions(platformVersion, minVersion) < 0) {
                log.debug("Platform version {} is less than minimum required {}", platformVersion, minVersion);
                return false;
            }

            // Check maximum version
            if(maxVersion != null && compareVersions(platformVersion, maxVersion) > 0) {
                log.debug("Platform version {} is greater than maximum supported {}", platformVersion, maxVersion);
                return false;
            }

            return true;
        } catch(Exception e) {
            log.error("Error checking version compatibility", e);
            return false;
        }
    }

    /**
     * Compare two semantic version strings
     * @return negative if v1 < v2, zero if v1 = v2, positive if v1 > v2
     */
    public int compareVersions(String version1, String version2) {
        SemanticVersion v1 = parseVersion(version1);
        SemanticVersion v2 = parseVersion(version2);

        int result = Integer.compare(v1.major, v2.major);
        if(result != 0) return result;

        result = Integer.compare(v1.minor, v2.minor);
        if(result != 0) return result;

        result = Integer.compare(v1.patch, v2.patch);
        if(result != 0) return result;

        // Handle pre-release versions
        if(v1.preRelease == null && v2.preRelease != null) return 1;
        if(v1.preRelease != null && v2.preRelease == null) return -1;
        if(v1.preRelease != null && v2.preRelease != null) {
            return v1.preRelease.compareTo(v2.preRelease);
        }

        return 0;
    }

    /**
     * Parse a semantic version string
     */
    private SemanticVersion parseVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }

        return new SemanticVersion(
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2)),
            Integer.parseInt(matcher.group(3)),
            matcher.group(4)
       );
    }

    /**
     * Internal representation of a semantic version
     */
    private static class SemanticVersion {
        final int major;
        final int minor;
        final int patch;
        final String preRelease;

        SemanticVersion(int major, int minor, int patch, String preRelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preRelease = preRelease;
        }
    }

    /**
     * Get a human-readable compatibility message
     */
    public String getCompatibilityMessage(String minVersion, String maxVersion) {
        if(minVersion == null && maxVersion == null) {
            return "Compatible with all platform versions";
        }

        StringBuilder message = new StringBuilder("Requires platform version ");

        if(minVersion != null && maxVersion != null) {
            message.append(minVersion).append(" to ").append(maxVersion);
        } else if(minVersion != null) {
            message.append(minVersion).append(" or higher");
        } else {
            message.append(maxVersion).append(" or lower");
        }

        message.append(" (current: ").append(platformVersion).append(")");

        if(!isPluginCompatible(minVersion, maxVersion)) {
            message.append(" - INCOMPATIBLE");
        }

        return message.toString();
    }
}
