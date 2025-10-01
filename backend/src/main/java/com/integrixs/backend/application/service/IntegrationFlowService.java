package com.integrixs.backend.application.service;

import com.integrixs.backend.annotation.AuditCreate;
import com.integrixs.backend.annotation.AuditDelete;
import com.integrixs.backend.annotation.AuditUpdate;
import com.integrixs.backend.api.dto.request.CreateFlowRequest;
import com.integrixs.backend.api.dto.request.UpdateFlowRequest;
import com.integrixs.backend.api.dto.response.FlowResponse;
import com.integrixs.backend.domain.service.FlowValidationService;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.AuditTrail;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.sql.repository.FlowStructureSqlRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for integration flow use cases
 */
@Service
public class IntegrationFlowService {

    private static final Logger log = LoggerFactory.getLogger(IntegrationFlowService.class);

    private final IntegrationFlowSqlRepository flowRepository;
    private final FlowValidationService validationService;
    private final AuditTrailService auditTrailService;
    private final CommunicationAdapterSqlRepository adapterRepository;
    private final FlowStructureSqlRepository structureRepository;

    public IntegrationFlowService(IntegrationFlowSqlRepository flowRepository,
                                FlowValidationService validationService,
                                AuditTrailService auditTrailService,
                                CommunicationAdapterSqlRepository adapterRepository,
                                FlowStructureSqlRepository structureRepository) {
        this.flowRepository = flowRepository;
        this.validationService = validationService;
        this.auditTrailService = auditTrailService;
        this.adapterRepository = adapterRepository;
        this.structureRepository = structureRepository;
    }

    public List<FlowResponse> getAllFlows() {
        return flowRepository.findAll().stream()
                .map(this::toFlowResponse)
                .collect(Collectors.toList());
    }

    public FlowResponse getFlowById(String id) {
        UUID flowId = UUID.fromString(id);
        return flowRepository.findById(flowId)
                .map(this::toFlowResponse)
                .orElseThrow(() -> new RuntimeException("Flow not found: " + id));
    }

    @AuditCreate
    public FlowResponse createFlow(CreateFlowRequest request) {
        log.debug("Creating flow with name: {}", request.getName());

        // Validate name uniqueness
        validationService.validateFlowNameUniqueness(request.getName(), null);

        // Create new flow
        IntegrationFlow flow = new IntegrationFlow();
        flow.setName(request.getName());
        flow.setDescription(request.getDescription());
        flow.setActive(request.isActive());
        flow.setStatus(FlowStatus.DRAFT);
        flow.setExecutionCount(0);
        flow.setSuccessCount(0);
        flow.setErrorCount(0);

        // Set adapters if provided
        if(request.getInboundAdapterId() != null) {
            flow.setInboundAdapterId(UUID.fromString(request.getInboundAdapterId()));
        }
        if(request.getOutboundAdapterId() != null) {
            flow.setOutboundAdapterId(UUID.fromString(request.getOutboundAdapterId()));
        }

        // Set structures if provided
        if(request.getSourceFlowStructureId() != null) {
            flow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
        }
        if(request.getTargetFlowStructureId() != null) {
            flow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
        }

        // Validate flow
        validationService.validateFlow(flow);

        // Save and audit
        IntegrationFlow saved = flowRepository.save(flow);

        Map<String, Object> details = new HashMap<>();
        details.put("flowName", saved.getName());
        details.put("flowId", saved.getId().toString());
        auditTrailService.logAction("IntegrationFlow", saved.getId().toString(),
                AuditTrail.AuditAction.CREATE, details);

        log.info("Created flow: {} with ID: {}", saved.getName(), saved.getId());
        return toFlowResponse(saved);
    }

