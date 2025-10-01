package com.integrixs.backend.controller;

import com.integrixs.backend.service.ExternalAuthenticationService;
import com.integrixs.backend.service.OAuth2TokenRefreshService;
import com.integrixs.shared.dto.ExternalAuthenticationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing external authentication configurations
 */
@RestController
@RequestMapping("/api/external - auth")
@Tag(name = "External Authentication", description = "External authentication management endpoints")
public class ExternalAuthenticationController {

    @Autowired
    private ExternalAuthenticationService authenticationService;

    @Autowired
    private OAuth2TokenRefreshService tokenRefreshService;

    @PostMapping
    @Operation(summary = "Create external authentication", description = "Create a new external authentication configuration")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<ExternalAuthenticationDTO> createAuthentication(@RequestBody ExternalAuthenticationDTO dto) {
        ExternalAuthenticationDTO created = authenticationService.createAuthentication(dto);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/ {id}")
    @Operation(summary = "Update external authentication", description = "Update an existing external authentication configuration")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<ExternalAuthenticationDTO> updateAuthentication(
            @PathVariable String id,
            @RequestBody ExternalAuthenticationDTO dto) {
        ExternalAuthenticationDTO updated = authenticationService.updateAuthentication(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/ {id}")
    @Operation(summary = "Delete external authentication", description = "Delete an external authentication configuration")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteAuthentication(@PathVariable String id) {
        authenticationService.deleteAuthentication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ {id}")
    @Operation(summary = "Get external authentication", description = "Get a specific external authentication configuration")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<ExternalAuthenticationDTO> getAuthentication(@PathVariable String id) {
        ExternalAuthenticationDTO auth = authenticationService.getAuthenticationById(id);
        return ResponseEntity.ok(auth);
    }

    @GetMapping("/business - component/ {businessComponentId}")
    @Operation(summary = "List authentications for business component",
               description = "Get all external authentication configurations for a business component")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<ExternalAuthenticationDTO>> getAuthenticationsByBusinessComponent(
            @PathVariable String businessComponentId) {
        List<ExternalAuthenticationDTO> auths = authenticationService.getAuthenticationsByBusinessComponent(businessComponentId);
        return ResponseEntity.ok(auths);
    }

    @PostMapping("/ {id}/test")
    @Operation(summary = "Test authentication", description = "Test an external authentication configuration")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<Map<String, Object>> testAuthentication(@PathVariable String id) {
        Map<String, Object> result = authenticationService.testAuthentication(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ {id}/refresh - token")
    @Operation(summary = "Refresh OAuth2 token", description = "Manually refresh an OAuth2 access token")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<Map<String, Object>> refreshOAuth2Token(@PathVariable String id) {
        Map<String, Object> result = tokenRefreshService.refreshTokenManually(id);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ {id}/oauth2/initiate")
    @Operation(summary = "Initiate OAuth2 flow", description = "Get authorization URL for OAuth2 flow")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<Map<String, String>> initiateOAuth2Flow(
            @PathVariable String id,
            @RequestParam(required = false) String redirectUri) {
        Map<String, String> result = tokenRefreshService.initiateAuthorizationFlow(id, redirectUri);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ {id}/oauth2/callback")
    @Operation(summary = "OAuth2 callback", description = "Handle OAuth2 authorization callback and exchange code for token")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<Map<String, Object>> handleOAuth2Callback(
            @PathVariable String id,
            @RequestParam String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String redirectUri) {
        Map<String, Object> result = tokenRefreshService.exchangeAuthorizationCode(id, code, state, redirectUri);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ {id}/oauth2/client - credentials")
    @Operation(summary = "Request client credentials token", description = "Request OAuth2 token using client credentials flow")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<Map<String, Object>> requestClientCredentialsToken(@PathVariable String id) {
        Map<String, Object> result = tokenRefreshService.requestClientCredentialsToken(id);
        return ResponseEntity.ok(result);
    }
}
