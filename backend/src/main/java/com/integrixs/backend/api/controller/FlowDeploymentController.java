package com.integrixs.backend.api.controller;

import com.integrixs.backend.api.dto.response.DeploymentInfoResponse;
import com.integrixs.backend.application.service.FlowDeploymentApplicationService;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.UserSqlRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for flow deployment management
 */
@RestController
@RequestMapping("/api/flows/ {flowId}/deployment")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Flow Deployment", description = "Flow deployment management")
public class FlowDeploymentController {

    private static final Logger log = LoggerFactory.getLogger(FlowDeploymentController.class);


    private final FlowDeploymentApplicationService deploymentService;
    private final UserSqlRepository userRepository;

    public FlowDeploymentController(FlowDeploymentApplicationService deploymentService,
                                   UserSqlRepository userRepository) {
        this.deploymentService = deploymentService;
        this.userRepository = userRepository;
    }

    /**
     * Deploy a flow
     */
    @PostMapping("/deploy")
    @Operation(summary = "Deploy an integration flow")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<?> deployFlow(@PathVariable String flowId) {
        try {
            log.info("Deploy flow request for flowId: {}", flowId);

            String username = SecurityUtils.getCurrentUsernameStatic();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            DeploymentInfoResponse deploymentInfo = deploymentService.deployFlow(flowId, currentUser);

            log.info("Deployment successful for flowId: {}", flowId);
            return ResponseEntity.ok(deploymentInfo);

        } catch(IllegalStateException e) {
            log.error("IllegalStateException during deployment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch(Exception e) {
            log.error("Exception during deployment for flowId {}: {}", flowId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Failed to deploy flow",
                    "type", e.getClass().getSimpleName(),
                    "flowId", flowId
               ));
        }
    }

    /**
     * Undeploy a flow
     */
    @PostMapping("/undeploy")
    @Operation(summary = "Undeploy an integration flow")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<?> undeployFlow(@PathVariable String flowId) {
        try {
            log.info("Undeploy flow request for flowId: {}", flowId);

            String username = SecurityUtils.getCurrentUsernameStatic();
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

            deploymentService.undeployFlow(flowId, currentUser);

            log.info("Undeploy successful for flowId: {}", flowId);
            return ResponseEntity.ok().body(Map.of(
                "message", "Flow undeployed successfully",
                "flowId", flowId
           ));

        } catch(IllegalStateException e) {
            log.error("IllegalStateException during undeployment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch(Exception e) {
            log.error("Exception during undeployment for flowId {}: {}", flowId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Failed to undeploy flow",
                    "flowId", flowId
               ));
        }
    }

    /**
     * Get deployment information
     */
    @GetMapping
    @Operation(summary = "Get deployment information for a flow")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<?> getDeploymentInfo(@PathVariable String flowId) {
        try {
            log.debug("Get deployment info request for flowId: {}", flowId);

            DeploymentInfoResponse deploymentInfo = deploymentService.getDeploymentInfo(flowId);

            if(deploymentInfo != null) {
                return ResponseEntity.ok(deploymentInfo);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch(Exception e) {
            log.error("Exception getting deployment info for flowId {}: {}", flowId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Failed to get deployment info",
                    "flowId", flowId
               ));
        }
    }
}
