package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.ChangePasswordRequest;
import com.integrixs.backend.api.dto.request.CreateUserRequest;
import com.integrixs.backend.api.dto.request.UpdateUserRequest;
import com.integrixs.backend.api.dto.response.PagedUserResponse;
import com.integrixs.backend.api.dto.response.UserResponse;
import com.integrixs.backend.application.service.UserManagementApplicationService;
import com.integrixs.backend.logging.BusinessOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user management
 */
@RestController
@RequestMapping("/api/users")
@Validated
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    private final UserManagementApplicationService userService;

    public UserController(UserManagementApplicationService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user account")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @BusinessOperation(value = "USER.CREATE", module = "UserManagement", logInput = true)
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.debug("Creating new user: {}", request.getUsername());
        UserResponse response = userService.createUser(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ {id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user information by ID")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "USER.GET", module = "UserManagement")
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable String id) {

        log.debug("Getting user by ID: {}", id);
        return userService.getUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/ {username}")
    @Operation(summary = "Get user by username", description = "Retrieve user information by username")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "USER.GET_BY_USERNAME", module = "UserManagement")
    public ResponseEntity<UserResponse> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {

        log.debug("Getting user by username: {}", username);
        return userService.getUserByUsername(username)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve paginated list of users")
    @PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR', 'ROLE_DEVELOPER', 'ROLE_INTEGRATOR', 'ROLE_VIEWER')")
    @BusinessOperation(value = "USER.LIST", module = "UserManagement")
    public ResponseEntity<PagedUserResponse> getUsers(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "username") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "ASC") String sortDirection) {

        log.debug("Getting users - page: {}, size: {}", page, size);
        PagedUserResponse response = userService.getUsers(page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/ {id}")
    @Operation(summary = "Update user", description = "Update user information")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @BusinessOperation(value = "USER.UPDATE", module = "UserManagement", logInput = true)
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.debug("Updating user: {}", id);
        return userService.updateUser(id, request, currentUser.getUsername())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/ {id}/password")
    @Operation(summary = "Change password", description = "Change user password")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR') or #id == authentication.principal.id")
    @BusinessOperation(value = "USER.CHANGE_PASSWORD", module = "UserManagement", logInput = false)
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "User ID") @PathVariable String id,
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.debug("Changing password for user: {}", id);
        userService.changePassword(id, request, currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/ {id}")
    @Operation(summary = "Delete user", description = "Delete a user account")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @BusinessOperation(value = "USER.DELETE", module = "UserManagement")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable String id,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.debug("Deleting user: {}", id);
        boolean deleted = userService.deleteUser(id, currentUser.getUsername());
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PutMapping("/ {id}/activate")
    @Operation(summary = "Activate user", description = "Activate a user account")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @BusinessOperation(value = "USER.ACTIVATE", module = "UserManagement")
    public ResponseEntity<Void> activateUser(
            @Parameter(description = "User ID") @PathVariable String id,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.debug("Activating user: {}", id);
        userService.activateUser(id, currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/ {id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivate a user account")
    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
    @BusinessOperation(value = "USER.DEACTIVATE", module = "UserManagement")
    public ResponseEntity<Void> deactivateUser(
            @Parameter(description = "User ID") @PathVariable String id,
            @AuthenticationPrincipal UserDetails currentUser) {

        log.debug("Deactivating user: {}", id);
        userService.deactivateUser(id, currentUser.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get the currently authenticated user")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails currentUser) {

        log.debug("Getting current user: {}", currentUser.getUsername());
        return userService.getUserByUsername(currentUser.getUsername())
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
