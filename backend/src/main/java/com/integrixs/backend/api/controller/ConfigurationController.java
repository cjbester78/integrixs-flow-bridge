package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.ConfigurationManagementApplicationService;
import com.integrixs.backend.dto.configuration.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/configurations")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigurationController {

    private final ConfigurationManagementApplicationService configurationService;

    @Autowired
    public ConfigurationController(ConfigurationManagementApplicationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ConfigurationCategoryDto>> getCategories() {
        return ResponseEntity.ok(configurationService.getCategories());
    }

    @GetMapping("/category/{categoryCode}")
    public ResponseEntity<List<ConfigurationDto>> getConfigurationsByCategory(
            @PathVariable String categoryCode,
            @RequestParam(required = false) String environment) {
        return ResponseEntity.ok(configurationService.getConfigurationsByCategory(categoryCode, environment));
    }

    @GetMapping("/messaging")
    public ResponseEntity<List<MessagingConfigurationDto>> getMessagingConfigurations(
            @RequestParam(required = false) String messagingSystem) {
        return ResponseEntity.ok(configurationService.getMessagingConfigurations(messagingSystem));
    }

    @GetMapping("/security")
    public ResponseEntity<List<SecurityConfigurationDto>> getSecurityConfigurations(
            @RequestParam(required = false) String securityDomain) {
        return ResponseEntity.ok(configurationService.getSecurityConfigurations(securityDomain));
    }

    @GetMapping("/adapter/{adapterTypeId}")
    public ResponseEntity<List<AdapterConfigurationDto>> getAdapterDefaultConfigurations(
            @PathVariable String adapterTypeId) {
        return ResponseEntity.ok(configurationService.getAdapterDefaultConfigurations(adapterTypeId));
    }

    @PutMapping("/{configKey}")
    public ResponseEntity<Void> updateConfiguration(
            @PathVariable String configKey,
            @RequestBody Map<String, Object> request) {
        String value = request.get("value").toString();
        String table = request.getOrDefault("table", "application_configurations").toString();
        configurationService.updateConfiguration(configKey, value, table);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/batch")
    public ResponseEntity<Void> updateConfigurations(
            @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, String> configurations = (Map<String, String>) request.get("configurations");
        String table = request.getOrDefault("table", "application_configurations").toString();
        configurationService.updateConfigurations(configurations, table);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshConfigurations() {
        configurationService.refreshConfigurations();
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> exportConfigurations(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String environment) {
        byte[] data = configurationService.exportConfigurations(category, environment);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=configurations.json")
                .body(data);
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Integer>> importConfigurations(
            @RequestParam("file") MultipartFile file) {
        Map<String, Integer> result = configurationService.importConfigurations(file);
        return ResponseEntity.ok(result);
    }
}