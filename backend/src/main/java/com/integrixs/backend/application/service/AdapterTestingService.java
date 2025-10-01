package com.integrixs.backend.application.service;

import com.integrixs.adapters.core.AdapterException;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.adapters.core.BaseAdapter;
import com.integrixs.adapters.factory.AdapterFactoryManager;
import com.integrixs.backend.api.dto.request.TestAdapterRequest;
import com.integrixs.backend.api.dto.response.AdapterTestResponse;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.backend.domain.service.AdapterConfigurationService;
import com.integrixs.data.model.CommunicationAdapter;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application service for testing adapter connections
 */
@Service
public class AdapterTestingService {

    private static final Logger log = LoggerFactory.getLogger(AdapterTestingService.class);


    private final CommunicationAdapterSqlRepository adapterRepository;
    private final AdapterConfigurationService configurationService;
    private final AdapterFactoryManager factoryManager = AdapterFactoryManager.getInstance();

    public AdapterTestingService(CommunicationAdapterSqlRepository adapterRepository,
                                 AdapterConfigurationService configurationService) {
        this.adapterRepository = adapterRepository;
        this.configurationService = configurationService;
    }

    public AdapterTestResponse testAdapter(TestAdapterRequest request) {
        UUID adapterId = UUID.fromString(request.getAdapterId());
        log.debug("Testing adapter: {}", adapterId);

        CommunicationAdapter adapter = adapterRepository.findById(adapterId)
                .orElseThrow(() -> new RuntimeException("Adapter not found: " + adapterId));

        long startTime = System.currentTimeMillis();

        try {
            // Decrypt configuration for testing
            String decryptedConfig = configurationService.decryptConfiguration(adapter.getConfiguration());
            Map<String, Object> configMap = configurationService.parseConfiguration(decryptedConfig);

            // Merge external authentication config into the adapter configuration before creation
            if(adapter.getExternalAuthentication() != null) {
                configMap.put("authType", adapter.getExternalAuthentication().getAuthType().name());
                configMap.put("username", adapter.getExternalAuthentication().getUsername());
                configMap.put("encryptedPassword", adapter.getExternalAuthentication().getEncryptedPassword());
                configMap.put("clientId", adapter.getExternalAuthentication().getClientId());
                configMap.put("encryptedApiKey", adapter.getExternalAuthentication().getEncryptedApiKey());
            }

            // Create adapter instance
            // Convert shared enum AdapterType to domain model AdapterType
            com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum coreAdapterType =
                com.integrixs.adapters.domain.model.AdapterConfiguration.AdapterTypeEnum.valueOf(adapter.getType().name());

            Object adapterInstance = factoryManager.createAndInitialize(
                coreAdapterType,
                adapter.getMode(),
                configMap
           );

            AdapterTestResponse.AdapterTestResponseBuilder responseBuilder = AdapterTestResponse.builder()
                    .testedAt(LocalDateTime.now())
                    .responseTimeMs(System.currentTimeMillis() - startTime);

            // Create AdapterConfiguration for testing
            com.integrixs.adapters.domain.model.AdapterConfiguration adapterConfig =
                com.integrixs.adapters.domain.model.AdapterConfiguration.builder()
                    .adapterType(coreAdapterType)
                    .adapterMode(adapter.getMode())
                    .connectionProperties(configMap)
                    .build();

            // Cast to AdapterPort and test connection
            com.integrixs.adapters.domain.port.AdapterPort adapterPort =
                (com.integrixs.adapters.domain.port.AdapterPort) adapterInstance;

            if(request.isValidateOnly()) {
                // Only validate configuration by testing connection
                com.integrixs.adapters.domain.model.AdapterOperationResult validationResult = adapterPort.testConnection(adapterConfig);
                return responseBuilder
                        .success(validationResult.isSuccess())
                        .message(validationResult.isSuccess() ? "Configuration is valid" : "Configuration is invalid: " + validationResult.getMessage())
                        .connectionValid(validationResult.isSuccess())
                        .build();
            } else {
                // Test actual connection
                com.integrixs.adapters.domain.model.AdapterOperationResult testResult = adapterPort.testConnection(adapterConfig);

                Map<String, Object> testResults = new HashMap<>();
                if(testResult.getData() != null) {
                    testResults.put("response", testResult.getData());
                }

                Map<String, String> connectionDetails = new HashMap<>();
                connectionDetails.put("adapterType", adapter.getType().name());
                connectionDetails.put("adapterMode", adapter.getMode().name());
                connectionDetails.put("endpoint", extractEndpoint(configMap));

                return responseBuilder
                        .success(testResult.isSuccess())
                        .message(testResult.isSuccess() ? "Connection test successful" : testResult.getMessage())
                        .errorDetails(testResult.getMessage())
                        .connectionValid(testResult.isSuccess())
                        .authenticationValid(testResult.isSuccess())
                        .testResults(testResults)
                        .connectionDetails(connectionDetails)
                        .build();
            }
        } catch(Exception e) {
            log.error("Adapter test failed", e);
            return AdapterTestResponse.builder()
                    .success(false)
                    .message("Connection test failed")
                    .errorDetails(e.getMessage())
                    .testedAt(LocalDateTime.now())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .connectionValid(false)
                    .authenticationValid(false)
                    .build();
        }
    }

    private String extractEndpoint(Map<String, Object> config) {
        // Extract endpoint based on adapter type
        if(config.containsKey("url")) {
            return(String) config.get("url");
        } else if(config.containsKey("host")) {
            String host = (String) config.get("host");
            Object port = config.get("port");
            return host + (port != null ? ":" + port : "");
        } else if(config.containsKey("jdbcUrl")) {
            return(String) config.get("jdbcUrl");
        } else if(config.containsKey("wsdlUrl")) {
            return(String) config.get("wsdlUrl");
        } else if(config.containsKey("directory")) {
            return(String) config.get("directory");
        }
        return "N/A";
    }
}
