package com.integrixs.backend.application.service;

import com.integrixs.data.sql.repository.FlowTransformationSqlRepository;
import com.integrixs.data.sql.repository.IntegrationFlowSqlRepository;
import com.integrixs.backend.domain.service.FlowTransformationManagementService;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.shared.dto.flow.FlowTransformationDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for flow transformation operations
 */
@Service
public class FlowTransformationApplicationService {

    private static final Logger log = LoggerFactory.getLogger(FlowTransformationApplicationService.class);


    private final FlowTransformationManagementService transformationManagementService;
    private final FlowTransformationSqlRepository transformationRepository;
    private final IntegrationFlowSqlRepository flowRepository;

    public FlowTransformationApplicationService(FlowTransformationManagementService transformationManagementService,
                                              FlowTransformationSqlRepository transformationRepository,
                                              IntegrationFlowSqlRepository flowRepository) {
        this.transformationManagementService = transformationManagementService;
        this.transformationRepository = transformationRepository;
        this.flowRepository = flowRepository;
    }

    /**
     * Get all transformations for a flow
     * @param flowId The flow ID
     * @return List of transformation DTOs
     */
    public List<FlowTransformationDTO> getByFlowId(String flowId) {
        UUID flowUuid = UUID.fromString(flowId);
        List<FlowTransformation> transformations = transformationRepository.findByFlowIdOrderByExecutionOrder(flowUuid);

        log.debug("Found {} transformations for flow {}", transformations.size(), flowId);

        return transformations.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get transformation by ID
     * @param id The transformation ID
     * @return Optional transformation DTO
     */
    public Optional<FlowTransformationDTO> getById(String id) {
        UUID transformationId = UUID.fromString(id);
        return transformationRepository.findById(transformationId)
            .map(this::toDTO);
    }

    /**
     * Create or update a transformation
     * @param dto The transformation DTO
     * @return Saved transformation DTO
     */
    public FlowTransformationDTO save(FlowTransformationDTO dto) {
        // Get the flow
        UUID flowId = UUID.fromString(dto.getFlowId());
        IntegrationFlow flow = flowRepository.findById(flowId)
            .orElseThrow(() -> new IllegalArgumentException("Flow not found: " + flowId));

        FlowTransformation transformation;

        if(dto.getId() != null) {
            // Update existing
            UUID transformationId = UUID.fromString(dto.getId());
            transformation = transformationRepository.findById(transformationId)
                .orElseThrow(() -> new IllegalArgumentException("Transformation not found: " + transformationId));

            // Create updated data
            FlowTransformation updatedData = fromDTO(dto);

            // Validate
            transformationManagementService.validateTransformation(updatedData, flow);

            // Check name uniqueness
            List<FlowTransformation> existingTransformations = transformationRepository.findByFlowId(flowId);
            if(!transformationManagementService.isTransformationNameUnique(
                    flowId, updatedData.getName(), transformationId, existingTransformations)) {
                throw new IllegalArgumentException("Transformation name already exists in flow");
            }

            // Check execution order uniqueness
            if(!transformationManagementService.isExecutionOrderUnique(
                    flowId, updatedData.getExecutionOrder(), transformationId, existingTransformations)) {
                throw new IllegalArgumentException("Execution order already exists in flow");
            }

            // Update
            transformationManagementService.prepareForUpdate(transformation, updatedData);

        } else {
            // Create new
            transformation = fromDTO(dto);

            // Validate
            transformationManagementService.validateTransformation(transformation, flow);

            // Check name uniqueness
            List<FlowTransformation> existingTransformations = transformationRepository.findByFlowId(flowId);
            if(!transformationManagementService.isTransformationNameUnique(
                    flowId, transformation.getName(), null, existingTransformations)) {
                throw new IllegalArgumentException("Transformation name already exists in flow");
            }

            // Check execution order uniqueness
            if(!transformationManagementService.isExecutionOrderUnique(
                    flowId, transformation.getExecutionOrder(), null, existingTransformations)) {
                throw new IllegalArgumentException("Execution order already exists in flow");
            }

            // Prepare for creation
            transformationManagementService.prepareForCreation(transformation, flow);
        }

        // Save
        FlowTransformation saved = transformationRepository.save(transformation);
        log.info("Saved transformation ' {}' for flow ' {}'", saved.getName(), flow.getName());

        return toDTO(saved);
    }

    /**
     * Delete a transformation
     * @param id The transformation ID
     */
    public void delete(String id) {
        UUID transformationId = UUID.fromString(id);

        FlowTransformation transformation = transformationRepository.findById(transformationId)
            .orElseThrow(() -> new IllegalArgumentException("Transformation not found: " + transformationId));

        if(!transformationManagementService.canDeleteTransformation(transformation)) {
            throw new IllegalStateException("Cannot delete transformation: " + transformation.getName());
        }

        transformationRepository.deleteById(transformationId);
        log.info("Deleted transformation: {}", transformation.getName());
    }

    /**
     * Delete all transformations for a flow
     * @param flowId The flow ID
     */
    public void deleteByFlowId(String flowId) {
        UUID flowUuid = UUID.fromString(flowId);
        transformationRepository.deleteByFlowId(flowUuid);
        log.info("Deleted all transformations for flow: {}", flowId);
    }

    /**
     * Count transformations for a flow
     * @param flowId The flow ID
     * @return Count of transformations
     */
    public long countByFlowId(String flowId) {
        UUID flowUuid = UUID.fromString(flowId);
        return transformationRepository.countByFlowId(flowUuid);
    }

    /**
     * Convert entity to DTO
     */
    private FlowTransformationDTO toDTO(FlowTransformation transformation) {
        FlowTransformationDTO dto = new FlowTransformationDTO();
        dto.setId(transformation.getId() != null ? transformation.getId().toString() : null);
        dto.setFlowId(transformation.getFlow() != null ? transformation.getFlow().getId().toString() : null);
        dto.setType(transformation.getType().toString());
        dto.setName(transformation.getName());
        dto.setConfiguration(transformation.getConfiguration());
        dto.setExecutionOrder(transformation.getExecutionOrder());
        dto.setActive(transformation.isActive());
        dto.setCreatedAt(transformation.getCreatedAt());
        dto.setUpdatedAt(transformation.getUpdatedAt());
        // Note: fieldMappings would be populated separately if needed
        return dto;
    }

    /**
     * Convert DTO to entity
     */
    private FlowTransformation fromDTO(FlowTransformationDTO dto) {
        FlowTransformation transformation = new FlowTransformation();

        if(dto.getId() != null) {
            transformation.setId(UUID.fromString(dto.getId()));
        }

        transformation.setType(FlowTransformation.TransformationType.valueOf(dto.getType()));
        transformation.setName(dto.getName());
        transformation.setConfiguration(dto.getConfiguration());
        transformation.setExecutionOrder(dto.getExecutionOrder());
        transformation.setActive(dto.isActive());

        return transformation;
    }
}
