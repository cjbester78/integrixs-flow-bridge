package com.integrixs.backend.controller.v1;

import com.integrixs.backend.config.ApiVersioningConfig.ApiV1;
import com.integrixs.backend.domain.services.FlowDomainService;
import com.integrixs.backend.service.FlowCompositionService;
import com.integrixs.backend.application.service.IntegrationFlowService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.shared.dto.flow.FlowCreateRequestDTO;
import com.integrixs.shared.dto.flow.IntegrationFlowDTO;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageImpl;
import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST controller for integration flows - API Version 1.
 *
 * <p>Provides versioned endpoints for flow management.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/flows")
@ApiV1 // This annotation marks it for v1 versioning
@CrossOrigin(origins = "*", maxAge = 3600)
public class FlowControllerV1 {

    private static final Logger log = LoggerFactory.getLogger(FlowControllerV1.class);


    private final FlowCompositionService flowCompositionService;
    private final IntegrationFlowService integrationFlowService;
    private final FlowDomainService flowDomainService;

    public FlowControllerV1(FlowCompositionService flowCompositionService,
                            IntegrationFlowService integrationFlowService,
                            FlowDomainService flowDomainService) {
        this.flowCompositionService = flowCompositionService;
        this.integrationFlowService = integrationFlowService;
        this.flowDomainService = flowDomainService;
    }

