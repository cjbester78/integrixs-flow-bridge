package com.integrixs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.security.SecurityUtils;
import com.integrixs.data.model.SystemLog;
import com.integrixs.data.sql.repository.SystemLogSqlRepository;
import com.integrixs.data.model.*;
import com.integrixs.data.sql.repository.*;
import com.integrixs.shared.dto.*;
import com.integrixs.shared.dto.business.BusinessComponentDTO;
import com.integrixs.shared.dto.certificate.CertificateDTO;
import com.integrixs.shared.dto.export.*;
import com.integrixs.shared.dto.flow.FlowTransformationDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for exporting integration flows with all dependencies.
 */
@Service
public class FlowExportService {

    private static final Logger log = LoggerFactory.getLogger(FlowExportService.class);


    private final IntegrationFlowSqlRepository integrationFlowRepository;
    private final CommunicationAdapterSqlRepository communicationAdapterRepository;
    private final BusinessComponentSqlRepository businessComponentRepository;
    private final FlowTransformationSqlRepository flowTransformationRepository;
    private final FieldMappingSqlRepository fieldMappingRepository;
    private final CertificateSqlRepository certificateRepository;
    private final SystemLogSqlRepository systemLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.version:1.0.0}")
    private String applicationVersion;

    public FlowExportService(IntegrationFlowSqlRepository integrationFlowRepository,
                            CommunicationAdapterSqlRepository communicationAdapterRepository,
                            BusinessComponentSqlRepository businessComponentRepository,
                            FlowTransformationSqlRepository flowTransformationRepository,
                            FieldMappingSqlRepository fieldMappingRepository,
                            CertificateSqlRepository certificateRepository,
                            SystemLogSqlRepository systemLogRepository,
                            ObjectMapper objectMapper) {
        this.integrationFlowRepository = integrationFlowRepository;
        this.communicationAdapterRepository = communicationAdapterRepository;
        this.businessComponentRepository = businessComponentRepository;
        this.flowTransformationRepository = flowTransformationRepository;
        this.fieldMappingRepository = fieldMappingRepository;
        this.certificateRepository = certificateRepository;
        this.systemLogRepository = systemLogRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Export a flow with all its dependencies.
     *
     * @param request Export request with options
     * @return Exported flow data
     */
    public FlowExportDTO exportFlow(FlowExportRequestDTO request) {
        log.info("Exporting flow: {}", request.getFlowId());

        // Load the flow
        IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(request.getFlowId()))
                .orElseThrow(() -> new ResourceNotFoundException("Flow not found: " + request.getFlowId()));

        // Build export
        FlowExportDTO export = FlowExportDTO.builder()
                .metadata(buildExportMetadata(request, flow))
                .flow(convertToFlowDTO(flow))
                .build();

        // Include dependencies based on options
        FlowExportRequestDTO.ExportOptions options = request.getOptions();

        // Load and include adapters
        if(options.isIncludeAdapterConfigs()) {
            export.setInboundAdapter(loadAdapter(flow.getInboundAdapterId() != null ? flow.getInboundAdapterId().toString() : null));
            export.setOutboundAdapter(loadAdapter(flow.getOutboundAdapterId() != null ? flow.getOutboundAdapterId().toString() : null));

            // Include certificate references
            if(options.isIncludeCertificateReferences()) {
                Set<String> certificateIds = new HashSet<>();
                certificateIds.addAll(extractCertificateIds(export.getInboundAdapter()));
                certificateIds.addAll(extractCertificateIds(export.getOutboundAdapter()));

                List<FlowExportDTO.CertificateReferenceDTO> certRefs = certificateIds.stream()
                        .map(this::createCertificateReference)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                export.setCertificateReferences(certRefs);
            }
        }

        // Include business component
        if(options.isIncludeBusinessComponent() && flow.getBusinessComponent() != null) {
            export.setBusinessComponent(convertToBusinessComponentDTO(flow.getBusinessComponent()));
        }

        // Load transformations and field mappings
        List<FlowTransformation> transformations = flowTransformationRepository.findByFlowId(flow.getId());
        export.setTransformations(transformations.stream()
                .map(this::convertToFlowTransformationDTO)
                .collect(Collectors.toList()));

        // Flatten field mappings for easy access
        List<FieldMappingDTO> allMappings = new ArrayList<>();
        for(FlowTransformation transformation : transformations) {
            List<FieldMapping> mappings = fieldMappingRepository.findByTransformationId(transformation.getId());
            allMappings.addAll(mappings.stream()
                    .map(this::convertToFieldMappingDTO)
                    .collect(Collectors.toList()));
        }
        export.setFieldMappings(allMappings);

        // Log the export
        logExportActivity(flow, request);

        return export;
    }

    /**
     * Validate if a flow can be exported.
     *
     * @param flowId Flow ID to validate
     * @return Validation result
     */
    public Map<String, Object> validateExport(String flowId) {
        Map<String, Object> validation = new HashMap<>();

        try {
            IntegrationFlow flow = integrationFlowRepository.findById(UUID.fromString(flowId))
                    .orElseThrow(() -> new ResourceNotFoundException("Flow not found"));

            validation.put("canExport", true);
            validation.put("flowName", flow.getName());
            validation.put("status", flow.getStatus().toString());

            // Check dependencies
            boolean hasSourceAdapter = communicationAdapterRepository.existsById(flow.getInboundAdapterId());
            boolean hasTargetAdapter = communicationAdapterRepository.existsById(flow.getOutboundAdapterId());

            validation.put("hasSourceAdapter", hasSourceAdapter);
            validation.put("hasTargetAdapter", hasTargetAdapter);

            if(!hasSourceAdapter || !hasTargetAdapter) {
                validation.put("canExport", false);
                validation.put("reason", "Missing adapter dependencies");
            }

        } catch(Exception e) {
            validation.put("canExport", false);
            validation.put("error", e.getMessage());
        }

        return validation;
    }

    private FlowExportDTO.ExportMetadata buildExportMetadata(FlowExportRequestDTO request, IntegrationFlow flow) {
        FlowExportRequestDTO.ExportOptions options = request.getOptions();

        Map<String, String> tags = new HashMap<>();
        if(options.getTags() != null) {
            options.getTags().forEach(tag -> tags.put(tag, "true"));
        }
        tags.put("flowName", flow.getName());
        tags.put("flowStatus", flow.getStatus().toString());

        return FlowExportDTO.ExportMetadata.builder()
                .exportId(UUID.randomUUID().toString())
                .exportVersion("1.0")
                .applicationVersion(applicationVersion)
                .exportDate(LocalDateTime.now())
                .exportedBy(SecurityUtils.getCurrentUserId() != null ? SecurityUtils.getCurrentUserId().toString() : null)
                .exportedByUsername(SecurityUtils.getCurrentUsernameStatic())
                .environment(request.getEnvironment())
                .description(request.getDescription())
                .tags(tags)
                .build();
    }

    private CommunicationAdapterDTO loadAdapter(String adapterId) {
        return communicationAdapterRepository.findById(UUID.fromString(adapterId))
                .map(this::convertToAdapterDTO)
                .orElse(null);
    }

    private Set<String> extractCertificateIds(CommunicationAdapterDTO adapter) {
        Set<String> certificateIds = new HashSet<>();

        if(adapter == null || adapter.getConfiguration() == null) {
            return certificateIds;
        }

        try {
            // Configuration is already a Map
            Map<String, Object> config = adapter.getConfiguration();

            // Common certificate field names
            String[] certFields = {"certificateId", "clientCertificateId", "serverCertificateId",
                                 "truststoreId", "keystoreId", "sslCertificateId"};

            for(String field : certFields) {
                if(config.containsKey(field) && config.get(field) != null) {
                    certificateIds.add(config.get(field).toString());
                }
            }

        } catch(Exception e) {
            log.warn("Failed to extract certificate IDs from adapter config: {}", e.getMessage());
        }

        return certificateIds;
    }

    private FlowExportDTO.CertificateReferenceDTO createCertificateReference(String certificateId) {
        return certificateRepository.findById(UUID.fromString(certificateId))
                .map(cert -> FlowExportDTO.CertificateReferenceDTO.builder()
                        .certificateId(cert.getId().toString())
                        .certificateName(cert.getName())
                        .certificateType(cert.getType())
                        .usedByAdapterIds("")
                        .build())
                .orElse(null);
    }

    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return Base64.getEncoder().encodeToString(hash);
        } catch(Exception e) {
            log.error("Failed to calculate checksum", e);
            return null;
        }
    }

    private void logExportActivity(IntegrationFlow flow, FlowExportRequestDTO request) {
        try {
            Map<String, Object> details = new HashMap<>();
            details.put("flowId", flow.getId());
            details.put("flowName", flow.getName());
            details.put("exportOptions", request.getOptions());

            SystemLog log = SystemLog.builder()
                    .timestamp(LocalDateTime.now())
                    .level(SystemLog.LogLevel.INFO)
                    .message("Integration flow exported")
                    .details(objectMapper.writeValueAsString(details))
                    .source("EXPORT_SERVICE")
                    .category("FLOW_EXPORT")
                    .userId(SecurityUtils.getCurrentUserId())
                    .username(SecurityUtils.getCurrentUsernameStatic())
                    .domainType("INTEGRATION_FLOW")
                    .domainReferenceId(flow.getId().toString())
                    .build();

            systemLogRepository.save(log);

        } catch(Exception e) {
            log.error("Failed to log export activity", e);
        }
    }

    // Conversion methods

    private FlowDTO convertToFlowDTO(IntegrationFlow flow) {
        return FlowDTO.builder()
                .id(flow.getId().toString())
                .name(flow.getName())
                .description(flow.getDescription())
                .inboundAdapterId(flow.getInboundAdapterId() != null ? flow.getInboundAdapterId().toString() : null)
                .outboundAdapterId(flow.getOutboundAdapterId() != null ? flow.getOutboundAdapterId().toString() : null)
                .sourceFlowStructureId(flow.getSourceFlowStructureId() != null ? flow.getSourceFlowStructureId().toString() : null)
                .targetFlowStructureId(flow.getTargetFlowStructureId() != null ? flow.getTargetFlowStructureId().toString() : null)
                .status(flow.getStatus().toString())
                // Configuration field removed - using native columns instead
                // isActive not available in FlowDTO
                .mappingMode(flow.getMappingMode() != null ? flow.getMappingMode().toString() : null)
                .createdAt(flow.getCreatedAt())
                .updatedAt(flow.getUpdatedAt())
                .createdBy(flow.getCreatedBy() != null ? flow.getCreatedBy().getUsername() : null)
                .build();
    }

    private CommunicationAdapterDTO convertToAdapterDTO(CommunicationAdapter adapter) {
        return CommunicationAdapterDTO.builder()
                .id(adapter.getId().toString())
                .name(adapter.getName())
                .type(adapter.getType().toString())
                .mode(adapter.getMode().toString())
                .configuration(parseConfiguration(adapter.getConfiguration()))
                // isActive not in CommunicationAdapterDTO
                .description(adapter.getDescription())
                .businessComponentId(adapter.getBusinessComponentId() != null ? adapter.getBusinessComponentId().toString() : null)
                .build();
    }

    private BusinessComponentDTO convertToBusinessComponentDTO(BusinessComponent component) {
        return BusinessComponentDTO.builder()
                .id(component.getId().toString())
                .name(component.getName())
                .description(component.getDescription())
                .contactEmail(component.getContactEmail())
                .contactPhone(component.getContactPhone())
                .createdAt(component.getCreatedAt())
                .updatedAt(component.getUpdatedAt())
                .build();
    }

    private FlowTransformationDTO convertToFlowTransformationDTO(FlowTransformation transformation) {
        return FlowTransformationDTO.builder()
                .id(transformation.getId().toString())
                .flowId(transformation.getFlow().getId().toString())
                .name(transformation.getName())
                .description(transformation.getDescription())
                .type(transformation.getType().name())
                .configuration(transformation.getConfiguration())
                .executionOrder(transformation.getExecutionOrder())
                .isActive(transformation.isActive())
                .createdAt(transformation.getCreatedAt())
                .updatedAt(transformation.getUpdatedAt())
                .build();
    }

    private FieldMappingDTO convertToFieldMappingDTO(FieldMapping mapping) {
        return FieldMappingDTO.builder()
                .id(mapping.getId().toString())
                .transformationId(mapping.getTransformation().getId().toString())
                .sourceFields(mapping.getSourceFieldsList())
                .targetField(mapping.getTargetField())
                .javaFunction(mapping.getJavaFunction())
                .mappingRule(mapping.getMappingRule())
                .sourceXPath(mapping.getSourceXPath())
                .targetXPath(mapping.getTargetXPath())
                .isArrayMapping(mapping.isArrayMapping())
                .arrayContextPath(mapping.getArrayContextPath())
                .namespaceAware(mapping.isNamespaceAware())
                .inputTypes(mapping.getInputTypes())
                .outputType(mapping.getOutputType())
                .description(mapping.getDescription())
                .version(mapping.getVersion())
                .functionName(mapping.getFunctionName())
                .isActive(mapping.isActive())
                .createdAt(mapping.getCreatedAt())
                .updatedAt(mapping.getUpdatedAt())
                .build();
    }

    private Map<String, Object> parseConfiguration(String configJson) {
        if(configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson,
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class));
        } catch(Exception e) {
            log.warn("Failed to parse configuration JSON: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
