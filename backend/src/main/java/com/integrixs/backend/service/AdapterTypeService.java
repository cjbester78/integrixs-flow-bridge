package com.integrixs.backend.service;

import com.integrixs.backend.api.dto.AdapterTypeDTO;
import com.integrixs.backend.api.dto.ConfigurationSchemaDTO;
import com.integrixs.data.model.AdapterType;
import com.integrixs.data.model.AdapterCategory;
import com.integrixs.data.sql.repository.AdapterTypeSqlRepository;
import com.integrixs.data.sql.repository.AdapterCategorySqlRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdapterTypeService {

    private static final Logger log = LoggerFactory.getLogger(AdapterTypeService.class);


    private final AdapterTypeSqlRepository adapterTypeRepository;
    private final AdapterCategorySqlRepository adapterCategoryRepository;

    public AdapterTypeService(AdapterTypeSqlRepository adapterTypeRepository,
                              AdapterCategorySqlRepository adapterCategoryRepository) {
        this.adapterTypeRepository = adapterTypeRepository;
        this.adapterCategoryRepository = adapterCategoryRepository;
    }

    /**
     * Get paginated list of adapter types with optional filters
     */
    public Page<AdapterType> getAdapterTypes(Pageable pageable, String category, String search) {
        log.debug("Fetching adapter types with category: {}, search: {}", category, search);

        UUID categoryId = null;
        if(category != null && !category.isEmpty()) {
            // Resolve category code to ID
            categoryId = adapterCategoryRepository.findByCode(category)
                .map(AdapterCategory::getId)
                .orElse(null);
        }

        return adapterTypeRepository.findWithFilters(categoryId, "active", search, pageable);
    }

    /**
     * Register a new adapter type
     */
    public AdapterType registerAdapterType(AdapterTypeDTO dto) {
        log.info("Registering new adapter type with code: {}", dto.getCode());

        // Check if adapter type already exists
        if(adapterTypeRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Adapter type with code " + dto.getCode() + " already exists");
        }

        AdapterType adapterType = new AdapterType();
        mapDtoToEntity(dto, adapterType);

        return adapterTypeRepository.save(adapterType);
    }

    /**
     * Update an existing adapter type
     */
    public void updateAdapterType(UUID id, AdapterTypeDTO dto) {
        log.info("Updating adapter type with id: {}", id);

        AdapterType adapterType = adapterTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Adapter type not found with id: " + id));

        mapDtoToEntity(dto, adapterType);
        adapterTypeRepository.save(adapterType);
    }

    /**
     * Get configuration schema for specific adapter type and direction
     */
    public ConfigurationSchemaDTO getConfigurationSchema(UUID typeId, String direction) {
        log.debug("Fetching configuration schema for adapter type: {}, direction: {}", typeId, direction);

        AdapterType adapterType = adapterTypeRepository.findById(typeId)
            .orElseThrow(() -> new IllegalArgumentException("Adapter type not found with id: " + typeId));

        ConfigurationSchemaDTO schemaDTO = new ConfigurationSchemaDTO();
        schemaDTO.setDirection(direction);

        // Get the appropriate schema based on direction
        Map<String, Object> schema = null;
        switch(direction.toLowerCase()) {
            case "inbound":
                if(!adapterType.isSupportsInbound()) {
                    throw new IllegalArgumentException("Adapter type does not support inbound direction");
                }
                schema = adapterType.getInboundConfigSchema();
                break;
            case "outbound":
                if(!adapterType.isSupportsOutbound()) {
                    throw new IllegalArgumentException("Adapter type does not support outbound direction");
                }
                schema = adapterType.getOutboundConfigSchema();
                break;
            case "bidirectional":
                if(!adapterType.isSupportsBidirectional()) {
                    throw new IllegalArgumentException("Adapter type does not support bidirectional direction");
                }
                // Merge common, inbound, and outbound schemas
                schema = adapterType.getCommonConfigSchema();
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }

        schemaDTO.setSchema(schema);
        schemaDTO.setSupportedAuthMethods(adapterType.getAuthenticationMethods());

        return schemaDTO;
    }

    /**
     * Get adapter type by ID
     */
    public AdapterType getAdapterType(UUID id) {
        return adapterTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Adapter type not found with id: " + id));
    }

    /**
     * Get adapter type by code
     */
    public AdapterType getAdapterTypeByCode(String code) {
        return adapterTypeRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("Adapter type not found with code: " + code));
    }

    /**
     * Get all adapter categories
     */
    public List<AdapterCategory> getAdapterCategories() {
        return adapterCategoryRepository.findAll();
    }

    /**
     * Map DTO to entity
     */
    private void mapDtoToEntity(AdapterTypeDTO dto, AdapterType entity) {
        entity.setCode(dto.getCode());
        entity.setName(dto.getName());
        entity.setVendor(dto.getVendor());
        entity.setVersion(dto.getVersion());
        entity.setDescription(dto.getDescription());
        entity.setIcon(dto.getIcon());
        entity.setSupportsInbound(dto.isSupportsInbound());
        entity.setSupportsOutbound(dto.isSupportsOutbound());
        entity.setSupportsBidirectional(dto.isSupportsBidirectional());
        entity.setInboundConfigSchema(dto.getInboundConfigSchema());
        entity.setOutboundConfigSchema(dto.getOutboundConfigSchema());
        entity.setCommonConfigSchema(dto.getCommonConfigSchema());
        entity.setCapabilities(dto.getCapabilities());
        entity.setSupportedProtocols(dto.getSupportedProtocols());
        entity.setSupportedFormats(dto.getSupportedFormats());
        entity.setAuthenticationMethods(dto.getAuthenticationMethods());
        entity.setDocumentationUrl(dto.getDocumentationUrl());
        entity.setSupportUrl(dto.getSupportUrl());
        entity.setPricingTier(dto.getPricingTier());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : "active");
        entity.setCertified(dto.isCertified());
        entity.setCertificationDate(dto.getCertificationDate());
    }
}
