package com.integrixs.backend.domain.service;

import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.IntegrationFlow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Domain service for flow transformation business logic
 */
@Service
public class FlowTransformationManagementService {

    /**
     * Validate transformation data
     * @param transformation The transformation to validate
     * @param flow The integration flow
     * @throws IllegalArgumentException if validation fails
     */

    private static final Logger log = LoggerFactory.getLogger(FlowTransformationManagementService.class);

    public void validateTransformation(FlowTransformation transformation, IntegrationFlow flow) {
        if(transformation == null) {
            throw new IllegalArgumentException("Transformation cannot be null");
        }

        if(flow == null) {
            throw new IllegalArgumentException("Flow cannot be null");
        }

        if(transformation.getName() == null || transformation.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Transformation name is required");
        }

        if(transformation.getType() == null) {
            throw new IllegalArgumentException("Transformation type is required");
        }

        if(transformation.getExecutionOrder() < 1) {
            throw new IllegalArgumentException("Execution order must be at least 1");
        }

        // Validate configuration based on type
        validateConfigurationForType(transformation);
    }

    /**
     * Validate configuration based on transformation type
     * @param transformation The transformation to validate
     */
    private void validateConfigurationForType(FlowTransformation transformation) {
        switch(transformation.getType()) {
            case FIELD_MAPPING:
                // Field mappings are stored separately, configuration can be empty
                break;

            case CUSTOM_FUNCTION:
                if(transformation.getConfiguration() == null || transformation.getConfiguration().trim().isEmpty()) {
                    throw new IllegalArgumentException("Configuration is required for custom function transformation");
                }
                break;

            case FILTER:
                if(transformation.getConfiguration() == null || transformation.getConfiguration().trim().isEmpty()) {
                    throw new IllegalArgumentException("Configuration is required for filter transformation");
                }
                break;

            case ENRICHMENT:
                if(transformation.getConfiguration() == null || transformation.getConfiguration().trim().isEmpty()) {
                    throw new IllegalArgumentException("Configuration is required for enrichment transformation");
                }
                break;

            case VALIDATION:
                if(transformation.getConfiguration() == null || transformation.getConfiguration().trim().isEmpty()) {
                    throw new IllegalArgumentException("Configuration is required for validation transformation");
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported transformation type: " + transformation.getType());
        }
    }

    /**
     * Check if a transformation name is unique within a flow
     * @param flowId The flow ID
     * @param name The transformation name
     * @param excludeId The ID to exclude(for updates)
     * @param existingTransformations List of existing transformations
     * @return true if the name is unique
     */
    public boolean isTransformationNameUnique(UUID flowId, String name, UUID excludeId,
                                            List<FlowTransformation> existingTransformations) {
        return existingTransformations.stream()
            .filter(t -> t.getFlow().getId().equals(flowId))
            .filter(t -> !t.getId().equals(excludeId))
            .noneMatch(t -> t.getName().equalsIgnoreCase(name));
    }

    /**
     * Check if an execution order is unique within a flow
     * @param flowId The flow ID
     * @param executionOrder The execution order
     * @param excludeId The ID to exclude(for updates)
     * @param existingTransformations List of existing transformations
     * @return true if the execution order is unique
     */
    public boolean isExecutionOrderUnique(UUID flowId, Integer executionOrder, UUID excludeId,
                                        List<FlowTransformation> existingTransformations) {
        return existingTransformations.stream()
            .filter(t -> t.getFlow().getId().equals(flowId))
            .filter(t -> !t.getId().equals(excludeId))
            .noneMatch(t -> t.getExecutionOrder() == executionOrder);
    }

    /**
     * Prepare transformation for creation
     * @param transformation The transformation to prepare
     * @param flow The integration flow
     */
    public void prepareForCreation(FlowTransformation transformation, IntegrationFlow flow) {
        transformation.setFlow(flow);

        // isActive is a primitive boolean, defaults to true

        log.debug("Prepared transformation ' {}' for creation in flow ' {}'",
            transformation.getName(), flow.getName());
    }

    /**
     * Prepare transformation for update
     * @param existingTransformation The existing transformation
     * @param updatedData The updated transformation data
     */
    public void prepareForUpdate(FlowTransformation existingTransformation, FlowTransformation updatedData) {
        existingTransformation.setName(updatedData.getName());
        existingTransformation.setType(updatedData.getType());
        existingTransformation.setConfiguration(updatedData.getConfiguration());
        existingTransformation.setExecutionOrder(updatedData.getExecutionOrder());
        existingTransformation.setActive(updatedData.isActive());

        log.debug("Prepared transformation ' {}' for update", existingTransformation.getName());
    }

    /**
     * Can delete transformation
     * @param transformation The transformation to check
     * @return true if can be deleted
     */
    public boolean canDeleteTransformation(FlowTransformation transformation) {
        // Could add checks here for dependencies, active flows, etc.
        return true;
    }

    /**
     * Get transformation type display name
     * @param type The transformation type
     * @return Display name
     */
    public String getTransformationTypeDisplayName(FlowTransformation.TransformationType type) {
        switch(type) {
            case FIELD_MAPPING:
                return "Field Mapping";
            case CUSTOM_FUNCTION:
                return "Custom Function";
            case FILTER:
                return "Filter";
            case ENRICHMENT:
                return "Enrichment";
            case VALIDATION:
                return "Validation";
            default:
                return type.name();
        }
    }
}
