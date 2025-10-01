package com.integrixs.backend.plugin.service;

import com.integrixs.backend.plugin.api.*;
import com.integrixs.backend.plugin.dto.*;
import com.integrixs.backend.plugin.loader.PluginLoader;
import com.integrixs.backend.plugin.registry.PluginRegistry;
import com.integrixs.backend.plugin.validation.PluginValidator;
import com.integrixs.backend.plugin.security.PluginSecurityScanner;
import com.integrixs.backend.plugin.version.PluginVersionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing plugins
 */
@Service
public class PluginService {

    private static final Logger log = LoggerFactory.getLogger(PluginService.class);


    private final PluginRegistry pluginRegistry;
    private final PluginLoader pluginLoader;
    private final PluginValidator pluginValidator;
    private final PluginSecurityScanner securityScanner;
    private final PluginVersionManager versionManager;
    private final PlatformVersionService platformVersionService;

    @Value("${plugin.upload.directory:${user.home}/.integrix/plugins}")
    private String pluginUploadDirectory;

    @Value("${plugin.temp.directory:${java.io.tmpdir}/integrix-plugins}")
    private String pluginTempDirectory;

    // Constructor
    public PluginService(PluginRegistry pluginRegistry,
                        PluginLoader pluginLoader,
                        PluginValidator pluginValidator,
                        PluginSecurityScanner securityScanner,
                        PluginVersionManager versionManager,
                        PlatformVersionService platformVersionService) {
        this.pluginRegistry = pluginRegistry;
        this.pluginLoader = pluginLoader;
        this.pluginValidator = pluginValidator;
        this.securityScanner = securityScanner;
        this.versionManager = versionManager;
        this.platformVersionService = platformVersionService;
    }

    /**
     * Upload and register a new plugin
     */
    public UploadResultDto uploadPlugin(MultipartFile file, boolean validate) throws IOException {
        if(file.isEmpty()) {
            return UploadResultDto.failure("No file uploaded");
        }

        if(!file.getOriginalFilename().endsWith(".jar")) {
            return UploadResultDto.failure("Only JAR files are supported");
        }

        // Create temp directory if it doesn't exist
        Path tempDir = Path.of(pluginTempDirectory);
        Files.createDirectories(tempDir);

        // Save uploaded file to temp location
        Path tempFile = tempDir.resolve(UUID.randomUUID() + ".jar");
        file.transferTo(tempFile.toFile());

        try {
            // Validate plugin if requested
            if(validate) {
                List<String> validationErrors = pluginValidator.validatePluginJar(tempFile);
                if(!validationErrors.isEmpty()) {
                    Files.deleteIfExists(tempFile);
                    return UploadResultDto.failure("Plugin validation failed: " +
                        String.join(", ", validationErrors));
                }
            }

            // Security scan
            var scanResult = securityScanner.scanPlugin(tempFile);
            if(!scanResult.isPassed()) {
                Files.deleteIfExists(tempFile);
                return UploadResultDto.failure("Security scan failed: " + scanResult.getSummary());
            }

            // Load plugin to get metadata
            var descriptor = pluginLoader.loadPluginFromJar(tempFile);
            AdapterMetadata metadata = descriptor.getMetadata();

            // Check if plugin already exists
            if(pluginRegistry.getRegisteredPlugins().containsKey(metadata.getId())) {
                Files.deleteIfExists(tempFile);
                return UploadResultDto.failure("Plugin with ID '" + metadata.getId() + "' already exists");
            }

            // Create plugin directory if it doesn't exist
            Path pluginDir = Path.of(pluginUploadDirectory);
            Files.createDirectories(pluginDir);

            // Move to permanent location
            Path permanentFile = pluginDir.resolve(metadata.getId() + "-" +
                metadata.getVersion() + ".jar");
            Files.move(tempFile, permanentFile, StandardCopyOption.REPLACE_EXISTING);

            // Register plugin
            try {
                pluginRegistry.registerPlugin(descriptor);
            } catch (ClassNotFoundException e) {
                Files.deleteIfExists(permanentFile);
                return UploadResultDto.failure("Failed to load plugin class: " + e.getMessage());
            }

            // Register version
            versionManager.registerVersion(metadata.getId(), metadata, permanentFile.toString());

            // Collect warnings
            List<String> warnings = new ArrayList<>();

            // Check platform version compatibility
            if(metadata.getMinPlatformVersion() != null) {
                boolean isCompatible = platformVersionService.isPluginCompatible(
                    metadata.getMinPlatformVersion(),
                    metadata.getMaxPlatformVersion()
               );

                if(!isCompatible) {
                    String compatMessage = platformVersionService.getCompatibilityMessage(
                        metadata.getMinPlatformVersion(),
                        metadata.getMaxPlatformVersion()
                   );
                    warnings.add("Platform compatibility warning: " + compatMessage);
                    log.warn("Plugin {} may not be compatible: {}", metadata.getId(), compatMessage);
                } else {
                    log.info("Plugin {} platform compatibility verified", metadata.getId());
                }
            }
            if(scanResult.getRiskLevel() != PluginSecurityScanner.RiskLevel.MINIMAL) {
                warnings.add("Security risk level: " + scanResult.getRiskLevel());
            }

            log.info("Successfully uploaded plugin: {} v {}", metadata.getId(), metadata.getVersion());
            return UploadResultDto.success(metadata.getId(), metadata, warnings);

        } catch(Exception e) {
            Files.deleteIfExists(tempFile);
            throw new IOException("Failed to upload plugin", e);
        }
    }

