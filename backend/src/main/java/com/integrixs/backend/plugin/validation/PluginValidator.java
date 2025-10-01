package com.integrixs.backend.plugin.validation;

import com.integrixs.backend.plugin.api.AdapterPlugin;
import com.integrixs.backend.plugin.api.PluginInitializationException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("pluginValidationService")
public class PluginValidator {

    public ValidationResult validate(AdapterPlugin plugin) {
        List<String> errors = new ArrayList<>();

        // Validate plugin metadata
        if (plugin.getMetadata() == null) {
            errors.add("Plugin metadata is required");
        } else {
            if (plugin.getMetadata().getName() == null || plugin.getMetadata().getName().isEmpty()) {
                errors.add("Plugin name is required");
            }
            if (plugin.getMetadata().getVersion() == null || plugin.getMetadata().getVersion().isEmpty()) {
                errors.add("Plugin version is required");
            }
        }

        // Validate handlers
        if (plugin.getInboundHandler() == null && plugin.getOutboundHandler() == null) {
            errors.add("Plugin must provide at least one handler (inbound or outbound)");
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        private ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, new ArrayList<>());
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    public List<String> validatePluginJar(java.nio.file.Path jarPath) {
        List<String> errors = new ArrayList<>();

        // Check if file exists
        if (!java.nio.file.Files.exists(jarPath)) {
            errors.add("JAR file does not exist: " + jarPath);
            return errors;
        }

        // Check if it's a JAR file
        if (!jarPath.toString().toLowerCase().endsWith(".jar")) {
            errors.add("File is not a JAR file: " + jarPath);
            return errors;
        }

        // Check file size
        try {
            long fileSize = java.nio.file.Files.size(jarPath);
            if (fileSize == 0) {
                errors.add("JAR file is empty");
            } else if (fileSize > 100 * 1024 * 1024) { // 100MB
                errors.add("JAR file is too large (max 100MB)");
            }
        } catch (java.io.IOException e) {
            errors.add("Cannot read JAR file: " + e.getMessage());
        }

        // Try to open as JAR
        try (java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath.toFile())) {
            // Check for manifest
            if (jarFile.getManifest() == null) {
                errors.add("JAR file has no manifest");
            }

            // Check for required entries
            boolean hasClasses = jarFile.stream()
                    .anyMatch(entry -> entry.getName().endsWith(".class"));
            if (!hasClasses) {
                errors.add("JAR file contains no classes");
            }
        } catch (java.io.IOException e) {
            errors.add("Invalid JAR file: " + e.getMessage());
        }

        return errors;
    }
}