    @AuditUpdate
    public FlowResponse updateFlow(String id, UpdateFlowRequest request) {
        UUID flowId = UUID.fromString(id);
        log.debug("Updating flow: {}", flowId);

        IntegrationFlow flow = flowRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow not found: " + id));

        // Validate name uniqueness if changed
        if(!flow.getName().equals(request.getName())) {
            validationService.validateFlowNameUniqueness(request.getName(), flowId);
        }

        // Update fields
        flow.setName(request.getName());
        flow.setDescription(request.getDescription());
        flow.setActive(request.isActive());

        // Update adapters
        if(request.getInboundAdapterId() != null) {
            flow.setInboundAdapterId(UUID.fromString(request.getInboundAdapterId()));
        } else {
            flow.setInboundAdapterId(null);
        }

        if(request.getOutboundAdapterId() != null) {
            flow.setOutboundAdapterId(UUID.fromString(request.getOutboundAdapterId()));
        } else {
            flow.setOutboundAdapterId(null);
        }

        // Update structures
        if(request.getSourceFlowStructureId() != null) {
            flow.setSourceFlowStructureId(UUID.fromString(request.getSourceFlowStructureId()));
        } else {
            flow.setSourceFlowStructureId(null);
        }

        if(request.getTargetFlowStructureId() != null) {
            flow.setTargetFlowStructureId(UUID.fromString(request.getTargetFlowStructureId()));
        } else {
            flow.setTargetFlowStructureId(null);
        }

        // Validate flow
        validationService.validateFlow(flow);

        if(flow.isActive()) {
            validationService.validateFlowActivation(flow);
        }

        // Save and audit
        IntegrationFlow updated = flowRepository.save(flow);

        Map<String, Object> details = new HashMap<>();
        details.put("flowName", updated.getName());
        details.put("changes", "Updated flow configuration");
        auditTrailService.logAction("IntegrationFlow", updated.getId().toString(),
                AuditTrail.AuditAction.UPDATE, details);

        log.info("Updated flow: {} with ID: {}", updated.getName(), updated.getId());
        return toFlowResponse(updated);
    }

    @AuditDelete
    public void deleteFlow(String id) {
        UUID flowId = UUID.fromString(id);
        log.debug("Deleting flow: {}", flowId);

        IntegrationFlow flow = flowRepository.findById(flowId)
                .orElseThrow(() -> new RuntimeException("Flow not found: " + id));

        Map<String, Object> details = new HashMap<>();
        details.put("flowName", flow.getName());
        auditTrailService.logAction("IntegrationFlow", flow.getId().toString(),
                AuditTrail.AuditAction.DELETE, details);

        flowRepository.deleteById(flowId);

        log.info("Deleted flow: {} with ID: {}", flow.getName(), flowId);
    }

    private FlowResponse toFlowResponse(IntegrationFlow flow) {
        FlowResponse.FlowResponseBuilder builder = FlowResponse.builder()
                .id(flow.getId().toString())
                .name(flow.getName())
                .description(flow.getDescription())
                .status(flow.getStatus().name())
                .active(flow.isActive())
                .executionCount(flow.getExecutionCount())
                .successCount(flow.getSuccessCount())
                .errorCount(flow.getErrorCount())
                .lastExecutionAt(flow.getLastExecutionAt())
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt())
                .createdBy(flow.getCreatedBy() != null ? flow.getCreatedBy().getUsername() : null);

        // Add adapter information
        if(flow.getInboundAdapterId() != null) {
            builder.inboundAdapterId(flow.getInboundAdapterId().toString());
            adapterRepository.findById(flow.getInboundAdapterId()).ifPresent(adapter -> {
                builder.inboundAdapterName(adapter.getName())
                       .inboundAdapterType(adapter.getType().name());
            });
        }

        if(flow.getOutboundAdapterId() != null) {
            builder.outboundAdapterId(flow.getOutboundAdapterId().toString());
            adapterRepository.findById(flow.getOutboundAdapterId()).ifPresent(adapter -> {
                builder.outboundAdapterName(adapter.getName())
                       .outboundAdapterType(adapter.getType().name());
            });
        }

        // Add structure information
        if(flow.getSourceFlowStructureId() != null) {
            builder.sourceFlowStructureId(flow.getSourceFlowStructureId().toString());
            structureRepository.findById(flow.getSourceFlowStructureId()).ifPresent(structure -> {
                builder.sourceFlowStructureName(structure.getName());
            });
        }

        if(flow.getTargetFlowStructureId() != null) {
            builder.targetFlowStructureId(flow.getTargetFlowStructureId().toString());
            structureRepository.findById(flow.getTargetFlowStructureId()).ifPresent(structure -> {
                builder.targetFlowStructureName(structure.getName());
            });
        }

        return builder.build();
    }

    public IntegrationFlow createIntegrationFlow(IntegrationFlow flow) {
        // Save the integration flow
        return flowRepository.save(flow);
    }

    public void deleteIntegrationFlow(UUID flowId) {
        // Delete the integration flow
        flowRepository.deleteById(flowId);
    }
}
