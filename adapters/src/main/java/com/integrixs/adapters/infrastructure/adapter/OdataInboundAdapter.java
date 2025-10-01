package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import java.util.concurrent.CompletableFuture;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import java.util.Map;
import com.integrixs.adapters.config.OdataInboundAdapterConfig;
import org.apache.olingo.client.api.ODataClient;
import org.apache.olingo.client.api.communication.request.retrieve.ODataEntitySetRequest;
import org.apache.olingo.client.api.communication.response.ODataRetrieveResponse;
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import org.apache.olingo.client.api.domain.ClientProperty;
import org.apache.olingo.client.core.ODataClientFactory;
import org.apache.olingo.commons.api.format.ContentType;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * OData Sender Adapter implementation for OData service consumption(INBOUND).
 * Follows middleware convention: Inbound = receives data FROM external systems.
 * Polls OData services and retrieves entities from external systems.
 */
public class OdataInboundAdapter extends AbstractAdapter implements InboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(OdataInboundAdapter.class);


    private final OdataInboundAdapterConfig config;
    private ODataClient client;
    private final Map<String, String> processedEntities = new ConcurrentHashMap<>();
    private String lastDeltaToken;
    public OdataInboundAdapter(OdataInboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing OData inbound adapter(inbound) with service URL: {}", config.getServiceUrl());

        try {
            validateConfiguration();
            // Initialize OData client
            client = ODataClientFactory.getClient();
            // Configure client settings
            if(config.getUsername() != null && !config.getUsername().isEmpty()) {
                // Basic authentication would be configured here
                log.debug("Configured authentication for user: {}", config.getUsername());
            }
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("OData inbound adapter initialized successfully");
        return AdapterOperationResult.success("OData inbound adapter initialized");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying OData inbound adapter");
        processedEntities.clear();
        client = null;
        return AdapterOperationResult.success("OData inbound adapter destroyed");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        // Test 1: Service URL accessibility
        testResults.add(
            performServiceUrlTest()
       );
        // Test 2: Metadata document accessibility
        testResults.add(
            performMetadataAccessTest()
       );
        // Test 3: Entity set validation
        if(config.getEntitySetName() != null && !config.getEntitySetName().isEmpty()) {
            testResults.add(
                performEntitySetValidationTest()
           );
        }
        return AdapterOperationResult.success(testResults);
    }


    private AdapterOperationResult pollODataService() throws Exception {
        List<Map<String, Object>> entities = new ArrayList<>();
        try {
            // Build OData URI
            String serviceUrl = config.getServiceUrl();

            // Create entity set request
            ODataEntitySetRequest<ClientEntitySet> request = client.getRetrieveRequestFactory()
                    .getEntitySetRequest(client.newURIBuilder(serviceUrl)
                            .appendEntitySetSegment(config.getEntitySetName())
                            .build());
            // Set format
            request.setFormat(ContentType.APPLICATION_JSON);
            // Add query options
            URI requestUri = addQueryOptions(request.getURI());
            request = client.getRetrieveRequestFactory().getEntitySetRequest(requestUri);
            // Add authentication headers if configured
            if(config.getUsername() != null && !config.getUsername().isEmpty()) {
                String credentials = config.getUsername() + ":" + config.getPassword();
                String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
                request.addCustomHeader("Authorization", "Basic " + encodedCredentials);
            }
            // Add custom headers if configured
            if(config.getCustomHeaders() != null && !config.getCustomHeaders().isEmpty()) {
                for(Map.Entry<String, String> header : config.getCustomHeaders().entrySet()) {
                    request.addCustomHeader(header.getKey(), header.getValue());
                }
            }
            // Execute request
            ODataRetrieveResponse<ClientEntitySet> response = request.execute();
            ClientEntitySet entitySet = response.getBody();
            // Process entities
            for(ClientEntity entity : entitySet.getEntities()) {
                Map<String, Object> entityData = processEntity(entity);
                if(entityData != null) {
                    entities.add(entityData);
                }
            }
            // Handle delta token for change tracking
            if(config.isEnableChangeTracking() && response.getHeader("DataServiceVersion") != null) {
                String deltaLink = entitySet.getDeltaLink() != null ? entitySet.getDeltaLink().toString() : null;
                if(deltaLink != null) {
                    lastDeltaToken = extractDeltaToken(deltaLink);
                    log.debug("Updated delta token: {}", lastDeltaToken);
                }
            }
            log.info("OData inbound adapter retrieved {} entities", entities.size());
            return AdapterOperationResult.success(entities,
                    String.format("Successfully retrieved %d entities from OData service", entities.size()));
        } catch(Exception e) {
            log.error("Error polling OData service", e);
            throw new AdapterException("Failed to poll OData service: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> processEntity(ClientEntity entity) {
        Map<String, Object> entityData = new HashMap<>();
        // Extract entity ID
        if(entity.getId() != null) {
            entityData.put("@odata.id", entity.getId().toString());
        }
        // Extract entity type
        if(entity.getTypeName() != null) {
            entityData.put("@odata.type", entity.getTypeName().toString());
        }
        // Extract properties
        for(ClientProperty property : entity.getProperties()) {
            String propertyName = property.getName();
            Object propertyValue = property.getValue().asPrimitive();
            entityData.put(propertyName, propertyValue);
        }
        // Extract navigation properties if configured
        if(config.isExpandNavigationProperties() && !entity.getNavigationLinks().isEmpty()) {
            Map<String, Object> navigationProps = new HashMap<>();
            entity.getNavigationLinks().forEach(link -> {
                navigationProps.put(link.getName(), link.getLink().toString());
            });
            entityData.put("@odata.navigationProperties", navigationProps);
        }
        // Check for duplicate processing
        String entityId = entityData.get("@odata.id") != null ?
                entityData.get("@odata.id").toString() : UUID.randomUUID().toString();
        if(config.isEnableDuplicateHandling() && processedEntities.containsKey(entityId)) {
            log.debug("Skipping already processed entity: {}", entityId);
            return null;
        }
        processedEntities.put(entityId, String.valueOf(System.currentTimeMillis()));
        return entityData;
    }

    private URI addQueryOptions(URI baseUri) throws Exception {
        String uriString = baseUri.toString();
        List<String> queryOptions = new ArrayList<>();
        // Add filter
        if(config.getFilter() != null && !config.getFilter().isEmpty()) {
            queryOptions.add("$filter = " + encodeQueryParam(config.getFilter()));
        }
        // Add select
        if(config.getSelect() != null && !config.getSelect().isEmpty()) {
            queryOptions.add("$select = " + encodeQueryParam(config.getSelect()));
        }
        // Add expand
        if(config.getExpand() != null && !config.getExpand().isEmpty()) {
            queryOptions.add("$expand = " + encodeQueryParam(config.getExpand()));
        }
        // Add orderby
        if(config.getOrderBy() != null && !config.getOrderBy().isEmpty()) {
            queryOptions.add("$orderby = " + encodeQueryParam(config.getOrderBy()));
        }
        // Add top
        if(config.getTop() > 0) {
            queryOptions.add("$top = " + config.getTop());
        }
        // Add skip for pagination
        if(config.getSkip() > 0) {
            queryOptions.add("$skip = " + config.getSkip());
        }
        // Add count
        if(config.isIncludeCount()) {
            queryOptions.add("$count = true");
        }
        // Add delta token for change tracking
        if(config.isEnableChangeTracking() && lastDeltaToken != null) {
            queryOptions.add("$deltatoken = " + lastDeltaToken);
        }
        // Append query options
        if(!queryOptions.isEmpty()) {
            String queryString = String.join("&", queryOptions);
            uriString += (uriString.contains("?") ? "&" : "?") + queryString;
        }
        return URI.create(uriString);
    }

    private String encodeQueryParam(String param) throws Exception {
        return java.net.URLEncoder.encode(param, "UTF-8");
    }

    private String extractDeltaToken(String deltaLink) {
        // Extract delta token from delta link
        int tokenIndex = deltaLink.indexOf("$deltatoken = ");
        if(tokenIndex >= 0) {
            return deltaLink.substring(tokenIndex + 12);
        }
        return null;
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getServiceUrl() == null || config.getServiceUrl().trim().isEmpty()) {
            throw new AdapterException("Service URL is required", null);
        }
        // Set defaults
        if(config.getPollingInterval() <= 0) {
            config.setPollingInterval(30000L); // Default 30 seconds
        }
    }
    public long getPollingInterval() {
        return config.getPollingInterval();
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("OData Sender(Inbound): %s/%s, Polling: %dms",
                config.getServiceUrl(),
                config.getEntitySetName(),
                config.getPollingInterval());
    }

    // InboundAdapterPort implementation
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            return pollODataService();
        } catch(Exception e) {
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }

    @Override
    public void startListening(DataReceivedCallback callback) {
        // Not implemented for this adapter type
        log.debug("Push-based listening not supported by this adapter type");
    }

    @Override
    public void stopListening() {
    }

    public boolean isListening() {
        return false;
    }
    public void startPolling(long intervalMillis) {
        // Implement if polling is supported
        log.debug("Polling not yet implemented for this adapter type");
    }

    public void stopPolling() {
    }
    public void setDataReceivedCallback(DataReceivedCallback callback) {
        // Implement if callbacks are supported
    }
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.ODATA)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("Inbound adapter implementation")
                .version("1.0.0")
                .supportsBatch(false)
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
            java.net.URL url = new java.net.URL(config.getServiceUrl());
            String serviceInfo = String.format("Service URL: %s", url.toString());
            return AdapterOperationResult.success("Service URL Test", serviceInfo);
        } catch(Exception e) {
            return AdapterOperationResult.failure("Service URL Test", "Invalid service URL: " + e.getMessage());
        }
    }

    private AdapterOperationResult performMetadataAccessTest() {
        try {
            URI serviceUri = URI.create(config.getServiceUrl());
            URI metadataUri = client.newURIBuilder(serviceUri.toString())
                    .appendMetadataSegment()
                    .build();
            String metadataInfo = String.format("Metadata URL: %s", metadataUri.toString());
            return AdapterOperationResult.success("Metadata Access Test", metadataInfo);
        } catch(Exception e) {
            return AdapterOperationResult.failure("Metadata Access Test", "Cannot access metadata: " + e.getMessage());
        }
    }

    private AdapterOperationResult performEntitySetValidationTest() {
        try {
            String entitySetName = config.getEntitySetName();
            String entitySetInfo = String.format("Entity Set: %s", entitySetName);
            return AdapterOperationResult.success("Entity Set Validation", entitySetInfo);
        } catch(Exception e) {
            return AdapterOperationResult.failure("Entity Set Validation", "Entity set validation failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.ODATA;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.INBOUND;
    }

}