    /**
     * Gets all integration flows with pagination.
     *
     * @param pageable pagination parameters
     * @return page of flows
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Timed(value = "api.flows.list", description = "Time taken to list flows")
    public ResponseEntity<Page<IntegrationFlowDTO>> getAllFlows(Pageable pageable) {
        log.debug("GET /api/v1/flows - page: {}, size: {}",
                 pageable.getPageNumber(), pageable.getPageSize());

        // Convert list to page manually
        List<com.integrixs.backend.api.dto.response.FlowResponse> allFlows = integrationFlowService.getAllFlows();
        List<IntegrationFlowDTO> flowDTOs = allFlows.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());

        // Simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), flowDTOs.size());

        Page<IntegrationFlowDTO> flows = new PageImpl<>(
            flowDTOs.subList(start, end),
            pageable,
            flowDTOs.size()
       );
        return ResponseEntity.ok(flows);
    }

    /**
     * Gets a specific integration flow by ID.
     *
     * @param id the flow ID
     * @return the flow
     */
    @GetMapping("/ {id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR', 'VIEWER')")
    @Timed(value = "api.flows.get", description = "Time taken to get flow")
    public ResponseEntity<IntegrationFlowDTO> getFlow(@PathVariable String id) {
        log.debug("GET /api/v1/flows/ {}", id);

        try {
            com.integrixs.backend.api.dto.response.FlowResponse flow = integrationFlowService.getFlowById(id);
            return ResponseEntity.ok(mapToDTO(flow));
        } catch(Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Creates a new integration flow.
     *
     * @param request the flow creation request
     * @param userDetails the authenticated user
     * @return created flow
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Timed(value = "api.flows.create", description = "Time taken to create flow")
    public ResponseEntity<IntegrationFlowDTO> createFlow(
            @Valid @RequestBody FlowCreateRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("POST /api/v1/flows - Creating flow: {} by user: {}",
                 request.getName(), userDetails.getUsername());

        // Convert FlowCreateRequestDTO to CreateFlowRequest
        com.integrixs.backend.api.dto.request.CreateFlowRequest createRequest =
            new com.integrixs.backend.api.dto.request.CreateFlowRequest();
        // Note: Proper mapping needed here

        com.integrixs.backend.api.dto.response.FlowResponse created =
            integrationFlowService.createFlow(createRequest);
        IntegrationFlowDTO dto = mapToDTO(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Activates an integration flow.
     *
     * @param id the flow ID
     * @param userDetails the authenticated user
     * @return activated flow
     */
    @PutMapping("/ {id}/activate")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Timed(value = "api.flows.activate", description = "Time taken to activate flow")
    public ResponseEntity<IntegrationFlowDTO> activateFlow(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("PUT /api/v1/flows/ {}/activate - User: {}", id, userDetails.getUsername());

        IntegrationFlow activated = flowDomainService.activateFlow(id, userDetails.getUsername());
        // Convert IntegrationFlow entity to FlowResponse
        com.integrixs.backend.api.dto.response.FlowResponse flowResponse = convertEntityToResponse(activated);
        IntegrationFlowDTO dto = mapToDTO(flowResponse);

        return ResponseEntity.ok(dto);
    }

    /**
     * Deactivates an integration flow.
     *
     * @param id the flow ID
     * @param reason the deactivation reason
     * @param userDetails the authenticated user
     * @return deactivated flow
     */
    @PutMapping("/ {id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR', 'DEVELOPER', 'INTEGRATOR')")
    @Timed(value = "api.flows.deactivate", description = "Time taken to deactivate flow")
    public ResponseEntity<IntegrationFlowDTO> deactivateFlow(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "Manual deactivation") String reason,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("PUT /api/v1/flows/ {}/deactivate - User: {}, Reason: {}",
                 id, userDetails.getUsername(), reason);

        IntegrationFlow deactivated = flowDomainService.deactivateFlow(
            id, userDetails.getUsername(), reason);
        // Convert IntegrationFlow entity to FlowResponse
        com.integrixs.backend.api.dto.response.FlowResponse flowResponse = convertEntityToResponse(deactivated);
        IntegrationFlowDTO dto = mapToDTO(flowResponse);

        return ResponseEntity.ok(dto);
    }

    /**
     * Deletes an integration flow.
     *
     * @param id the flow ID
     * @param userDetails the authenticated user
     * @return no content
     */
    @DeleteMapping("/ {id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Timed(value = "api.flows.delete", description = "Time taken to delete flow")
    public ResponseEntity<Void> deleteFlow(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.warn("DELETE /api/v1/flows/ {} - User: {}", id, userDetails.getUsername());

        integrationFlowService.deleteFlow(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Maps DTO to entity.
     *
     * @param dto the DTO
     * @return the entity
     */
    private IntegrationFlow mapToEntity(FlowCreateRequestDTO dto) {
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(dto.getName());
        flow.setDescription(dto.getDescription());
        flow.setInboundAdapterId(UUID.fromString(dto.getInboundAdapterId()));
        flow.setOutboundAdapterId(UUID.fromString(dto.getOutboundAdapterId()));
        // Configuration field removed - using native columns instead
        return flow;
    }

    /**
     * Convert IntegrationFlow entity to FlowResponse.
     *
     * @param flow the entity
     * @return the response DTO
     */
    private com.integrixs.backend.api.dto.response.FlowResponse convertEntityToResponse(IntegrationFlow flow) {
        com.integrixs.backend.api.dto.response.FlowResponse response = new com.integrixs.backend.api.dto.response.FlowResponse();
        response.setId(flow.getId().toString());
        response.setName(flow.getName());
        response.setDescription(flow.getDescription());
        response.setInboundAdapterId(flow.getInboundAdapterId().toString());
        response.setOutboundAdapterId(flow.getOutboundAdapterId().toString());
        response.setSourceFlowStructureId(flow.getSourceFlowStructureId() != null ? flow.getSourceFlowStructureId().toString() : null);
        response.setTargetFlowStructureId(flow.getTargetFlowStructureId() != null ? flow.getTargetFlowStructureId().toString() : null);
        response.setStatus(flow.getStatus().toString());
        response.setActive(flow.isActive());
        response.setCreatedAt(flow.getCreatedAt());
        response.setUpdatedAt(flow.getUpdatedAt());
        response.setCreatedBy(flow.getCreatedBy() != null ? flow.getCreatedBy().getUsername() : null);
        // Note: getMappingMode() might not be available in FlowResponse
        return response;
    }

    /**
     * Maps entity to DTO.
     *
     * @param flow the entity
     * @return the DTO
     */
    private IntegrationFlowDTO mapToDTO(com.integrixs.backend.api.dto.response.FlowResponse flow) {
        return IntegrationFlowDTO.builder()
            .id(flow.getId().toString())
            .name(flow.getName())
            .description(flow.getDescription())
            .inboundAdapterId(flow.getInboundAdapterId() != null ? flow.getInboundAdapterId().toString() : null)
            .outboundAdapterId(flow.getOutboundAdapterId() != null ? flow.getOutboundAdapterId().toString() : null)
            .sourceFlowStructureId(flow.getSourceFlowStructureId() != null ? flow.getSourceFlowStructureId().toString() : null)
            .targetFlowStructureId(flow.getTargetFlowStructureId() != null ? flow.getTargetFlowStructureId().toString() : null)
            .status(flow.getStatus().toString())
            .mappingMode(null) // Mapping mode not available in FlowResponse
            .isActive(flow.isActive())
            .createdAt(flow.getCreatedAt())
            .updatedAt(flow.getUpdatedAt())
            .createdBy(flow.getCreatedBy())
            .build();
    }
}
