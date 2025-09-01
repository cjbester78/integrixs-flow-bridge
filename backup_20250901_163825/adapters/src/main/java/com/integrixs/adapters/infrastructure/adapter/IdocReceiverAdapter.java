package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.core.AdapterException;

import com.integrixs.adapters.domain.model.*;
import java.util.concurrent.CompletableFuture;
import com.integrixs.adapters.domain.port.ReceiverAdapterPort;
import java.util.Map;
import com.integrixs.adapters.config.IdocReceiverAdapterConfig;
import java.util.*;
import java.text.SimpleDateFormat;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * IDoc Receiver Adapter implementation for SAP IDoc sending (OUTBOUND).
 * Follows middleware convention: Receiver = sends data TO external systems.
 * Sends IDocs to SAP systems.
 * 
 * Note: This is a simulation. Real implementation would require SAP JCo IDoc libraries.
 */
@Slf4j
public class IdocReceiverAdapter extends AbstractAdapter implements ReceiverAdapterPort {
    
    private final IdocReceiverAdapterConfig config;
    private boolean connectionEstablished = false;
    
    public IdocReceiverAdapter(IdocReceiverAdapterConfig config) {
        super();
        this.config = config;
    }
    
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing IDoc receiver adapter (outbound) for system: {}", config.getSystemId());
        
        try {
            validateConfiguration();
            // In real implementation, would:
            // 1. Initialize SAP JCo IDoc environment
            // 2. Create destination configuration
            // 3. Establish IDoc connection
            connectionEstablished = true;
        } catch (Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }
        
