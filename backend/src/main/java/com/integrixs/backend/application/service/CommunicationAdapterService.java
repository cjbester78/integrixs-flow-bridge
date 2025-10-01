package com.integrixs.backend.application.service;

import com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterModeEnum;
import com.integrixs.backend.annotation.AuditCreate;
import com.integrixs.backend.annotation.AuditDelete;
import com.integrixs.backend.annotation.AuditUpdate;
import com.integrixs.backend.api.dto.request.CreateAdapterRequest;
import com.integrixs.backend.api.dto.request.UpdateAdapterRequest;
import com.integrixs.backend.api.dto.response.AdapterResponse;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.backend.domain.service.AdapterConfigurationService;
import com.integrixs.backend.domain.service.AdapterValidationService;
import com.integrixs.backend.service.AuditTrailService;
import com.integrixs.data.model.AuditTrail;
import com.integrixs.data.model.BusinessComponent;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.ExternalAuthentication;
import com.integrixs.data.sql.repository.BusinessComponentSqlRepository;
import com.integrixs.data.sql.repository.ExternalAuthenticationSqlRepository;
import com.integrixs.shared.enums.AdapterType;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for communication adapter use cases
 */
@Service
public class CommunicationAdapterService {

    private static final Logger log = LoggerFactory.getLogger(CommunicationAdapterService.class);


    private final CommunicationAdapterSqlRepository adapterRepository;
    private final AdapterValidationService validationService;
    private final AdapterConfigurationService configurationService;
    private final AuditTrailService auditTrailService;
    private final BusinessComponentSqlRepository businessComponentRepository;
    private final ExternalAuthenticationSqlRepository externalAuthRepository;

    public CommunicationAdapterService(CommunicationAdapterSqlRepository adapterRepository,
                                       AdapterValidationService validationService,
                                       AdapterConfigurationService configurationService,
                                       AuditTrailService auditTrailService,
                                       BusinessComponentSqlRepository businessComponentRepository,
                                       ExternalAuthenticationSqlRepository externalAuthRepository) {
        this.adapterRepository = adapterRepository;
        this.validationService = validationService;
        this.configurationService = configurationService;
        this.auditTrailService = auditTrailService;
        this.businessComponentRepository = businessComponentRepository;
        this.externalAuthRepository = externalAuthRepository;
    }

    public List<AdapterResponse> getAllAdapters() {
        return adapterRepository.findAll().stream()
                .map(this::toAdapterResponse)
                .collect(Collectors.toList());
    }

    public AdapterResponse getAdapterById(String id) {
        UUID adapterId = UUID.fromString(id);
        return adapterRepository.findById(adapterId)
                .map(this::toAdapterResponse)
                .orElseThrow(() -> new RuntimeException("Adapter not found: " + id));
    }

    @AuditCreate
    public AdapterResponse createAdapter(CreateAdapterRequest request) {
        log.debug("Creating adapter with name: {}", request.getName());

        // Validate name uniqueness
        validationService.validateAdapterNameUniqueness(request.getName(), null);

        // Parse and validate configuration
        Map<String, Object> config = configurationService.parseConfiguration(request.getConfiguration());
        AdapterType type = AdapterType.valueOf(request.getType().toUpperCase());
        AdapterModeEnum mode = mapDtoModeToAdapterMode(request.getMode(), request.getDirection());

        validationService.validateAdapterConfiguration(type, mode, config);

        // Create new adapter
        CommunicationAdapter adapter = new CommunicationAdapter();
        adapter.setName(request.getName());
        adapter.setType(type);
        adapter.setMode(mode);
        adapter.setDescription(request.getDescription());
        adapter.setActive(request.isActive());

        // Set direction based on mode(following reversed convention)
        adapter.setDirection(mode == AdapterModeEnum.INBOUND ? "OUTBOUND" : "INBOUND");

        // Encrypt and set configuration
        String encryptedConfig = configurationService.encryptConfiguration(request.getConfiguration());
        adapter.setConfiguration(encryptedConfig);

        // Set business component
        BusinessComponent businessComponent = businessComponentRepository.findById(
                UUID.fromString(request.getBusinessComponentId()))
                .orElseThrow(() -> new IllegalArgumentException(
                    "Business component not found: " + request.getBusinessComponentId()));
        adapter.setBusinessComponent(businessComponent);

        // Set external authentication if provided
        if(request.getExternalAuthId() != null && !request.getExternalAuthId().trim().isEmpty()) {
            ExternalAuthentication externalAuth = externalAuthRepository.findById(
                    UUID.fromString(request.getExternalAuthId()))
                    .orElseThrow(() -> new IllegalArgumentException(
                        "External authentication not found: " + request.getExternalAuthId()));
            adapter.setExternalAuthentication(externalAuth);
        }

        // Validate activation if requested
        if(adapter.isActive()) {
            validationService.validateAdapterActivation(adapter);
        }

        // Save and audit
        CommunicationAdapter saved = adapterRepository.save(adapter);

        Map<String, Object> details = new HashMap<>();
        details.put("adapterName", saved.getName());
        details.put("adapterType", saved.getType().name());
        details.put("adapterMode", saved.getMode().name());
        auditTrailService.logAction("CommunicationAdapter", saved.getId().toString(),
                AuditTrail.AuditAction.CREATE, details);

        log.info("Created adapter: {} with ID: {}", saved.getName(), saved.getId());
        return toAdapterResponse(saved);
    }

