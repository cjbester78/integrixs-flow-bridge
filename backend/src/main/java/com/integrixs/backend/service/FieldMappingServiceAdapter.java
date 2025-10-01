package com.integrixs.backend.service;

import com.integrixs.backend.api.dto.request.CreateFieldMappingRequest;
import com.integrixs.backend.application.service.FieldMappingApplicationService;
import com.integrixs.data.model.User;
import com.integrixs.data.sql.repository.UserSqlRepository;
import com.integrixs.shared.dto.FieldMappingDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service adapter to bridge between FlowCompositionService(using DTOs)
 * and FieldMappingApplicationService(using domain requests)
 */
@Service
public class FieldMappingServiceAdapter {

    private static final Logger log = LoggerFactory.getLogger(FieldMappingServiceAdapter.class);


    private final FieldMappingApplicationService fieldMappingApplicationService;
    private final UserSqlRepository userRepository;

    public FieldMappingServiceAdapter(FieldMappingApplicationService fieldMappingApplicationService,
                                      UserSqlRepository userRepository) {
        this.fieldMappingApplicationService = fieldMappingApplicationService;
        this.userRepository = userRepository;
    }

    /**
     * Create a field mapping from DTO
     */
    public void createMapping(FieldMappingDTO dto, User createdBy) {
        log.debug("Creating field mapping for transformation: {}", dto.getTransformationId());

        // Convert DTO to CreateFieldMappingRequest
        CreateFieldMappingRequest request = new CreateFieldMappingRequest();
        request.setSourceFields(dto.getSourceFields());

        // Handle target fields - support both single and multiple
        if(dto.getTargetFields() != null && !dto.getTargetFields().isEmpty()) {
            request.setTargetFields(dto.getTargetFields());
            // Set single field for backward compatibility if only one target
            if(dto.getTargetFields().size() == 1) {
                request.setTargetField(dto.getTargetFields().get(0));
            }
        } else if(dto.getTargetField() != null) {
            request.setTargetField(dto.getTargetField());
            request.setTargetFields(List.of(dto.getTargetField()));
        }

        request.setMappingType(dto.getMappingType() != null ? dto.getMappingType() : "DIRECT");
        request.setSplitConfiguration(dto.getSplitConfiguration());
        request.setJavaFunction(dto.getJavaFunction());
        request.setMappingRule(dto.getMappingRule());
        request.setInputTypes(dto.getInputTypes());
        request.setOutputType(dto.getOutputType());
        request.setDescription(dto.getDescription());
        request.setFunctionName(dto.getFunctionName());
        request.setActive(dto.isActive());
        request.setArrayMapping(dto.isArrayMapping());
        request.setArrayContextPath(dto.getArrayContextPath());
        request.setSourceXPath(dto.getSourceXPath());
        request.setTargetXPath(dto.getTargetXPath());
        request.setMappingOrder(dto.getMappingOrder());
        request.setVisualFlowData(dto.getVisualFlowData());
        request.setFunctionNode(dto.getFunctionNode());

        // Create the mapping
        fieldMappingApplicationService.createMapping(
            dto.getTransformationId(),
            request,
            createdBy
       );
    }

    /**
     * Create a field mapping from DTO using system user
     */
    public void createMapping(FieldMappingDTO dto) {
        // Get system user or first admin user
        User systemUser = userRepository.findByUsername("system").orElse(null);
        if (systemUser == null) {
            systemUser = userRepository.findAll().stream()
                .filter(u -> u.getUsername() != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No users found in system"));
        }

        createMapping(dto, systemUser);
    }

    /**
     * Create a field mapping from DTO using user ID
     */
    public void createMapping(FieldMappingDTO dto, String userId) {
        User user = null;
        if(userId != null) {
            try {
                user = userRepository.findById(UUID.fromString(userId))
                    .orElse(null);
            } catch(Exception e) {
                log.warn("Invalid user ID: {}", userId);
            }
        }

        if(user == null) {
            // Fallback to system user
            createMapping(dto);
        } else {
            createMapping(dto, user);
        }
    }
}
