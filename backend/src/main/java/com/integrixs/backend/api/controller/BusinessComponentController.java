package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.request.CreateBusinessComponentRequest;
import com.integrixs.backend.api.dto.request.UpdateBusinessComponentRequest;
import com.integrixs.backend.api.dto.response.BusinessComponentResponse;
import com.integrixs.backend.application.service.BusinessComponentApplicationService;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for business component management
 */
@RestController
@RequestMapping("/api/business-components")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Business Components", description = "Business component management")
public class BusinessComponentController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BusinessComponentController.class);

    private final BusinessComponentApplicationService businessComponentService;
    private final UserSqlRepository userRepository;

    public BusinessComponentController(BusinessComponentApplicationService businessComponentService,
                                      UserSqlRepository userRepository) {
        this.businessComponentService = businessComponentService;
        this.userRepository = userRepository;
    }

    /**
     * Create a new business component
     */
    @PostMapping
    @Operation(summary = "Create a new business component")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<BusinessComponentResponse> createBusinessComponent(
            @Valid @RequestBody CreateBusinessComponentRequest request) {
        log.info("Creating business component: {}", request.getName());

        String username = SecurityUtils.getCurrentUsernameStatic();
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        BusinessComponentResponse response = businessComponentService.createBusinessComponent(request, currentUser);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all business components
     */
    @GetMapping
    @Operation(summary = "Get all business components")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<BusinessComponentResponse>> getAllBusinessComponents() {
        log.debug("Fetching all business components");

        List<BusinessComponentResponse> components = businessComponentService.getAllBusinessComponents();

        return ResponseEntity.ok(components);
    }

    /**
     * Get business component by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get business component by ID")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<BusinessComponentResponse> getBusinessComponentById(@PathVariable String id) {
        log.debug("Fetching business component: {}", id);

        BusinessComponentResponse component = businessComponentService.getBusinessComponentById(id);

        return ResponseEntity.ok(component);
    }

    /**
     * Update business component
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update business component")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<BusinessComponentResponse> updateBusinessComponent(
            @PathVariable String id,
            @Valid @RequestBody UpdateBusinessComponentRequest request) {
        log.info("Updating business component: {}", id);

        String username = SecurityUtils.getCurrentUsernameStatic();
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        BusinessComponentResponse response = businessComponentService.updateBusinessComponent(id, request, currentUser);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete business component
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete business component")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Void> deleteBusinessComponent(@PathVariable String id) {
        log.info("Deleting business component: {}", id);

        String username = SecurityUtils.getCurrentUsernameStatic();
        User currentUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        businessComponentService.deleteBusinessComponent(id, currentUser);

        return ResponseEntity.noContent().build();
    }
}
