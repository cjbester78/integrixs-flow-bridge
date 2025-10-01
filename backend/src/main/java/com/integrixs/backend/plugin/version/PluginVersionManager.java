package com.integrixs.backend.plugin.version;

import com.integrixs.backend.plugin.api.AdapterMetadata;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages plugin versions and updates
 */
@Component
public class PluginVersionManager {

    private static final Logger log = LoggerFactory.getLogger(PluginVersionManager.class);


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
            public static class PluginVersion {
        private String pluginId;
        private String version;
        private AdapterMetadata metadata;
        private String jarPath;
        private LocalDateTime releaseDate;
        private String changelog;
        private String checksum;
        private long fileSize;

        // Getters and Setters
        public String getPluginId() {
            return pluginId;
        }

        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public AdapterMetadata getMetadata() {
            return metadata;
        }

        public void setMetadata(AdapterMetadata metadata) {
            this.metadata = metadata;
        }

        public String getJarPath() {
            return jarPath;
        }

        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }

        public LocalDateTime getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(LocalDateTime releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getChangelog() {
            return changelog;
        }

        public void setChangelog(String changelog) {
            this.changelog = changelog;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        // Builder pattern
        public static PluginVersionBuilder builder() {
            return new PluginVersionBuilder();
        }

        public static class PluginVersionBuilder {
            private String pluginId;
            private String version;
            private AdapterMetadata metadata;
            private String jarPath;
            private LocalDateTime releaseDate;
            private String changelog;
            private String checksum;
            private long fileSize;

            public PluginVersionBuilder pluginId(String pluginId) {
                this.pluginId = pluginId;
                return this;
            }

            public PluginVersionBuilder version(String version) {
                this.version = version;
                return this;
            }

            public PluginVersionBuilder metadata(AdapterMetadata metadata) {
                this.metadata = metadata;
                return this;
            }

            public PluginVersionBuilder jarPath(String jarPath) {
                this.jarPath = jarPath;
                return this;
            }

            public PluginVersionBuilder releaseDate(LocalDateTime releaseDate) {
                this.releaseDate = releaseDate;
                return this;
            }

            public PluginVersionBuilder changelog(String changelog) {
                this.changelog = changelog;
                return this;
            }

            public PluginVersionBuilder checksum(String checksum) {
                this.checksum = checksum;
                return this;
            }

            public PluginVersionBuilder fileSize(long fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            public PluginVersion build() {
                PluginVersion version = new PluginVersion();
                version.pluginId = this.pluginId;
                version.version = this.version;
                version.metadata = this.metadata;
                version.jarPath = this.jarPath;
                version.releaseDate = this.releaseDate;
                version.changelog = this.changelog;
                version.checksum = this.checksum;
                version.fileSize = this.fileSize;
                return version;
            }
        }
    }

    /**
     * Semantic version representation
     */
    private static class SemanticVersion {
        private int major;
        private int minor;
        private int patch;
        private String preRelease;

        // Constructor
        public SemanticVersion(int major, int minor, int patch, String preRelease) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
            this.preRelease = preRelease;
        }

        // Builder pattern
        public static SemanticVersionBuilder builder() {
            return new SemanticVersionBuilder();
        }

        public static class SemanticVersionBuilder {
            private int major;
            private int minor;
            private int patch;
            private String preRelease;

            public SemanticVersionBuilder major(int major) {
                this.major = major;
                return this;
            }

            public SemanticVersionBuilder minor(int minor) {
                this.minor = minor;
                return this;
            }

            public SemanticVersionBuilder patch(int patch) {
                this.patch = patch;
                return this;
            }

            public SemanticVersionBuilder preRelease(String preRelease) {
                this.preRelease = preRelease;
                return this;
            }

            public SemanticVersion build() {
                return new SemanticVersion(major, minor, patch, preRelease);
            }
        }
    }

    /**
     * Update check result
     */
            public static class UpdateCheckResult {
        private boolean updateAvailable;
        private String currentVersion;
        private String latestVersion;
        private LocalDateTime releaseDate;
        private String changelog;
        private UpdatePolicy updatePolicy;
        private String message;

        // Getters and Setters
        public boolean isUpdateAvailable() {
            return updateAvailable;
        }

        public void setUpdateAvailable(boolean updateAvailable) {
            this.updateAvailable = updateAvailable;
        }

        public String getCurrentVersion() {
            return currentVersion;
        }

        public void setCurrentVersion(String currentVersion) {
            this.currentVersion = currentVersion;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public void setLatestVersion(String latestVersion) {
            this.latestVersion = latestVersion;
        }

        public LocalDateTime getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(LocalDateTime releaseDate) {
            this.releaseDate = releaseDate;
        }

        public String getChangelog() {
            return changelog;
        }

        public void setChangelog(String changelog) {
            this.changelog = changelog;
        }

        public UpdatePolicy getUpdatePolicy() {
            return updatePolicy;
        }

        public void setUpdatePolicy(UpdatePolicy updatePolicy) {
            this.updatePolicy = updatePolicy;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        // Builder pattern
        public static UpdateCheckResultBuilder builder() {
            return new UpdateCheckResultBuilder();
        }

        public static class UpdateCheckResultBuilder {
            private boolean updateAvailable;
            private String currentVersion;
            private String latestVersion;
            private LocalDateTime releaseDate;
            private String changelog;
            private UpdatePolicy updatePolicy;
            private String message;

            public UpdateCheckResultBuilder updateAvailable(boolean updateAvailable) {
                this.updateAvailable = updateAvailable;
                return this;
            }

            public UpdateCheckResultBuilder currentVersion(String currentVersion) {
                this.currentVersion = currentVersion;
                return this;
            }

            public UpdateCheckResultBuilder latestVersion(String latestVersion) {
                this.latestVersion = latestVersion;
                return this;
            }

            public UpdateCheckResultBuilder releaseDate(LocalDateTime releaseDate) {
                this.releaseDate = releaseDate;
                return this;
            }

            public UpdateCheckResultBuilder changelog(String changelog) {
                this.changelog = changelog;
                return this;
            }

            public UpdateCheckResultBuilder updatePolicy(UpdatePolicy updatePolicy) {
                this.updatePolicy = updatePolicy;
                return this;
            }

            public UpdateCheckResultBuilder message(String message) {
                this.message = message;
                return this;
            }

            public UpdateCheckResult build() {
                UpdateCheckResult result = new UpdateCheckResult();
                result.updateAvailable = this.updateAvailable;
                result.currentVersion = this.currentVersion;
                result.latestVersion = this.latestVersion;
                result.releaseDate = this.releaseDate;
                result.changelog = this.changelog;
                result.updatePolicy = this.updatePolicy;
                result.message = this.message;
                return result;
            }
        }
    }

    /**
     * Update information
     */
    public static class UpdateInfo {
        private String pluginId;
        private String currentVersion;
        private String latestVersion;
        private LocalDateTime releaseDate;
        private UpdatePolicy updatePolicy;

        // Getters and Setters
        public String getPluginId() {
            return pluginId;
        }

        public void setPluginId(String pluginId) {
            this.pluginId = pluginId;
        }

        public String getCurrentVersion() {
            return currentVersion;
        }

        public void setCurrentVersion(String currentVersion) {
            this.currentVersion = currentVersion;
        }

        public String getLatestVersion() {
            return latestVersion;
        }

        public void setLatestVersion(String latestVersion) {
            this.latestVersion = latestVersion;
        }

        public LocalDateTime getReleaseDate() {
            return releaseDate;
        }

        public void setReleaseDate(LocalDateTime releaseDate) {
            this.releaseDate = releaseDate;
        }

        public UpdatePolicy getUpdatePolicy() {
            return updatePolicy;
        }

        public void setUpdatePolicy(UpdatePolicy updatePolicy) {
            this.updatePolicy = updatePolicy;
        }

        // Builder pattern
        public static UpdateInfoBuilder builder() {
            return new UpdateInfoBuilder();
        }

        public static class UpdateInfoBuilder {
            private String pluginId;
            private String currentVersion;
            private String latestVersion;
            private LocalDateTime releaseDate;
            private UpdatePolicy updatePolicy;

            public UpdateInfoBuilder pluginId(String pluginId) {
                this.pluginId = pluginId;
                return this;
            }

            public UpdateInfoBuilder currentVersion(String currentVersion) {
                this.currentVersion = currentVersion;
                return this;
            }

            public UpdateInfoBuilder latestVersion(String latestVersion) {
                this.latestVersion = latestVersion;
                return this;
            }

            public UpdateInfoBuilder releaseDate(LocalDateTime releaseDate) {
                this.releaseDate = releaseDate;
                return this;
            }

            public UpdateInfoBuilder updatePolicy(UpdatePolicy updatePolicy) {
                this.updatePolicy = updatePolicy;
                return this;
            }

            public UpdateInfo build() {
                UpdateInfo info = new UpdateInfo();
                info.pluginId = this.pluginId;
                info.currentVersion = this.currentVersion;
                info.latestVersion = this.latestVersion;
                info.releaseDate = this.releaseDate;
                info.updatePolicy = this.updatePolicy;
                return info;
            }
        }
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
