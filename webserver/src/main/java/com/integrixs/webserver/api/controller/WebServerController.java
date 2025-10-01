package com.integrixs.webserver.api.controller;

import com.integrixs.webserver.api.dto.*;
import com.integrixs.webserver.application.service.WebServerApplicationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for web server operations
 */
@RestController
@RequestMapping("/api/webserver")
public class WebServerController {

    private static final Logger logger = LoggerFactory.getLogger(WebServerController.class);

    private final WebServerApplicationService applicationService;

    public WebServerController(WebServerApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Execute an outbound request
     * @param request Outbound request
     * @return Response from external service
     */
    @PostMapping("/execute")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<OutboundResponseDTO> executeRequest(@Valid @RequestBody OutboundRequestDTO request) {
        logger.info("Executing outbound request to: {}", request.getTargetUrl());

        try {
            OutboundResponseDTO response = applicationService.executeRequest(request);

            if(response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }
        } catch(Exception e) {
            logger.error("Error executing request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Execute request with endpoint configuration
     * @param endpointId Endpoint ID
     * @param request Request details
     * @return Response from external service
     */
    @PostMapping("/endpoints/ {endpointId}/execute")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<OutboundResponseDTO> executeWithEndpoint(
            @PathVariable String endpointId,
            @Valid @RequestBody EndpointRequestDTO request) {
        logger.info("Executing request with endpoint: {}", endpointId);

        try {
            OutboundResponseDTO response = applicationService.executeRequestWithEndpoint(endpointId, request);

            if(response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(response);
            }
        } catch(Exception e) {
            logger.error("Error executing request with endpoint {}: {}", endpointId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Register a new service endpoint
     * @param request Endpoint registration request
     * @return Registered endpoint
     */
    @PostMapping("/endpoints")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<ServiceEndpointDTO> registerEndpoint(@Valid @RequestBody RegisterEndpointDTO request) {
        logger.info("Registering new endpoint: {}", request.getName());

        try {
            ServiceEndpointDTO endpoint = applicationService.registerEndpoint(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(endpoint);
        } catch(Exception e) {
            logger.error("Error registering endpoint: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Update service endpoint
     * @param endpointId Endpoint ID
     * @param request Update request
     * @return Updated endpoint
     */
    @PutMapping("/endpoints/ {endpointId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER')")
    public ResponseEntity<ServiceEndpointDTO> updateEndpoint(
            @PathVariable String endpointId,
            @RequestBody UpdateEndpointDTO request) {
        logger.info("Updating endpoint: {}", endpointId);

        try {
            ServiceEndpointDTO endpoint = applicationService.updateEndpoint(endpointId, request);
            return ResponseEntity.ok(endpoint);
        } catch(Exception e) {
            logger.error("Error updating endpoint {}: {}", endpointId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Get all service endpoints
     * @return List of endpoints
     */
    @GetMapping("/endpoints")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<ServiceEndpointDTO>> getAllEndpoints() {
        logger.info("Getting all endpoints");

        try {
            List<ServiceEndpointDTO> endpoints = applicationService.getAllEndpoints();
            return ResponseEntity.ok(endpoints);
        } catch(Exception e) {
            logger.error("Error getting endpoints: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get endpoint by ID
     * @param endpointId Endpoint ID
     * @return Endpoint details
     */
    @GetMapping("/endpoints/ {endpointId}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<ServiceEndpointDTO> getEndpoint(@PathVariable String endpointId) {
        logger.info("Getting endpoint: {}", endpointId);

        try {
            ServiceEndpointDTO endpoint = applicationService.getEndpoint(endpointId);
            return ResponseEntity.ok(endpoint);
        } catch(Exception e) {
            logger.error("Error getting endpoint {}: {}", endpointId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Test endpoint connectivity
     * @param endpointId Endpoint ID
     * @return Test result
     */
    @PostMapping("/endpoints/ {endpointId}/test")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    public ResponseEntity<EndpointTestResultDTO> testEndpoint(@PathVariable String endpointId) {
        logger.info("Testing endpoint connectivity: {}", endpointId);

        try {
            EndpointTestResultDTO result = applicationService.testEndpoint(endpointId);
            return ResponseEntity.ok(result);
        } catch(Exception e) {
            logger.error("Error testing endpoint {}: {}", endpointId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get request history
     * @param criteria Search criteria
     * @return List of request history
     */
    @PostMapping("/history/search")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    public ResponseEntity<List<RequestHistoryDTO>> getRequestHistory(@RequestBody RequestHistoryCriteriaDTO criteria) {
        logger.info("Searching request history with criteria: {}", criteria);

        try {
            List<RequestHistoryDTO> history = applicationService.getRequestHistory(criteria);
            return ResponseEntity.ok(history);
        } catch(Exception e) {
            logger.error("Error getting request history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("WebServer service is healthy");
    }
}
