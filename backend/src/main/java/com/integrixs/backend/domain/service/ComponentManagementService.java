package com.integrixs.backend.domain.service;

import com.integrixs.data.sql.repository.BusinessComponentSqlRepository;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.data.model.BusinessComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Domain service for business component management
 * Contains core business logic for component operations
 */
@Service
public class ComponentManagementService {

    private static final Logger log = LoggerFactory.getLogger(ComponentManagementService.class);
    private final BusinessComponentSqlRepository businessComponentRepository;
    private final CommunicationAdapterSqlRepository communicationAdapterRepository;

    public ComponentManagementService(BusinessComponentSqlRepository businessComponentRepository,
                                     CommunicationAdapterSqlRepository communicationAdapterRepository) {
        this.businessComponentRepository = businessComponentRepository;
        this.communicationAdapterRepository = communicationAdapterRepository;
    }

    /**
     * Validate business component before creation
     */
    public void validateNewComponent(BusinessComponent component) {
        if(component.getName() == null || component.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Business component name is required");
        }

        // Check for duplicate names
        if(businessComponentRepository.existsByName(component.getName())) {
            throw new IllegalStateException("Business component with name '" + component.getName() + "' already exists");
        }

        validateContactInfo(component);
    }

    /**
     * Validate business component before update
     */
    public void validateComponentUpdate(BusinessComponent existing, BusinessComponent updates) {
        if(updates.getName() == null || updates.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Business component name is required");
        }

        // Check for duplicate names(excluding current component)
        if(!existing.getName().equals(updates.getName()) &&
            businessComponentRepository.existsByNameAndIdNot(updates.getName(), existing.getId())) {
            throw new IllegalStateException("Business component with name '" + updates.getName() + "' already exists");
        }

        validateContactInfo(updates);
    }

    /**
     * Validate component can be deleted
     */
    public void validateComponentDeletion(BusinessComponent component) {
        // Check if component is used by any adapters
        long adapterCount = communicationAdapterRepository.countByBusinessComponent_Id(component.getId());
        if(adapterCount > 0) {
            throw new IllegalStateException(
                String.format("Cannot delete business component '%s' as it is associated with %d adapter(s)",
                    component.getName(), adapterCount)
           );
        }

        log.info("Business component {} is safe to delete", component.getName());
    }

    /**
     * Apply updates to existing component
     */
    public void applyUpdates(BusinessComponent existing, BusinessComponent updates) {
        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setContactEmail(updates.getContactEmail());
        existing.setContactPhone(updates.getContactPhone());
    }

    /**
     * Validate contact information
     */
    private void validateContactInfo(BusinessComponent component) {
        if(component.getContactEmail() != null && !component.getContactEmail().isEmpty()) {
            if(!isValidEmail(component.getContactEmail())) {
                throw new IllegalArgumentException("Invalid email format: " + component.getContactEmail());
            }
        }

        if(component.getContactPhone() != null && !component.getContactPhone().isEmpty()) {
            if(!isValidPhone(component.getContactPhone())) {
                throw new IllegalArgumentException("Invalid phone format: " + component.getContactPhone());
            }
        }
    }

    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email.matches("^[A - Za - z0-9 + _. - ] + @(. + )$");
    }

    /**
     * Simple phone validation(allows various formats)
     */
    private boolean isValidPhone(String phone) {
        // Remove all non - digits for validation
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 15;
    }
}
