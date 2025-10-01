package com.integrixs.backend.api.controller;

import com.integrixs.backend.application.service.RoleManagementApplicationService;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.shared.dto.RoleDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for role management
 * Handles role CRUD operations
 */
@RestController
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
public class RoleManagementController {

    private static final Logger log = LoggerFactory.getLogger(RoleManagementController.class);


    private final RoleManagementApplicationService roleManagementApplicationService;

    public RoleManagementController(RoleManagementApplicationService roleManagementApplicationService) {
        this.roleManagementApplicationService = roleManagementApplicationService;
    }

    /**
     * Get all roles with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<List<RoleDTO>> getAllRoles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("Getting all roles - page: {}, limit: {}", page, limit);
        List<RoleDTO> roles = roleManagementApplicationService.getAllRoles(page, limit);
        return ResponseEntity.ok(roles);
    }

    /**
     * Get all roles without pagination
     */
    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        log.debug("Getting all roles without pagination");
        List<RoleDTO> roles = roleManagementApplicationService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * Get role by ID
     */
    @GetMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable String id) {
        log.debug("Getting role by id: {}", id);
        RoleDTO role = roleManagementApplicationService.getRoleById(id);
        return ResponseEntity.ok(role);
    }

    /**
     * Get role by name
     */
    @GetMapping("/name/ {name}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<RoleDTO> getRoleByName(@PathVariable String name) {
        log.debug("Getting role by name: {}", name);
        RoleDTO role = roleManagementApplicationService.getRoleByName(name);
        return ResponseEntity.ok(role);
    }

    /**
     * Create new role
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<RoleDTO> createRole(
            @Valid @RequestBody RoleDTO roleDTO,
            Authentication authentication) {
        log.info("Creating new role: {}", roleDTO.getName());
        String userId = SecurityUtils.getCurrentUserId().toString();
        RoleDTO createdRole = roleManagementApplicationService.createRole(roleDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    /**
     * Update existing role
     */
    @PutMapping("/ {id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable String id,
            @Valid @RequestBody RoleDTO roleDTO,
            Authentication authentication) {
        log.info("Updating role: {}", id);
        String userId = SecurityUtils.getCurrentUserId().toString();
        RoleDTO updatedRole = roleManagementApplicationService.updateRole(id, roleDTO, userId);
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Delete role
     */
    @DeleteMapping("/ {id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteRole(
            @PathVariable String id,
            Authentication authentication) {
        log.info("Deleting role: {}", id);
        String userId = SecurityUtils.getCurrentUserId().toString();
        roleManagementApplicationService.deleteRole(id, userId);
        return ResponseEntity.noContent().build();
    }
}
