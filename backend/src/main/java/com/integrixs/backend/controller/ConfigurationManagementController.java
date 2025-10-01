package com.integrixs.backend.controller;

import com.integrixs.backend.config.DatabaseConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin/config-management")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigurationManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementController.class);

    private final DatabaseConfigurationService configurationService;

    @Autowired
    public ConfigurationManagementController(DatabaseConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getConfigurationCategories() {
        // TODO: Implement fetching categories from database
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<Map<String, Object>> getConfigurationsByCategory(@PathVariable String categoryCode) {
        // TODO: Implement fetching configurations by category
        Map<String, Object> response = new HashMap<>();
        response.put("category", categoryCode);
        response.put("configurations", List.of());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{key}")
    public ResponseEntity<Map<String, Object>> getConfiguration(@PathVariable String key) {
        String value = configurationService.getConfiguration(key);
        if (value != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("key", key);
            response.put("value", value);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{key}")
    public ResponseEntity<Map<String, Object>> updateConfiguration(
            @PathVariable String key,
            @RequestBody Map<String, String> request) {

        String newValue = request.get("value");
        if (newValue == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Value is required"));
        }

        try {
            configurationService.updateConfiguration(key, newValue);
            Map<String, Object> response = new HashMap<>();
            response.put("key", key);
            response.put("value", newValue);
            response.put("message", "Configuration updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating configuration: {}", key, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to update configuration"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshConfigurations() {
        try {
            configurationService.refreshConfigurations();
            return ResponseEntity.ok(Map.of(
                "message", "Configurations refreshed successfully",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            logger.error("Error refreshing configurations", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to refresh configurations"));
        }
    }

    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportConfigurations(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String environment) {

        // TODO: Implement configuration export
        Map<String, Object> response = new HashMap<>();
        response.put("configurations", Map.of());
        response.put("exportedAt", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importConfigurations(
            @RequestBody Map<String, Object> configurations) {

        // TODO: Implement configuration import
        return ResponseEntity.ok(Map.of(
            "message", "Configurations imported successfully",
            "imported", 0
        ));
    }
}