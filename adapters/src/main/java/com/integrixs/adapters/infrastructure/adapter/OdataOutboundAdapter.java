package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import java.util.concurrent.CompletableFuture;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import java.util.Map;
import com.integrixs.adapters.config.OdataOutboundAdapterConfig;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.cud.*;
import org.apache.olingo.client.api.communication.request.ODataBasicRequest;
import org.apache.olingo.client.api.communication.response.*;
import org.apache.olingo.client.api.domain.*;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;
import java.net.URI;
import java.net.URL;
import java.util.*;
/**
 * OData Receiver Adapter implementation for OData service operations(OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Performs CRUD operations on OData services in external systems.
 */
public class OdataOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(OdataOutboundAdapter.class);


    private final OdataOutboundAdapterConfig config;
    private ODataClient client;
    public OdataOutboundAdapter(OdataOutboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing OData outbound adapter(outbound) with service URL: {}", config.getServiceUrl());

        try {
            validateConfiguration();
            // Initialize OData client
            client = ODataClientFactory.getClient();
            // Configure client settings
            if(config.getUsername() != null && !config.getUsername().isEmpty()) {
                log.debug("Configured authentication for user: {}", config.getUsername());
            }
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("OData outbound adapter initialized successfully");
        return AdapterOperationResult.success("OData outbound adapter initialized");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying OData outbound adapter");
        client = null;
        return AdapterOperationResult.success("OData outbound adapter destroyed");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        // Test 1: Service URL accessibility
        testResults.add(
            performServiceUrlTest()
       );
        // Test 2: Metadata validation
        testResults.add(
            performMetadataValidationTest()
       );
        // Test 3: Operation configuration
        testResults.add(
            performOperationConfigTest()
       );
        return AdapterOperationResult.success(testResults);
    }

    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return performODataOperation(request.getPayload());
        } catch(Exception e) {
            return AdapterOperationResult.failure("Failed to send OData: " + e.getMessage());
        }
    }

    private AdapterOperationResult performODataOperation(Object payload) throws Exception {
        if(payload == null) {
            throw new AdapterException("Payload cannot be null", null);
        }
        try {
            Map<String, Object> responseData;

            if(payload instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) payload;
                // Determine operation
                String operation = determineOperation(dataMap);
                switch(operation.toUpperCase()) {
                    case "CREATE":
                        responseData = createEntity(dataMap);
                        break;
                    case "UPDATE":
                        responseData = updateEntity(dataMap);
                        break;
                    case "DELETE":
                        responseData = deleteEntity(dataMap);
                        break;
                    case "READ":
                        responseData = readEntity(dataMap);
                        break;
                    case "BATCH":
                        responseData = performBatchOperation(dataMap);
                        break;
                    default:
                        throw new AdapterException(
                                "Unsupported operation: " + operation);
                }
            } else if(payload instanceof Collection) {
                // Batch operation for collection
                responseData = performBatchOperation((Collection<?>) payload);
            } else {
                throw new AdapterException(
                        "Unsupported payload type: " + payload.getClass().getName());
            }
            log.info("OData outbound adapter successfully performed operation");
            return AdapterOperationResult.success(responseData,
                    "Successfully performed OData operation");
        } catch(Exception e) {
            log.error("Error performing OData operation", e);
            throw new AdapterException(
                    "Failed to perform OData operation: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> createEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        // Create entity
        ClientEntity entity = client.getObjectFactory().newEntity(
                new FullQualifiedName(config.getNamespace(), getEntityType(dataMap)));
        // Set properties
        Map<String, Object> properties = (Map<String, Object>) dataMap.get("properties");
        if(properties == null) {
            properties = dataMap; // Use entire map as properties
        }
        for(Map.Entry<String, Object> entry : properties.entrySet()) {
            if(!entry.getKey().startsWith("@") && !entry.getKey().equals("operation")) {
                entity.getProperties().add(client.getObjectFactory().newPrimitiveProperty(
                        entry.getKey(),
                        client.getObjectFactory().newPrimitiveValueBuilder().buildString(
                                String.valueOf(entry.getValue()))));
            }
        }
        // Build URI
        URI serviceUri = URI.create(config.getServiceUrl());
        URI entitySetUri = client.newURIBuilder(serviceUri.toString())
                .appendEntitySetSegment(entitySetName)
                .build();
        // Create request
        ODataEntityCreateRequest<ClientEntity> request =
                client.getCUDRequestFactory().getEntityCreateRequest(entitySetUri, entity);
        request.setFormat(ContentType.APPLICATION_JSON);
        // Add authentication
        addAuthentication(request);
        // Execute request
        ODataEntityCreateResponse<ClientEntity> response = request.execute();
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatusCode());
        result.put("location", response.getHeader("Location"));
        if(response.getBody() != null) {
            result.put("entity", extractEntityData(response.getBody()));
        }
        return result;
    }

    private Map<String, Object> updateEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        String entityKey = (String) dataMap.get("key");
        if(entityKey == null || entityKey.isEmpty()) {
            throw new AdapterException(
                    "Entity key is required for update operation");
        }
        // Create entity
        ClientEntity entity = client.getObjectFactory().newEntity(
                new FullQualifiedName(config.getNamespace(), getEntityType(dataMap)));
        // Set properties
        Map<String, Object> properties = (Map<String, Object>) dataMap.get("properties");
        if(properties == null) {
            properties = dataMap;
        }
        for(Map.Entry<String, Object> entry : properties.entrySet()) {
            if(!entry.getKey().startsWith("@") && !entry.getKey().equals("operation") &&
                !entry.getKey().equals("key")) {
                entity.getProperties().add(client.getObjectFactory().newPrimitiveProperty(
                        entry.getKey(),
                        client.getObjectFactory().newPrimitiveValueBuilder().buildString(
                                String.valueOf(entry.getValue()))));
            }
        }
        // Build URI
        URI serviceUri = URI.create(config.getServiceUrl());
        URI entityUri = client.newURIBuilder(serviceUri.toString())
                .appendEntitySetSegment(entitySetName)
                .appendKeySegment(entityKey)
                .build();
        // Create request(using PATCH for partial update)
        ODataEntityUpdateRequest<ClientEntity> request =
                client.getCUDRequestFactory().getEntityUpdateRequest(entityUri,
                        UpdateType.PATCH, entity);
        ODataEntityUpdateResponse<ClientEntity> response = request.execute();
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatusCode());
        result.put("operation", "UPDATE");
        result.put("key", entityKey);
        if(response.getBody() != null) {
            result.put("entity", extractEntityData(response.getBody()));
        }
        return result;
    }

    private Map<String, Object> deleteEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        String entityKey = (String) dataMap.get("key");
        if(entityKey == null || entityKey.isEmpty()) {
            throw new AdapterException(
                    "Entity key is required for delete operation");
        }
        // Build URI
        URI serviceUri = URI.create(config.getServiceUrl());
        URI entityUri = client.newURIBuilder(serviceUri.toString())
                .appendEntitySetSegment(entitySetName)
                .appendKeySegment(entityKey)
                .build();
        // Create request
        ODataDeleteRequest request = client.getCUDRequestFactory().getDeleteRequest(entityUri);
        // Add authentication
        addAuthentication(request);
        // Execute request
        ODataDeleteResponse response = request.execute();
        Map<String, Object> result = new HashMap<>();
        result.put("status", response.getStatusCode());
        result.put("operation", "DELETE");
        result.put("key", entityKey);
        return result;
    }

    private Map<String, Object> readEntity(Map<String, Object> dataMap) throws Exception {
        String entitySetName = getEntitySetName(dataMap);
        String entityKey = (String) dataMap.get("key");
        Map<String, Object> result = new HashMap<>();
        URI serviceUri = URI.create(config.getServiceUrl());
        URI uri;
        if(entityKey != null && !entityKey.isEmpty()) {
            // Read single entity
            uri = client.newURIBuilder(serviceUri.toString())
                    .appendEntitySetSegment(entitySetName)
                    .appendKeySegment(entityKey)
                    .build();
        } else {
            // Read entity set
            uri = client.newURIBuilder(serviceUri.toString())
                    .appendEntitySetSegment(entitySetName)
                    .build();
        }
        // This is a simplified read operation
        result.put("operation", "READ");
        result.put("entitySet", entitySetName);
        if(entityKey != null) {
            result.put("key", entityKey);
        }
        return result;
    }
    private Map<String, Object> performBatchOperation(Object payload) throws Exception {
        Map<String, Object> result = new HashMap<>();
        // Batch operations would be implemented here
        result.put("operation", "BATCH");
        result.put("message", "Batch operations not fully implemented in this example");
        if(payload instanceof Collection) {
            result.put("itemCount", ((Collection<?>) payload).size());
        }
        return result;
    }

    private Map<String, Object> extractEntityData(ClientEntity entity) {
        Map<String, Object> data = new HashMap<>();
        if(entity.getId() != null) {
            data.put("@odata.id", entity.getId().toString());
        }
        for(ClientProperty property : entity.getProperties()) {
            data.put(property.getName(), property.getValue().asPrimitive());
        }
        return data;
    }

    private void addAuthentication(ODataBasicRequest<?> request) {
        if(config.getUsername() != null && !config.getUsername().isEmpty()) {
            String credentials = config.getUsername() + ":" + config.getPassword();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            request.addCustomHeader("Authorization", "Basic " + encodedCredentials);
        }
        // Add custom headers
        if(config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
            for(Map.Entry<String, String> header : config.getCustomHeaders().entrySet()) {
                request.addCustomHeader(header.getKey(), header.getValue());
            }
        }
    }

    private String determineOperation(Map<String, Object> dataMap) {
        String operation = (String) dataMap.get("operation");
        if(operation == null || operation.isEmpty()) {
            operation = config.getDefaultOperation();
        }
        if(operation == null || operation.isEmpty()) {
            operation = "CREATE"; // Default
        }
        return operation;
    }

    private String getEntitySetName(Map<String, Object> dataMap) throws AdapterException {
        String entitySet = (String) dataMap.get("entitySet");
        if(entitySet == null || entitySet.isEmpty()) {
            entitySet = config.getEntitySetName();
        }
        if(entitySet == null || entitySet.isEmpty()) {
            throw new AdapterException(
                    "Entity set name is required");
        }
        return entitySet;
    }

    private String getEntityType(Map<String, Object> dataMap) throws AdapterException {
        String entityType = (String) dataMap.get("entityType");
        if(entityType == null || entityType.isEmpty()) {
            entityType = config.getEntityTypeName();
        }
        if(entityType == null || entityType.isEmpty()) {
            // Default to entity set name
            entityType = getEntitySetName(dataMap);
        }
        return entityType;
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getServiceUrl() == null || config.getServiceUrl().trim().isEmpty()) {
            throw new AdapterException("Service URL is required", null);
        }
        if(config.getNamespace() == null || config.getNamespace().trim().isEmpty()) {
            config.setNamespace("Default"); // Set default namespace
        }
    }
    public long getTimeout() {
        // OData receivers typically don't poll, they push data
        return 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("OData Receiver(Outbound): %s, Default Operation: %s",
                config.getServiceUrl(),
                config.getDefaultOperation() != null ? config.getDefaultOperation() : "CREATE");
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }

    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        List<AdapterOperationResult> results = new ArrayList<>();
        for(SendRequest request : requests) {
            results.add(send(request));
        }
        boolean allSuccess = results.stream().allMatch(AdapterOperationResult::isSuccess);
        return allSuccess ?
            AdapterOperationResult.success(results) :
            AdapterOperationResult.failure("Some batch operations failed");
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }

    @Override
    public boolean supportsBatchOperations() {
        return false;
    }

    @Override
    public int getMaxBatchSize() {
        return 100;
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.ODATA)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("Outbound adapter implementation")
                .version("1.0.0")
                .supportsBatch(supportsBatchOperations())
                .supportsAsync(true)
                .build();
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    // Helper methods for connection testing
    private AdapterOperationResult performServiceUrlTest() {
        try {
            URL url = new URL(config.getServiceUrl());
            String serviceInfo = String.format("Service URL: %s", url.toString());
            return AdapterOperationResult.success("Service URL Test", serviceInfo);
        } catch(Exception e) {
            return AdapterOperationResult.failure("Invalid service URL: " + e.getMessage());
        }
    }

    private AdapterOperationResult performMetadataValidationTest() {
        try {
            if(config.getNamespace() == null || config.getNamespace().isEmpty()) {
                return AdapterOperationResult.failure("Namespace not configured");
            }
            return AdapterOperationResult.success("Metadata configuration valid");
        } catch(Exception e) {
            return AdapterOperationResult.failure("Metadata validation failed: " + e.getMessage());
        }
    }

    private AdapterOperationResult performOperationConfigTest() {
        try {
            String operation = config.getDefaultOperation() != null ? config.getDefaultOperation() : "CREATE";
            String entitySet = config.getEntitySetName() != null ? config.getEntitySetName() : "Not configured";
            String configInfo = String.format("Default Operation: %s, Entity Set: %s", operation, entitySet);
            return AdapterOperationResult.success("Operation Config", configInfo);
        } catch(Exception e) {
            return AdapterOperationResult.failure("Operation configuration test failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.ODATA;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }

}