        log.info("IDoc receiver adapter initialized successfully");
        return AdapterOperationResult.success("IDoc receiver adapter initialized successfully");
    }
    
    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying IDoc receiver adapter");
        if (connectionEstablished) {
            // In real implementation, would close IDoc connections
            connectionEstablished = false;
        }
        return AdapterOperationResult.success("IDoc receiver adapter shutdown successfully");
    }
    
    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();
        
        // Test 1: SAP system connectivity for IDoc
        testResults.add(executeTest(() -> {
            String connectionInfo = String.format("System: %s, Host: %s:%s", 
                    config.getSystemId(), config.getApplicationServerHost(), config.getSystemNumber());
            
            if (config.getApplicationServerHost() == null || config.getApplicationServerHost().isEmpty()) {
                return AdapterOperationResult.failure("Application server host not configured");
            }
            return AdapterOperationResult.success(connectionInfo);
        }));
        
        // Test 2: IDoc port configuration
        testResults.add(executeTest(() -> {
            if (config.getIdocPort() == null || config.getIdocPort().isEmpty()) {
                return AdapterOperationResult.failure("IDoc port not configured");
            }
            return AdapterOperationResult.success("IDoc port configured: " + config.getIdocPort());
        }));
        
        // Test 3: Packet size configuration
        testResults.add(executeTest(() -> {
            String info = String.format("Packet size: %d, Queue processing: %s", 
                    config.getPacketSize(), 
                    config.isQueueProcessing() ? "Enabled" : "Disabled");
            return AdapterOperationResult.success(info);
        }));
        
        // Combine test results
        boolean allPassed = testResults.stream().allMatch(AdapterOperationResult::isSuccess);
        if (allPassed) {
            return AdapterOperationResult.success("All connection tests passed");
        } else {
            String failedTests = testResults.stream()
                    .filter(r -> !r.isSuccess())
                    .map(AdapterOperationResult::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown failures");
            return AdapterOperationResult.failure("Some tests failed: " + failedTests);
        }
    }
    
    // ReceiverAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return sendIdoc(request.getPayload());
        } catch (Exception e) {
            return AdapterOperationResult.failure("Failed to send IDoc: " + e.getMessage());
        }
    }
    
    private AdapterOperationResult sendIdoc(Object payload) throws Exception {
        if (payload == null) {
            throw new AdapterException.ValidationException(AdapterConfiguration.AdapterTypeEnum.IDOC, "Payload cannot be null");
        }
        
        try {
            Map<String, Object> responseData = new HashMap<>();
            List<String> sentIdocNumbers = new ArrayList<>();
            
            if (payload instanceof Map) {
                // Single IDoc
                Map<String, Object> idocResult = processSingleIdoc((Map<String, Object>) payload);
                sentIdocNumbers.add((String) idocResult.get("idocNumber"));
                responseData = idocResult;
            } else if (payload instanceof Collection) {
                // Multiple IDocs (packet)
                Collection<?> idocCollection = (Collection<?>) payload;
                List<Map<String, Object>> results = new ArrayList<>();
                for (Object idoc : idocCollection) {
                    if (idoc instanceof Map) {
                        Map<String, Object> idocResult = processSingleIdoc((Map<String, Object>) idoc);
                        results.add(idocResult);
                        sentIdocNumbers.add((String) idocResult.get("idocNumber"));
                        
                        // Check packet size
                        if (config.getPacketSize() > 0 && results.size() >= config.getPacketSize()) {
                            // Would commit packet in real implementation
                            log.debug("Reached packet size limit: {}", config.getPacketSize());
                        }
                    }
                }
                responseData.put("results", results);
                responseData.put("totalSent", results.size());
            } else {
                throw new AdapterException.ValidationException(AdapterConfiguration.AdapterTypeEnum.IDOC, 
                        "Unsupported payload type: " + payload.getClass().getName());
            }
            
            responseData.put("sentIdocNumbers", sentIdocNumbers);
            responseData.put("timestamp", new Date());
            responseData.put("system", config.getSystemId());
            
            log.info("IDoc receiver adapter sent {} IDoc(s) to SAP", sentIdocNumbers.size());
            return AdapterOperationResult.success(responseData, 
                    String.format("Successfully sent %d IDoc(s) to SAP", sentIdocNumbers.size()));
        } catch (Exception e) {
            log.error("Error sending IDoc", e);
            throw new AdapterException.OperationException(AdapterConfiguration.AdapterTypeEnum.IDOC, 
                    "Failed to send IDoc: " + e.getMessage(), e);
        }
    }
    
    private Map<String, Object> processSingleIdoc(Map<String, Object> idocData) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        // Extract or create control record
        Map<String, Object> controlRecord = (Map<String, Object>) idocData.get("controlRecord");
        if (controlRecord == null) {
            controlRecord = createControlRecord(idocData);
        }
        
        // Set sender/receiver information
        updateControlRecord(controlRecord);
        
        // Extract data records
        List<Map<String, Object>> dataRecords = (List<Map<String, Object>>) idocData.get("dataRecords");
        if (dataRecords == null || dataRecords.isEmpty()) {
            throw new AdapterException.ValidationException(AdapterConfiguration.AdapterTypeEnum.IDOC, 
                    "IDoc must contain at least one data record");
        }
        
        // In real implementation, would:
        // 1. Create IDoc document
        // 2. Set control record fields
        // 3. Add data records with proper segment structure
        // 4. Send IDoc to SAP
        // 5. Get IDoc number from response
        
        // Simulate sending
        String idocNumber = generateIdocNumber();
        result.put("idocNumber", idocNumber);
        result.put("idocType", controlRecord.get("IDOCTYP"));
        result.put("messageType", controlRecord.get("MESTYP"));
        result.put("status", "03"); // IDoc sent
        result.put("dataRecordCount", dataRecords.size());
        
        // Add status record
        Map<String, Object> statusRecord = new HashMap<>();
        statusRecord.put("STATUS", "03");
        statusRecord.put("STATXT", "IDoc sent to SAP system");
        statusRecord.put("CREDAT", new SimpleDateFormat("yyyyMMdd").format(new Date()));
        statusRecord.put("CRETIM", new SimpleDateFormat("HHmmss").format(new Date()));
        result.put("statusRecord", statusRecord);
        
        return result;
    }
    
    private Map<String, Object> createControlRecord(Map<String, Object> idocData) {
        Map<String, Object> control = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
        Date now = new Date();
        
        // Extract from payload or use defaults
        control.put("TABNAM", "EDI_DC40");
        control.put("IDOCTYP", idocData.getOrDefault("idocType", config.getDefaultIdocType()));
        control.put("MESTYP", idocData.getOrDefault("messageType", config.getDefaultMessageType()));
        control.put("CREDAT", dateFormat.format(now));
        control.put("CRETIM", timeFormat.format(now));
        control.put("STATUS", "01"); // IDoc created
        
        return control;
    }
    
    private void updateControlRecord(Map<String, Object> controlRecord) {
        // Set sender information (middleware)
        controlRecord.put("SNDPOR", config.getIdocPort());
        controlRecord.put("SNDPRT", "LS");
        controlRecord.put("SNDPRN", config.getSenderPartner() != null ? 
                config.getSenderPartner() : "MIDDLEWARE");
        
        // Set receiver information (SAP)
        controlRecord.put("RCVPOR", "SAP" + config.getSystemId());
        controlRecord.put("RCVPRT", "LS");
        controlRecord.put("RCVPRN", config.getReceiverPartner() != null ? 
                config.getReceiverPartner() : config.getSystemId());
    }
    
    private String generateIdocNumber() {
        // Generate a 16-digit IDoc number
        return String.format("%016d", System.currentTimeMillis() % 10000000000000000L);
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getSystemId() == null || config.getSystemId().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, "System ID is required");
        }
        if (config.getApplicationServerHost() == null || config.getApplicationServerHost().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, "Application server host is required");
        }
        if (config.getSystemNumber() == null || config.getSystemNumber().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, "System number is required");
        }
        if (config.getClient() == null || config.getClient().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, "Client is required");
        }
        if (config.getUser() == null || config.getUser().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, "User is required");
        }
        if (config.getIdocPort() == null || config.getIdocPort().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.IDOC, "IDoc port is required");
        }
        
        // Set defaults
        if (config.getPacketSize() <= 0) {
            config.setPacketSize(1); // Default to single IDoc processing
        }
    }
    public long getPollingInterval() {
        // IDoc receivers typically don't poll, they send IDocs
        return 0;
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("IDoc Receiver (Outbound): System: %s, Port: %s, Packet Size: %d", 
                config.getSystemId(),
                config.getIdocPort(),
                config.getPacketSize());
    }
    
    @Override
    public CompletableFuture<AdapterOperationResult> sendAsync(SendRequest request) {
        return CompletableFuture.supplyAsync(() -> send(request));
    }
    
    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        List<AdapterOperationResult> results = new ArrayList<>();
        for (SendRequest request : requests) {
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
                .adapterType(AdapterConfiguration.AdapterTypeEnum.IDOC)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.RECEIVER)
                .description("IDoc Receiver adapter implementation")
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
    
    private AdapterOperationResult executeTest(java.util.concurrent.Callable<AdapterOperationResult> test) {
        try {
            return test.call();
        } catch (Exception e) {
            return AdapterOperationResult.failure("Test execution failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.IDOC;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.RECEIVER;
    }

}
