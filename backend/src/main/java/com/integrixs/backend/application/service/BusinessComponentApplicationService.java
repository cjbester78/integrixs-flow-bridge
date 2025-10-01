package com.integrixs.backend.application.service;

import com.integrixs.backend.api.dto.request.CreateBusinessComponentRequest;
import com.integrixs.backend.api.dto.request.UpdateBusinessComponentRequest;
import com.integrixs.backend.api.dto.response.BusinessComponentResponse;
import com.integrixs.data.sql.repository.BusinessComponentSqlRepository;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.backend.domain.service.ComponentManagementService;
import com.integrixs.backend.exception.ResourceNotFoundException;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.BusinessComponent;
import com.integrixs.data.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for business component management
 * Orchestrates business component operations
 */
@Service
public class BusinessComponentApplicationService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BusinessComponentApplicationService.class);

    private final BusinessComponentSqlRepository businessComponentRepository;
    private final CommunicationAdapterSqlRepository communicationAdapterRepository;
    private final ComponentManagementService componentManagementService;
    private final AuditTrailService auditTrailService;

    public BusinessComponentApplicationService(BusinessComponentSqlRepository businessComponentRepository,
                                               CommunicationAdapterSqlRepository communicationAdapterRepository,
                                               ComponentManagementService componentManagementService,
                                               AuditTrailService auditTrailService) {
        this.businessComponentRepository = businessComponentRepository;
        this.communicationAdapterRepository = communicationAdapterRepository;
        this.componentManagementService = componentManagementService;
        this.auditTrailService = auditTrailService;
    }

    /**
     * Create a new business component
     */
    public BusinessComponentResponse createBusinessComponent(CreateBusinessComponentRequest request, User createdBy) {
        log.info("Creating business component: {}", request.getName());

        // Map request to entity
        BusinessComponent component = new BusinessComponent();
        component.setName(request.getName());
        component.setDescription(request.getDescription());
        component.setContactEmail(request.getContactEmail());
        component.setContactPhone(request.getContactPhone());

        // Validate
        componentManagementService.validateNewComponent(component);

        // Save
        BusinessComponent saved = businessComponentRepository.save(component);

        // Audit
        auditTrailService.logUserAction(
            createdBy,
            "BusinessComponent",
            saved.getId().toString(),
            "CREATE"
       );

        log.info("Created business component with ID: {}", saved.getId());

        // Return response
        return mapToResponse(saved);
    }

    /**
     * Get all business components
     */
    public List<BusinessComponentResponse> getAllBusinessComponents() {
        log.debug("Fetching all business components");

        return businessComponentRepository.findAll()
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get business component by ID
     */
    public BusinessComponentResponse getBusinessComponentById(String id) {
        log.debug("Fetching business component with ID: {}", id);

        BusinessComponent component = businessComponentRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Business component not found: " + id));

        return mapToResponse(component);
    }

    /**
     * Update business component
     */
    public BusinessComponentResponse updateBusinessComponent(String id, UpdateBusinessComponentRequest request, User updatedBy) {
        log.info("Updating business component: {}", id);

        // Find existing
        BusinessComponent existing = businessComponentRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Business component not found: " + id));

        // Map updates
        BusinessComponent updates = new BusinessComponent();
        updates.setName(request.getName());
        updates.setDescription(request.getDescription());
        updates.setContactEmail(request.getContactEmail());
        updates.setContactPhone(request.getContactPhone());

        // Validate
        componentManagementService.validateComponentUpdate(existing, updates);

        // Apply updates
        componentManagementService.applyUpdates(existing, updates);

        // Save
        BusinessComponent saved = businessComponentRepository.save(existing);

        // Audit
        auditTrailService.logUserAction(
            updatedBy,
            "BusinessComponent",
            saved.getId().toString(),
            "UPDATE"
       );

        log.info("Updated business component: {}", saved.getId());

        return mapToResponse(saved);
    }

    /**
     * Delete business component
     */
    public void deleteBusinessComponent(String id, User deletedBy) {
        log.info("Deleting business component: {}", id);

        // Find existing
        BusinessComponent component = businessComponentRepository.findById(UUID.fromString(id))
            .orElseThrow(() -> new ResourceNotFoundException("Business component not found: " + id));

        // Validate deletion
        componentManagementService.validateComponentDeletion(component);

        // Delete
        businessComponentRepository.deleteById(component.getId());

        // Audit
        auditTrailService.logUserAction(
            deletedBy,
            "BusinessComponent",
            id,
            "DELETE"
       );

        log.info("Deleted business component: {} ( {})", component.getName(), id);
    }

    /**
     * Map entity to response DTO
     */
    private BusinessComponentResponse mapToResponse(BusinessComponent component) {
        return BusinessComponentResponse.builder()
            .id(component.getId().toString())
            .name(component.getName())
            .description(component.getDescription())
            .contactEmail(component.getContactEmail())
            .contactPhone(component.getContactPhone())
            .createdAt(component.getCreatedAt())
            .updatedAt(component.getUpdatedAt())
            .associatedAdapterCount(communicationAdapterRepository.countByBusinessComponent_Id(component.getId()))
            .build();
    }
}