    @AuditUpdate
    public AdapterResponse updateAdapter(String id, UpdateAdapterRequest request) {
        UUID adapterId = UUID.fromString(id);
        log.debug("Updating adapter: {}", adapterId);

        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new RuntimeException("Adapter not found: " + id));

        // Validate name uniqueness if changed
        if(!adapter.getName().equals(request.getName())) {
            validationService.validateAdapterNameUniqueness(request.getName(), adapterId);
        }

        // Parse and validate configuration
        Map<String, Object> config = configurationService.parseConfiguration(request.getConfiguration());
        AdapterType type = AdapterType.valueOf(request.getType().toUpperCase());
        AdapterModeEnum mode = mapDtoModeToAdapterMode(request.getMode(), request.getDirection());

        validationService.validateAdapterConfiguration(type, mode, config);

        // Update fields
        adapter.setName(request.getName());
        adapter.setType(type);
        adapter.setMode(mode);
        adapter.setDescription(request.getDescription());
        adapter.setActive(request.isActive());
        adapter.setDirection(mode == AdapterModeEnum.INBOUND ? "OUTBOUND" : "INBOUND");

        // Encrypt and update configuration
        String encryptedConfig = configurationService.encryptConfiguration(request.getConfiguration());
        adapter.setConfiguration(encryptedConfig);

        // Update business component if provided
        if(request.getBusinessComponentId() != null) {
            BusinessComponent businessComponent = businessComponentRepository.findById(
                    UUID.fromString(request.getBusinessComponentId()))
                    .orElseThrow(() -> new IllegalArgumentException(
                        "Business component not found: " + request.getBusinessComponentId()));
            adapter.setBusinessComponent(businessComponent);
        }

        // Update external authentication
        if(request.getExternalAuthId() != null && !request.getExternalAuthId().trim().isEmpty()) {
            ExternalAuthentication externalAuth = externalAuthRepository.findById(
                    UUID.fromString(request.getExternalAuthId()))
                    .orElseThrow(() -> new IllegalArgumentException(
                        "External authentication not found: " + request.getExternalAuthId()));
            adapter.setExternalAuthentication(externalAuth);
        } else {
            adapter.setExternalAuthentication(null);
        }

        // Validate activation if requested
        if(adapter.isActive()) {
            validationService.validateAdapterActivation(adapter);
        }

        // Save and audit
        CommunicationAdapter updated = adapterRepository.save(adapter);

        Map<String, Object> details = new HashMap<>();
        details.put("adapterName", updated.getName());
        details.put("changes", "Updated adapter configuration");
        auditTrailService.logAction("CommunicationAdapter", updated.getId().toString(),
                AuditTrail.AuditAction.UPDATE, details);