    /**
     * Initialize a plugin with configuration
     */
    public void initializePlugin(String pluginId, Map<String, Object> configuration) {
        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + pluginId);
        }

        // Validate configuration against schema
        ValidationResultDto validation = validateConfiguration(pluginId, configuration);
        if(!validation.isValid()) {
            throw new IllegalArgumentException("Invalid configuration: " + validation.getMessage());
        }

        // Initialize plugin
        plugin.initialize(configuration);
        pluginRegistry.markInitialized(pluginId, true);

        log.info("Initialized plugin: {}", pluginId);
    }

    /**
     * Unregister a plugin
     */
    public void unregisterPlugin(String pluginId) {
        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin != null) {
            plugin.destroy();
        }

        pluginRegistry.unregisterPlugin(pluginId);

        // Remove plugin JAR file from disk
        try {
            // Get current version to find JAR file path
            PluginVersionManager.PluginVersion currentVersion = versionManager.getCurrentVersion(pluginId);
            if(currentVersion != null && currentVersion.getJarPath() != null) {
                Path jarPath = Path.of(currentVersion.getJarPath());
                if(Files.exists(jarPath)) {
                    Files.delete(jarPath);
                    log.info("Removed plugin JAR file: {}", jarPath);
                } else {
                    log.warn("Plugin JAR file not found: {}", jarPath);
                }
            } else {
                log.warn("No version information found for plugin: {}", pluginId);
            }

            // Also try to remove any other versions from the plugin directory
            Path pluginDir = Path.of(pluginUploadDirectory);
            if(Files.exists(pluginDir)) {
                Files.list(pluginDir)
                    .filter(path -> path.getFileName().toString().startsWith(pluginId + "-"))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Removed plugin file: {}", path);
                        } catch(IOException e) {
                            log.error("Failed to delete plugin file: {}", path, e);
                        }
                    });
            }
        } catch(IOException e) {
            log.error("Failed to remove plugin JAR files for: {}", pluginId, e);
        }

        log.info("Unregistered plugin: {}", pluginId);
    }

    /**
     * Test plugin connection
     */
    public ConnectionTestResult testConnection(String pluginId, AdapterPlugin.Direction direction,
                                             Map<String, Object> configuration) {
        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin == null) {
            return ConnectionTestResult.failure("Plugin not found",
                "Plugin with ID '" + pluginId + "' is not registered");
        }

        // If configuration provided, temporarily initialize plugin
        if(configuration != null && !configuration.isEmpty()) {
            try {
                plugin.initialize(configuration);
                return plugin.testConnection(direction);
            } finally {
                // Clean up temporary initialization
                plugin.destroy();
            }
        } else {
            // Use existing initialization
            return plugin.testConnection(direction);
        }
    }

    /**
     * Validate plugin configuration
     */
    public ValidationResultDto validateConfiguration(String pluginId, Map<String, Object> configuration) {
        AdapterPlugin plugin = pluginRegistry.getPlugin(pluginId);
        if(plugin == null) {
            return ValidationResultDto.error("Plugin not found: " + pluginId);
        }

        ConfigurationSchema schema = plugin.getConfigurationSchema();
        if(schema == null || schema.getSections() == null) {
            return ValidationResultDto.success();
        }

        List<ValidationResultDto.ValidationError> errors = new ArrayList<>();

        // Validate each field in the schema
        for(ConfigurationSchema.Section section : schema.getSections()) {
            if(section.getFields() == null) continue;

            for(ConfigurationSchema.Field field : section.getFields()) {
                String fieldName = field.getName();
                Object value = configuration.get(fieldName);

                // Check required fields
                if(field.isRequired() && (value == null || value.toString().trim().isEmpty())) {
                    errors.add(ValidationResultDto.ValidationError.builder()
                            .field(fieldName)
                            .message("Field is required")
                            .build());
                    continue;
                }

                if(value == null) continue;

                // Validate field type
                if(!validateFieldType(field.getType(), value)) {
                    errors.add(ValidationResultDto.ValidationError.builder()
                            .field(fieldName)
                            .message("Invalid type. Expected: " + field.getType())
                            .value(value.toString())
                            .build());
                    continue;
                }

                // Validate against field validation rules
                if(field.getValidation() != null) {
                    String error = validateFieldValue(field, value);
                    if(error != null) {
                        errors.add(ValidationResultDto.ValidationError.builder()
                                .field(fieldName)
                                .message(error)
                                .value(value.toString())
                                .build());
                    }
                }
            }
        }

        return errors.isEmpty() ? ValidationResultDto.success() :
            ValidationResultDto.withErrors(errors);
    }

    /**
     * Search plugins
     */
    public List<PluginDto> searchPlugins(String query, String category, List<String> tags) {
        return pluginRegistry.getRegisteredPlugins().values().stream()
                .filter(metadata -> {
                    // Filter by category
                    if(category != null && !category.equals(metadata.getCategory())) {
                        return false;
                    }

                    // Filter by tags
                    if(tags != null && !tags.isEmpty()) {
                        if(metadata.getTags() == null ||
                            !metadata.getTags().containsAll(tags)) {
                            return false;
                        }
                    }

                    // Filter by query(search in name, description, vendor)
                    if(query != null && !query.trim().isEmpty()) {
                        String lowerQuery = query.toLowerCase();
                        return metadata.getName().toLowerCase().contains(lowerQuery) ||
                               (metadata.getDescription() != null &&
                                metadata.getDescription().toLowerCase().contains(lowerQuery)) ||
                               metadata.getVendor().toLowerCase().contains(lowerQuery);
                    }

                    return true;
                })
                .map(PluginDto::fromMetadata)
                .collect(Collectors.toList());
    }

    private boolean validateFieldType(String type, Object value) {
        switch(type) {
            case "text":
            case "password":
            case "textarea":
                return value instanceof String;
            case "number":
                return value instanceof Number ||
                    (value instanceof String && isNumeric((String) value));
            case "boolean":
            case "checkbox":
                return value instanceof Boolean ||
                    (value instanceof String && isBooleanString((String) value));
            case "select":
            case "radio":
                return value instanceof String;
            case "multiselect":
                return value instanceof List;
            case "date":
            case "datetime":
                return value instanceof String && isValidDateFormat((String) value, type.equals("datetime"));
            case "json":
                return true; // Accept any type for JSON fields
            default:
                return true; // Unknown types are accepted
        }
    }

    private String validateFieldValue(ConfigurationSchema.Field field, Object value) {
        ConfigurationSchema.Validation validation = field.getValidation();

        if(field.getType().equals("text") || field.getType().equals("password")) {
            String strValue = value.toString();

            if(validation.getMinLength() != null && strValue.length() < validation.getMinLength()) {
                return "Minimum length is " + validation.getMinLength();
            }

            if(validation.getMaxLength() != null && strValue.length() > validation.getMaxLength()) {
                return "Maximum length is " + validation.getMaxLength();
            }

            if(validation.getPattern() != null && !strValue.matches(validation.getPattern())) {
                return validation.getMessage() != null ? validation.getMessage() :
                    "Value does not match required pattern";
            }
        }

        if(field.getType().equals("number")) {
            double numValue = ((Number) value).doubleValue();

            if(validation.getMin() != null && numValue < validation.getMin().doubleValue()) {
                return "Minimum value is " + validation.getMin();
            }

            if(validation.getMax() != null && numValue > validation.getMax().doubleValue()) {
                return "Maximum value is " + validation.getMax();
            }
        }

        return null;
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    private boolean isBooleanString(String str) {
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }

    /**
     * Validate date format
     * Supports ISO-8601 formats:
     *-Date: yyyy-MM-dd
     *-DateTime: yyyy-MM-dd'T'HH:mm:ss(with optional timezone)
     */
    private boolean isValidDateFormat(String dateStr, boolean includeTime) {
        if(dateStr == null || dateStr.trim().isEmpty()) {
            return false;
        }

        try {
            if(includeTime) {
                // Try to parse as LocalDateTime first(without timezone)
                if(dateStr.length() >= 19 && !dateStr.contains(" + ") && !dateStr.contains("Z")) {
                    java.time.LocalDateTime.parse(dateStr);
                    return true;
                } else {
                    // Try to parse as OffsetDateTime(with timezone)
                    java.time.OffsetDateTime.parse(dateStr);
                    return true;
                }
            } else {
                // Parse as LocalDate(yyyy-MM-dd)
                java.time.LocalDate.parse(dateStr);
                return true;
            }
        } catch(java.time.format.DateTimeParseException e) {
            log.debug("Invalid date format: {}-{}", dateStr, e.getMessage());
            return false;
        }
    }
}
