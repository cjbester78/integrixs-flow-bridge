package com.integrixs.backend.infrastructure.adapter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.FlowTransformation;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.FieldMapping;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Infrastructure service for adapter configuration handling
 */
@Service("adapterConfigurationInfrastructureService")
public class AdapterConfigurationService {

    private static final Logger log = LoggerFactory.getLogger(AdapterConfigurationService.class);


    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse adapter configuration JSON
     * @param configJson The configuration JSON string
     * @return Parsed configuration map
     */
    public Map<String, Object> parseAdapterConfiguration(String configJson) {
        if(configJson == null || configJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(configJson, new TypeReference<Map<String, Object>>() {});
        } catch(Exception e) {
            log.warn("Failed to parse adapter configuration: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Build conversion configuration for target adapter
     * @param flow The integration flow
     * @param outboundAdapter The target adapter
     * @param transformations List of transformations
     * @param fieldMappingProvider Provider for field mappings
     * @return Conversion configuration map
     */
    public Map<String, Object> buildConversionConfig(
            IntegrationFlow flow,
            CommunicationAdapter outboundAdapter,
            List<FlowTransformation> transformations,
            FieldMappingProvider fieldMappingProvider) {

        Map<String, Object> config = new HashMap<>();

        // Get adapter configuration
        Map<String, Object> adapterConfig = parseAdapterConfiguration(outboundAdapter.getConfiguration());

        String adapterType = outboundAdapter.getType().name();

        // Configure based on adapter type
        switch(adapterType) {
            case "FILE":
            case "FTP":
            case "SFTP":
                configureFileAdapter(config, adapterConfig);
                break;

            case "JDBC":
                configureJdbcAdapter(config, adapterConfig);
                break;

            default:
                // Default configuration for other adapter types
                break;
        }

        // Add field mappings if available
        Map<String, String> fieldMappings = extractFieldMappings(transformations, fieldMappingProvider);
        if(!fieldMappings.isEmpty()) {
            config.put("fieldMappings", fieldMappings);
        }

        return config;
    }

    private void configureFileAdapter(Map<String, Object> config, Map<String, Object> adapterConfig) {
        String fileFormat = (String) adapterConfig.getOrDefault("fileFormat", "CSV");

        if("CSV".equalsIgnoreCase(fileFormat)) {
            // CSV configuration with file separators
            config.put("delimiter", adapterConfig.getOrDefault("delimiter", ","));
            config.put("includeHeaders", adapterConfig.getOrDefault("includeHeaders", true));
            config.put("quoteAllFields", adapterConfig.getOrDefault("quoteAllFields", false));
            config.put("lineTerminator", adapterConfig.getOrDefault("lineTerminator", "\n"));
            config.put("quoteCharacter", adapterConfig.getOrDefault("quoteCharacter", "\""));
        } else if("FIXED".equalsIgnoreCase(fileFormat)) {
            // Fixed - length configuration
            config.put("fixedLength", true);
            config.put("fieldLengths", adapterConfig.get("fieldLengths")); // Map of field->length
            config.put("padCharacter", adapterConfig.getOrDefault("padCharacter", " "));
            config.put("lineTerminator", adapterConfig.getOrDefault("lineTerminator", "\n"));
        }
    }

    private void configureJdbcAdapter(Map<String, Object> config, Map<String, Object> adapterConfig) {
        config.put("tableName", adapterConfig.getOrDefault("tableName", "data"));
        config.put("operation", adapterConfig.getOrDefault("operation", "INSERT"));
        config.put("whereClause", adapterConfig.get("whereClause"));
        config.put("generateBatch", adapterConfig.getOrDefault("generateBatch", false));
    }

    private Map<String, String> extractFieldMappings(
            List<FlowTransformation> transformations,
            FieldMappingProvider fieldMappingProvider) {

        Map<String, String> fieldMappings = new HashMap<>();

        for(FlowTransformation transformation : transformations) {
            if(transformation.getType() == FlowTransformation.TransformationType.FIELD_MAPPING) {
                List<FieldMapping> mappings = fieldMappingProvider.getFieldMappings(transformation.getId());
                for(FieldMapping mapping : mappings) {
                    // Parse source fields JSON to get first field
                    try {
                        List<String> sourceFields = objectMapper.readValue(
                            mapping.getSourceFields(),
                            new TypeReference<List<String>>() {}
                       );
                        if(!sourceFields.isEmpty()) {
                            fieldMappings.put(sourceFields.get(0), mapping.getTargetField());
                        }
                    } catch(Exception e) {
                        log.warn("Failed to parse source fields for mapping: {}", mapping.getId());
                    }
                }
            }
        }

        return fieldMappings;
    }

    /**
     * Interface for providing field mappings
     */
    public interface FieldMappingProvider {
        List<FieldMapping> getFieldMappings(UUID transformationId);
    }
}
