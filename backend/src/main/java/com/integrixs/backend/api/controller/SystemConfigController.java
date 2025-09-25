package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.UpdateTimezoneRequest;
import com.integrixs.backend.api.dto.response.SystemConfigResponse;
import com.integrixs.backend.api.dto.response.TimezoneResponse;
import com.integrixs.backend.application.service.SystemConfigurationApplicationService;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.UserSqlRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for system configuration management
 */
@RestController
@RequestMapping("/api/system/config")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "System Configuration", description = "System configuration management")
public class SystemConfigController {

    private static final Logger log = LoggerFactory.getLogger(SystemConfigController.class);


    private final SystemConfigurationApplicationService systemConfigurationService;
    private final UserSqlRepository userRepository;

    public SystemConfigController(SystemConfigurationApplicationService systemConfigurationService,
                                  UserSqlRepository userRepository) {
        this.systemConfigurationService = systemConfigurationService;
        this.userRepository = userRepository;
    }

    /**
     * Get all system configurations
     */
    @GetMapping("/settings")
    @Operation(summary = "Get all system settings")
    public ResponseEntity<SystemConfigResponse> getSystemSettings() {
        log.debug("Getting all system settings");
        return ResponseEntity.ok(systemConfigurationService.getAllConfigurations());
    }

    /**
     * Get system timezone info
     */
    @GetMapping("/timezone")
    @Operation(summary = "Get system timezone information")
    public ResponseEntity<SystemConfigResponse> getTimezoneInfo() {
        log.debug("Getting system timezone info");
        return ResponseEntity.ok(systemConfigurationService.getTimezoneInfo());
    }

    /**
     * Update system timezone
     */
    @PutMapping("/timezone")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Update system timezone")
    public ResponseEntity<Map<String, String>> updateSystemTimezone(
            @Valid @RequestBody UpdateTimezoneRequest request) {

        log.info("Updating system timezone to: {}", request.getTimezone());

        try {
            String username = SecurityUtils.getCurrentUsernameStatic();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            systemConfigurationService.updateSystemTimezone(request.getTimezone(), currentUser);

            return ResponseEntity.ok(Map.of(
                "message", "Timezone updated successfully",
                "timezone", request.getTimezone()
           ));
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
           ));
        }
    }

    /**
     * Get available timezones
     */
    @GetMapping("/timezones")
    @Operation(summary = "Get available timezones")
    public ResponseEntity<List<TimezoneResponse>> getAvailableTimezones() {
        log.debug("Getting available timezones");
        return ResponseEntity.ok(systemConfigurationService.getAvailableTimezones());
    }

    /**
     * Get environment configuration
     */
    @GetMapping("/environment")
    @Operation(summary = "Get environment configuration")
    public ResponseEntity<Map<String, String>> getEnvironmentConfig() {
        log.debug("Getting environment configuration");
        return ResponseEntity.ok(Map.of(
            "environment", systemConfigurationService.getEnvironmentType(),
            "type", systemConfigurationService.getEnvironmentType()
       ));
    }

    /**
     * Get permissions configuration
     */
    @GetMapping("/permissions")
    @Operation(summary = "Get permissions configuration")
    public ResponseEntity<Map<String, Object>> getPermissionsConfig() {
        log.debug("Getting permissions configuration");
        String currentRole = SecurityUtils.getCurrentUserRole();
        return ResponseEntity.ok(Map.of(
            "role", currentRole,
            "canCreate", systemConfigurationService.canCreateResources(currentRole),
            "canModify", systemConfigurationService.canModifyResources(currentRole),
            "canDelete", systemConfigurationService.canDeleteResources(currentRole),
            "canDeploy", systemConfigurationService.canDeployFlows(currentRole)
       ));
    }
}
