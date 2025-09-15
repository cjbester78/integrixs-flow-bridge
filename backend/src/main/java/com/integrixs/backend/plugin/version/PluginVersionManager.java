package com.integrixs.backend.plugin.version;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Manages plugin versions and updates
 */
@Component
@Slf4j
public class PluginVersionManager {

    private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d + )\\.(\\d + )\\.(\\d + )(?:-(. + ))?$");

    // In - memory storage for simplicity - in production, use database
    private final Map<String, List<PluginVersion>> versionHistory = new ConcurrentHashMap<>();
    private final Map<String, PluginVersion> currentVersions = new ConcurrentHashMap<>();
    private final Map<String, UpdatePolicy> updatePolicies = new ConcurrentHashMap<>();

    /**
     * Register a new plugin version
     */
    public void registerVersion(String pluginId, AdapterMetadata metadata, String jarPath) {
        PluginVersion version = PluginVersion.builder()
                .pluginId(pluginId)
                .version(metadata.getVersion())
                .metadata(metadata)
                .jarPath(jarPath)
                .releaseDate(LocalDateTime.now())
                .build();

        // Add to version history
        versionHistory.computeIfAbsent(pluginId, k -> new ArrayList<>()).add(version);

        // Update current version if newer
        PluginVersion current = currentVersions.get(pluginId);
        if(current == null || isNewerVersion(version.getVersion(), current.getVersion())) {
            currentVersions.put(pluginId, version);
            log.info("Updated plugin {} to version {}", pluginId, version.getVersion());
        }
    }

    /**
     * Get current version of a plugin
     */
    public PluginVersion getCurrentVersion(String pluginId) {
        return currentVersions.get(pluginId);
    }

    /**
     * Get version history for a plugin
     */
    public List<PluginVersion> getVersionHistory(String pluginId) {
        return versionHistory.getOrDefault(pluginId, Collections.emptyList())
                .stream()
                .sorted((v1, v2) -> compareVersions(v2.getVersion(), v1.getVersion()))
                .collect(Collectors.toList());
    }

    /**
     * Check if update is available
     */
    public UpdateCheckResult checkForUpdate(String pluginId, String currentVersion) {
        PluginVersion latest = currentVersions.get(pluginId);
        if(latest == null) {
            return UpdateCheckResult.builder()
                    .updateAvailable(false)
                    .message("Plugin not found in registry")
                    .build();
        }

        boolean updateAvailable = isNewerVersion(latest.getVersion(), currentVersion);

        return UpdateCheckResult.builder()
                .updateAvailable(updateAvailable)
                .currentVersion(currentVersion)
                .latestVersion(latest.getVersion())
                .releaseDate(latest.getReleaseDate())
                .changelog(latest.getChangelog())
                .updatePolicy(getUpdatePolicy(pluginId))
                .build();
    }

    /**
     * Set update policy for a plugin
     */
    public void setUpdatePolicy(String pluginId, UpdatePolicy policy) {
        updatePolicies.put(pluginId, policy);
    }

    /**
     * Get update policy for a plugin
     */
    public UpdatePolicy getUpdatePolicy(String pluginId) {
        return updatePolicies.getOrDefault(pluginId, UpdatePolicy.MANUAL);
    }

    /**
     * Get all available updates
     */
    public List<UpdateInfo> getAllAvailableUpdates(Map<String, String> installedVersions) {
        List<UpdateInfo> updates = new ArrayList<>();

        for(Map.Entry<String, String> entry : installedVersions.entrySet()) {
            String pluginId = entry.getKey();
            String installedVersion = entry.getValue();

            UpdateCheckResult result = checkForUpdate(pluginId, installedVersion);
            if(result.isUpdateAvailable()) {
                updates.add(UpdateInfo.builder()
                        .pluginId(pluginId)
                        .currentVersion(installedVersion)
                        .latestVersion(result.getLatestVersion())
                        .releaseDate(result.getReleaseDate())
                        .updatePolicy(result.getUpdatePolicy())
                        .build());
            }
        }

        return updates;
    }

    /**
     * Compare two version strings
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

        // Handle pre - release versions
        if(v1.preRelease == null && v2.preRelease != null) return 1;
        if(v1.preRelease != null && v2.preRelease == null) return -1;
        if(v1.preRelease != null && v2.preRelease != null) {
            return v1.preRelease.compareTo(v2.preRelease);
        }

        return 0;
    }

    /**
     * Check if version1 is newer than version2
     */
    public boolean isNewerVersion(String version1, String version2) {
        return compareVersions(version1, version2) > 0;
    }

    /**
     * Check version compatibility
     */
    public boolean isCompatibleVersion(String requiredVersion, String actualVersion) {
        // Simple compatibility check - actual version must be >= required version
        return compareVersions(actualVersion, requiredVersion) >= 0;
    }

    /**
     * Parse semantic version string
     */
    private SemanticVersion parseVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);
        if(!matcher.matches()) {
            throw new IllegalArgumentException("Invalid version format: " + version);
        }

        return SemanticVersion.builder()
                .major(Integer.parseInt(matcher.group(1)))
                .minor(Integer.parseInt(matcher.group(2)))
                .patch(Integer.parseInt(matcher.group(3)))
                .preRelease(matcher.group(4))
                .build();
    }

    /**
     * Plugin version information
     */
    @Data
    @Builder
    public static class PluginVersion {
        private String pluginId;
        private String version;
        private AdapterMetadata metadata;
        private String jarPath;
        private LocalDateTime releaseDate;
        private String changelog;
        private String checksum;
        private long fileSize;
    }

    /**
     * Semantic version representation
     */
    @Data
    @Builder
    private static class SemanticVersion {
        private int major;
        private int minor;
        private int patch;
        private String preRelease;
    }

    /**
     * Update check result
     */
    @Data
    @Builder
    public static class UpdateCheckResult {
        private boolean updateAvailable;
        private String currentVersion;
        private String latestVersion;
        private LocalDateTime releaseDate;
        private String changelog;
        private UpdatePolicy updatePolicy;
        private String message;
    }

    /**
     * Update information
     */
    @Data
    @Builder
    public static class UpdateInfo {
        private String pluginId;
        private String currentVersion;
        private String latestVersion;
        private LocalDateTime releaseDate;
        private UpdatePolicy updatePolicy;
    }

    /**
     * Update policy
     */
    public enum UpdatePolicy {
        /**
         * Updates must be manually approved
         */
        MANUAL,

        /**
         * Automatically update to patch versions(x.x.*)
         */
        AUTO_PATCH,

        /**
         * Automatically update to minor versions(x.*.*)
         */
        AUTO_MINOR,

        /**
         * Automatically update to all versions
         */
        AUTO_ALL,

        /**
         * Never update automatically
         */
        DISABLED
    }
}
