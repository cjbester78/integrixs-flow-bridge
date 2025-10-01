package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.config.FtpOutboundAdapterConfig;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Map;
/**
 * FTP Receiver Adapter implementation for FTP file upload and transfer(OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Supports FTP connections, file uploads, batching, and validation.
 */
public class FtpOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(FtpOutboundAdapter.class);


    private final FtpOutboundAdapterConfig config;
    private FTPClient ftpClient;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final List<Object> batchBuffer = new ArrayList<>();
    private long lastBatchFlush = System.currentTimeMillis();

    public FtpOutboundAdapter(FtpOutboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing FTP outbound adapter(outbound) with server: {}: {}",
                config.getServerAddress(), config.getPort());

        try {
            validateConfiguration();

            // For per - file - transfer mode, we don't maintain persistent connection
            if("permanently".equals(config.getConnectionMode())) {
                connectToFtp();

            }
            log.info("FTP outbound adapter initialized successfully");
            return AdapterOperationResult.success("Initialized successfully");
        } catch(Exception e) {
            log.error("Failed to initialize FTP outbound adapter", e);
            return AdapterOperationResult.failure("Initialization failed: " + e.getMessage());
        }
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying FTP outbound adapter");

        // Flush any remaining batch data
        if(config.isEnableBatching() && !batchBuffer.isEmpty()) {
            try {
                flushBatch();
            } catch(Exception e) {
                log.warn("Error flushing batch during shutdown", e);
            }
        }

        disconnectFromFtp();
        batchBuffer.clear();
        return AdapterOperationResult.success("Shutdown successfully");
    }

    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }

    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }

    @Override
    protected AdapterOperationResult performConnectionTest() {
        List<AdapterOperationResult> testResults = new ArrayList<>();

        // Test 1: Basic FTP connectivity
        testResults.add(executeTest(() -> {

            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);

                if(testClient.isConnected() && FTPReply.isPositiveCompletion(testClient.getReplyCode())) {
                    return AdapterOperationResult.success("FTP Connection: Successfully connected to FTP server");
                } else {
                    return AdapterOperationResult.failure("FTP Connection: Failed to connect to FTP server");
                }
            } catch(Exception e) {
                return AdapterOperationResult.failure("FTP Connection: Error - " + e.getMessage());
            } finally {
                if(testClient != null) {
                    try {
                        testClient.disconnect();
                    } catch(Exception e) {
                        // Ignore
                    }
                }
            }
        }));

        // Test 2: Directory access and write permissions
        testResults.add(executeTest(() -> {

            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);

                // Test directory access
                boolean dirExists = testClient.changeWorkingDirectory(config.getTargetDirectory());
                if(!dirExists) {
                    // Try to create directory if configured
                    if(config.isCreateFileDirectory()) {
                        boolean created = testClient.makeDirectory(config.getTargetDirectory());
                        if(created) {
                            testClient.changeWorkingDirectory(config.getTargetDirectory());
                        } else {
                            return AdapterOperationResult.failure("Test failed");
                        }
                    } else {
                        return AdapterOperationResult.failure("Test failed");
                    }
                }

                return AdapterOperationResult.success("Directory Access: Target directory is accessible and writable");
            } catch(Exception e) {
                return AdapterOperationResult.failure("Directory Access: Error - " + e.getMessage());
            } finally {
                if(testClient != null) {
                    try {
                        testClient.disconnect();
                    } catch(Exception e) {
                        // Ignore
                    }
                }
            }
        }));

        // Test 3: File upload test
        testResults.add(executeTest(() -> {

            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);
                testClient.changeWorkingDirectory(config.getTargetDirectory());

                // Test file upload
                String testFileName = "test_upload_" + System.currentTimeMillis() + ".tmp";
                byte[] testContent = "test content".getBytes();

                try(ByteArrayInputStream bais = new ByteArrayInputStream(testContent)) {
                    boolean uploaded = testClient.storeFile(testFileName, bais);

                    if(uploaded) {
                        // Clean up test file
                        testClient.deleteFile(testFileName);
                        return AdapterOperationResult.success("File Upload: Successfully uploaded and deleted test file");
                    } else {
                        return AdapterOperationResult.failure("File Upload: Failed to upload test file");
                    }
                }
            } catch(Exception e) {
                return AdapterOperationResult.failure("File Upload: Error - " + e.getMessage());
            } finally {
                if(testClient != null) {
                    try {
                        testClient.disconnect();
                    } catch(Exception e) {
                        // Ignore
                    }
                }
            }
        }));

        // Combine test results
        boolean allPassed = testResults.stream().allMatch(AdapterOperationResult::isSuccess);
        if(allPassed) {
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

    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return performSend(request.getPayload());
        } catch(Exception e) {
            return AdapterOperationResult.failure("Send failed: " + e.getMessage());
        }
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
        long successCount = results.stream().filter(AdapterOperationResult::isSuccess).count();
        long failureCount = results.size() - successCount;

        if(failureCount == 0) {
            return AdapterOperationResult.success("All " + successCount + " requests sent successfully");
        } else {
            return AdapterOperationResult.success(String.format("%d sent, %d failed", successCount, failureCount));
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> sendBatchAsync(List<SendRequest> requests) {
        return CompletableFuture.supplyAsync(() -> sendBatch(requests));
    }

    @Override
    public boolean supportsBatchOperations() {
        return true;
    }

    @Override
    public int getMaxBatchSize() {
        return config.getBatchSize() != null ? config.getBatchSize() : 100;
    }

    protected AdapterOperationResult performSend(Object payload) throws Exception {
        // For FTP Receiver(outbound), this method uploads data TO FTP server
        if(config.isEnableBatching()) {
            return addToBatch(payload);
        } else {
            return uploadToFtp(payload);
        }
    }

    private AdapterOperationResult addToBatch(Object payload) throws Exception {
        synchronized(batchBuffer) {
            batchBuffer.add(payload);

            boolean shouldFlush = false;

            // Check size - based flushing
            if("SIZE_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                if(config.getBatchSize() != null && batchBuffer.size() >= config.getBatchSize()) {
                    shouldFlush = true;
                }
            }

            // Check time - based flushing
            if("TIME_BASED".equals(config.getBatchStrategy()) || "MIXED".equals(config.getBatchStrategy())) {
                long timeSinceLastFlush = System.currentTimeMillis() - lastBatchFlush;
                if(timeSinceLastFlush >= config.getBatchTimeoutMs()) {
                    shouldFlush = true;
                }
            }

            if(shouldFlush) {
                return flushBatch();
            } else {
                return AdapterOperationResult.success(null,
                        String.format("Added to batch(%d/%d items)",
                                batchBuffer.size(),
                                config.getBatchSize() != null ? config.getBatchSize() : "unlimited"));
            }
        }
    }
    private AdapterOperationResult flushBatch() throws Exception {
        synchronized(batchBuffer) {
            if(batchBuffer.isEmpty()) {
                return AdapterOperationResult.success(null, "No items in batch to flush");
            }

            List<Object> itemsToUpload = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
            lastBatchFlush = System.currentTimeMillis();

            return uploadBatchToFtp(itemsToUpload);
        }
    }

    private AdapterOperationResult uploadBatchToFtp(List<Object> items) throws Exception {
        String fileName = generateBatchFileName();

        // Combine all items into single file content
        StringBuilder batchContent = new StringBuilder();
        for(int i = 0; i < items.size(); i++) {
            String itemContent = convertToString(items.get(i));
            batchContent.append(itemContent);
            if(i < items.size() - 1) {
                batchContent.append(System.lineSeparator());
            }
        }

        return uploadContentToFtp(fileName, batchContent.toString().getBytes("UTF-8"), true, items.size());
    }
    private AdapterOperationResult uploadToFtp(Object payload) throws Exception {
        String fileName = generateFileName(payload);
        byte[] content = convertToBytes(payload);

        return uploadContentToFtp(fileName, content, false, 1);
    }

    private AdapterOperationResult uploadContentToFtp(String fileName, byte[] content, boolean isBatch, int itemCount) throws Exception {
        FTPClient client = null;
        String uploadPath = null;

        try {
            client = getOrCreateConnection();

            // Change to target directory
            if(!client.changeWorkingDirectory(config.getTargetDirectory())) {
                if(config.isCreateFileDirectory()) {
                    createDirectoryPath(client, config.getTargetDirectory());
                    if(!client.changeWorkingDirectory(config.getTargetDirectory())) {
                        throw new AdapterException("Failed to change to target directory after creation: " + config.getTargetDirectory());
                    }
                } else {
                    throw new AdapterException("Target directory does not exist and createFileDirectory is false: " + config.getTargetDirectory());
                }
            }

            // Use temporary file for atomic upload if configured
            String uploadFileName = fileName;
            if("temp - then - move".equals(config.getFilePlacement())) {
                uploadFileName = fileName + config.getTempFileExtension();
            }

            // Upload file
            client.setFileType(FTP.BINARY_FILE_TYPE);
            long bytesUploaded = 0;

            try(ByteArrayInputStream bais = new ByteArrayInputStream(content)) {
                boolean uploaded = client.storeFile(uploadFileName, bais);

                if(!uploaded) {
                    throw new AdapterException("File upload failed: " + fileName + ", FTP reply: " + client.getReplyString());
                }

                bytesUploaded = content.length;
            }

            // Move from temporary name to final name if atomic upload
            if("temp - then - move".equals(config.getFilePlacement())) {
                boolean renamed = client.rename(uploadFileName, fileName);
                if(!renamed) {
                    // Try to clean up temp file
                    client.deleteFile(uploadFileName);
                    throw new AdapterException("Failed to rename temporary file: " + uploadFileName + " to " + fileName);
                }
            }

            uploadPath = config.getTargetDirectory() + "/" + fileName;

            // Validate upload if configured
            if(config.isValidateBeforeUpload()) {
                validateUpload(client, fileName, content.length);
            }

            log.info("FTP outbound adapter uploaded {} bytes to file: {}", bytesUploaded, uploadPath);

            String message = isBatch ?
                    String.format("Successfully uploaded batch of %d items(%d bytes) to FTP file", itemCount, bytesUploaded) :
                    String.format("Successfully uploaded %d bytes to FTP file", bytesUploaded);

            AdapterOperationResult result = AdapterOperationResult.success(uploadPath, message);
            // Data would be included here

            return result;
        } catch(Exception e) {
            // Clean up on error
            if(ftpClient == null || ftpClient != client) {
                disconnectClient(client);
            }
            throw e;
        }
    }

    private byte[] convertToBytes(Object payload) throws Exception {
        if(payload == null) {
            return handleEmptyMessage();
        }

        if(payload instanceof byte[]) {
            return(byte[]) payload;
        }

        if(payload instanceof String) {
            return((String) payload).getBytes(
                    config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
        }

        if(payload instanceof Map || payload instanceof Collection) {
            // Convert to JSON or formatted string
            String jsonStr = payload.toString(); // Simple implementation
            return jsonStr.getBytes(
                    config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
        }

        return payload.toString().getBytes(
                config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
    }

    private String convertToString(Object payload) throws Exception {
        if(payload == null) {
            String emptyHandling = config.getEmptyMessageHandling();
            switch(emptyHandling.toLowerCase()) {
                case "ignore":
                    return "";
                case "error":
                    throw new AdapterException("Empty message not allowed");
                case "process":
                default:
                    return "";
            }
        }

        if(payload instanceof String) {
            return(String) payload;
        }

        if(payload instanceof byte[]) {
            return new String((byte[]) payload,
                    config.getFileEncoding() != null ? config.getFileEncoding() : "UTF-8");
        }

        return payload.toString();
    }

    private byte[] handleEmptyMessage() throws Exception {
        String handling = config.getEmptyMessageHandling();

        switch(handling.toLowerCase()) {
            case "ignore":
                return new byte[0];
            case "error":
                throw new AdapterException("Empty message not allowed");
            default:
                return new byte[0];
        }
    }

    private String generateFileName(Object payload) throws Exception {
        // Use file name pattern if configured
        if(config.getFileNamingPattern() != null && !config.getFileNamingPattern().isEmpty()) {
            return processTemplate(config.getFileNamingPattern(), payload);
        }

        // Check if payload has filename hint
        if(payload instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) payload;
            if(map.containsKey("filename")) {
                return String.valueOf(map.get("filename"));
            }
        }

        // Generate default filename
        String baseName = "file_" + System.currentTimeMillis();

        if(config.isIncludeTimestamp()) {
            String timestamp = DateTimeFormatter.ofPattern(config.getTimestampFormat()).format(LocalDateTime.now());
            baseName = "file_" + timestamp;
        }

        return baseName + ".txt";
    }

    private String generateBatchFileName() {
        String baseName = String.format("batch_%s_%d",
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()),
                batchCounter.incrementAndGet());

        return baseName + ".txt";
    }

    private String processTemplate(String template, Object payload) {
        String result = template;

        // Replace common placeholders
        result = result.replace("${timestamp}", String.valueOf(System.currentTimeMillis()));
        result = result.replace("${datetime}", DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()));
        result = result.replace("${date}", DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()));
        result = result.replace("${time}", DateTimeFormatter.ofPattern("HHmmss").format(LocalDateTime.now()));

        // Replace payload - specific placeholders if payload is available
        if(payload instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) payload;
            for(Map.Entry<?, ?> entry : map.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if(result.contains(placeholder)) {
                    result = result.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }
        }

        return result;
    }

    private void createBackup(FTPClient client, String fileName) throws Exception {
        if(config.getBackupDirectory() != null) {
            // Check if file exists
            if(fileExists(client, fileName)) {
                String backupFileName = fileName + "_backup_" + System.currentTimeMillis();
                String backupPath = config.getBackupDirectory() + "/" + backupFileName;

                // Create backup directory if needed
                createDirectoryPath(client, config.getBackupDirectory());

                // Copy file to backup location
                boolean renamed = client.rename(fileName, backupPath);
                if(renamed) {
                    log.debug("Created FTP backup: {}", backupPath);
                } else {
                    log.warn("Failed to create FTP backup for: {}", fileName);
                }
            }
        }
    }

    private boolean fileExists(FTPClient client, String fileName) throws Exception {
        return client.listFiles(fileName).length > 0;
    }

    private void createDirectoryPath(FTPClient client, String directoryPath) throws Exception {
        if(directoryPath == null || directoryPath.trim().isEmpty()) {
            return;
        }

        String[] pathParts = directoryPath.split("/");
        StringBuilder currentPath = new StringBuilder();

        for(String part : pathParts) {
            if(part.isEmpty()) continue;

            currentPath.append("/").append(part);

            if(!client.changeWorkingDirectory(currentPath.toString())) {
                boolean created = client.makeDirectory(currentPath.toString());
                if(!created) {
                    log.warn("Failed to create FTP directory: {}", currentPath.toString());
                }
            }
        }
    }

    private void validateContent(byte[] content) throws Exception {
        if(content.length > config.getMaxFileSize()) {
            throw new AdapterException("Content size exceeds maximum allowed: " + content.length + " > " + config.getMaxFileSize());
        }

        // Additional validation based on checksum if configured
        if(!"none".equals(config.getChecksumValidation())) {
            String checksum = generateChecksum(content);
            log.debug("Content checksum( {}): {}", config.getChecksumValidation(), checksum);
        }
    }

    private void validateUpload(FTPClient client, String fileName, long expectedSize) throws Exception {
        // Verify file was uploaded correctly
        long actualSize = client.listFiles(fileName)[0].getSize();
        if(actualSize != expectedSize) {
            throw new AdapterException("File size mismatch after upload: expected " + expectedSize + " but got " + actualSize);
        }
    }

    private String generateChecksum(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getChecksumValidation().toUpperCase());
        digest.update(content);

        StringBuilder result = new StringBuilder();
        for(byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private FTPClient getOrCreateConnection() throws Exception {
        if("permanently".equals(config.getConnectionMode())) {
            if(ftpClient == null || !ftpClient.isConnected()) {
                connectToFtp();
            }
            return ftpClient;
        } else {
            // Create new connection for each operation
            FTPClient client = createFtpClient();
            connectClient(client);
            return client;
        }
    }

    private void connectToFtp() throws Exception {
        if(ftpClient != null) {
            disconnectFromFtp();
        }

        ftpClient = createFtpClient();
        connectClient(ftpClient);
    }

    private FTPClient createFtpClient() throws Exception {
        FTPClient client = new FTPClient();

        // Configure timeouts
        int timeout = Integer.parseInt(config.getTimeout());
        client.setConnectTimeout(timeout);
        client.setDataTimeout(timeout);
        client.setDefaultTimeout(timeout);

        // Configure buffer size
        client.setBufferSize(config.getTransferBufferSize());

        return client;
    }

    private void connectClient(FTPClient client) throws Exception {
        // Connect to server
        int port = Integer.parseInt(config.getPort());
        client.connect(config.getServerAddress(), port);

        // Check connection reply
        int reply = client.getReplyCode();
        if(!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new AdapterException("FTP server refused connection: " + client.getReplyString());
        }

        // Login
        boolean loginSuccess = client.login(config.getUserName(), config.getPassword());
        if(!loginSuccess) {
            client.disconnect();
            throw new AdapterException("FTP login failed: " + client.getReplyString());
        }

        // Configure passive mode
        if(config.isEnablePassiveMode()) {
            client.enterLocalPassiveMode();
        } else {
            client.enterLocalActiveMode();
        }

        // Set binary mode for file transfers
        client.setFileType(FTP.BINARY_FILE_TYPE);

        log.debug("Successfully connected to FTP server: {}: {}",
                config.getServerAddress(), config.getPort());
    }

    private void disconnectFromFtp() {
        if(ftpClient != null) {
            disconnectClient(ftpClient);
            ftpClient = null;
        }
    }

    private void disconnectClient(FTPClient client) {
        if(client != null && client.isConnected()) {
            try {
                client.logout();
                client.disconnect();
            } catch(Exception e) {
                log.warn("Error disconnecting from FTP", e);
            }
        }
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getServerAddress() == null || config.getServerAddress().trim().isEmpty()) {
            throw new AdapterException("FTP server address is required", null);
        }
        if(config.getPassword() == null) {
            throw new AdapterException("FTP password is required", null);
        }
    }

    protected long getPollingIntervalMs() {
        // FTP receivers typically don't poll, they push files
        return 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("FTP Receiver(Outbound): %s:%s, User: %s, Dir: %s, Construction: %s, Batching: %s",
                config.getServerAddress(),
                config.getPort(),
                config.getUserName(),
                config.getTargetDirectory(),
                config.getFileConstructionMode(),
                config.isEnableBatching() ? "Enabled" : "Disabled");
    }

    private AdapterOperationResult executeTest(java.util.concurrent.Callable<AdapterOperationResult> test) {
        try {
            return test.call();
        } catch(Exception e) {
            return AdapterOperationResult.failure("Test failed: " + e.getMessage());
        }
    }


    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.FTP;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }
}