        log.info("Updated adapter: {} with ID: {}", updated.getName(), updated.getId());
        return toAdapterResponse(updated);
    }

    @AuditDelete
    public void deleteAdapter(String id) {
        UUID adapterId = UUID.fromString(id);
        log.debug("Deleting adapter: {}", adapterId);

        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new RuntimeException("Adapter not found: " + id));

        Map<String, Object> details = new HashMap<>();
        details.put("adapterName", adapter.getName());
        details.put("adapterType", adapter.getType().name());
        auditTrailService.logAction("CommunicationAdapter", adapter.getId().toString(),
                AuditTrail.AuditAction.DELETE, details);

        adapterRepository.deleteById(adapterId);

        log.info("Deleted adapter: {} with ID: {}", adapter.getName(), adapterId);
    }

    public AdapterResponse activateAdapter(String id) {
        UUID adapterId = UUID.fromString(id);
        log.debug("Activating adapter: {}", adapterId);

        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new RuntimeException("Adapter not found: " + id));

        validationService.validateAdapterActivation(adapter);

        adapter.setActive(true);
        CommunicationAdapter updated = adapterRepository.save(adapter);

        Map<String, Object> details = new HashMap<>();
        details.put("adapterName", updated.getName());
        details.put("action", "activated");
        auditTrailService.logAction("CommunicationAdapter", updated.getId().toString(),
                AuditTrail.AuditAction.UPDATE, details);

        return toAdapterResponse(updated);
    }

    public AdapterResponse deactivateAdapter(String id) {
        UUID adapterId = UUID.fromString(id);
        log.debug("Deactivating adapter: {}", adapterId);

        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new RuntimeException("Adapter not found: " + id));

        adapter.setActive(false);
        CommunicationAdapter updated = adapterRepository.save(adapter);

        Map<String, Object> details = new HashMap<>();
        details.put("adapterName", updated.getName());
        details.put("action", "deactivated");
        auditTrailService.logAction("CommunicationAdapter", updated.getId().toString(),
                AuditTrail.AuditAction.UPDATE, details);

        return toAdapterResponse(updated);
    }

    private AdapterResponse toAdapterResponse(CommunicationAdapter adapter) {
        // Decrypt configuration for response
        String decryptedConfig = configurationService.decryptConfiguration(adapter.getConfiguration());
        Map<String, Object> configMap = configurationService.parseConfiguration(decryptedConfig);

        return AdapterResponse.builder()
                .id(adapter.getId().toString())
                .name(adapter.getName())
                .type(adapter.getType().name())
                .mode(adapter.getMode().name())
                .direction(adapter.getDirection())
                .description(adapter.getDescription())
                .active(adapter.isActive())
                .healthy(adapter.isHealthy())
                .lastHealthCheck(adapter.getLastHealthCheck())
                .configuration(configMap)
                .businessComponentId(adapter.getBusinessComponent() != null ?
                    adapter.getBusinessComponent().getId().toString() : null)
                .businessComponentName(adapter.getBusinessComponent() != null ?
                    adapter.getBusinessComponent().getName() : null)
                .externalAuthId(adapter.getExternalAuthentication() != null ?
                    adapter.getExternalAuthentication().getId().toString() : null)
                .externalAuthName(adapter.getExternalAuthentication() != null ?
                    adapter.getExternalAuthentication().getName() : null)
                .createdAt(adapter.getCreatedAt())
                .updatedAt(adapter.getUpdatedAt())
                .createdBy(adapter.getCreatedBy() != null ? adapter.getCreatedBy().getUsername() : null)
                .updatedBy(adapter.getUpdatedBy() != null ? adapter.getUpdatedBy().getUsername() : null)
                .build();
    }

    private AdapterModeEnum mapDtoModeToAdapterMode(String dtoMode, String direction) {
        // Following the reversed convention:
        // INBOUND = OUTBOUND(receives from external)
        // OUTBOUND = INBOUND(sends to external)

        if("INBOUND".equalsIgnoreCase(dtoMode)) {
            return AdapterModeEnum.INBOUND;
        } else if("OUTBOUND".equalsIgnoreCase(dtoMode)) {
            return AdapterModeEnum.OUTBOUND;
        }

        // Fallback to direction - based mapping
        if("OUTBOUND".equalsIgnoreCase(direction)) {
            return AdapterModeEnum.INBOUND;
        } else {
            return AdapterModeEnum.OUTBOUND;
        }
    }
}
