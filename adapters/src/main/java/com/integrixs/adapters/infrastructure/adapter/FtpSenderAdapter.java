package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.core.AdapterException;

import com.integrixs.adapters.config.FtpSenderAdapterConfig;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.integrixs.adapters.domain.model.*;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * FTP Sender Adapter implementation for FTP file polling and retrieval (INBOUND).
 * Follows middleware convention: Sender = receives data FROM external systems.
 * Supports FTP connections, file polling, pattern matching, and post-processing.
 */
@Slf4j
public class FtpSenderAdapter extends AbstractAdapter implements com.integrixs.adapters.domain.port.SenderAdapterPort {
    
    private final FtpSenderAdapterConfig config;
    private final Map<String, String> processedFiles = new ConcurrentHashMap<>();
    private Pattern filePattern;
    private Pattern exclusionPattern;
    private FTPClient ftpClient;
    
    public FtpSenderAdapter(FtpSenderAdapterConfig config) {
        this.config = config;
    }
    
    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing FTP sender adapter (inbound) with server: {}:{}", 
                config.getServerAddress(), config.getPort());
        
        try {
            validateConfiguration();
            initializePatterns();
            
            // For per-file-transfer mode, we don't maintain persistent connection
            if ("permanently".equals(config.getConnectionMode())) {
                connectToFtp();
            }
            
            log.info("FTP sender adapter initialized successfully");
            return AdapterOperationResult.success("FTP sender adapter initialized");
        } catch (Exception e) {
            log.error("Failed to initialize FTP sender adapter", e);
            return AdapterOperationResult.failure("Initialization failed: " + e.getMessage());
        }
    }
    
    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying FTP sender adapter");
        
        disconnectFromFtp();
        processedFiles.clear();
        return AdapterOperationResult.success("FTP sender adapter destroyed");
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
                
                if (testClient.isConnected() && FTPReply.isPositiveCompletion(testClient.getReplyCode())) {
                    return AdapterOperationResult.success("FTP Connection: Successfully connected to FTP server");
                } else {
                    return AdapterOperationResult.failure("FTP Connection: Failed to connect");
                }
            } catch (Exception e) {
                return AdapterOperationResult.failure("Test failed: " + e.getMessage());
            } finally {
                if (testClient != null) {
                    try { testClient.disconnect(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Test 2: Directory access and permissions
        testResults.add(executeTest(() -> {
            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);
                
                // Test directory access
                boolean dirExists = testClient.changeWorkingDirectory(config.getSourceDirectory());
                if (!dirExists) {
                    return AdapterOperationResult.failure("Directory Access: Source directory does not exist");
                }
                
                // Test file listing
                FTPFile[] files = testClient.listFiles();
                int fileCount = files != null ? files.length : 0;
                
                return AdapterOperationResult.success("Directory Access: Source directory is accessible (" + fileCount + " files found)");
            } catch (Exception e) {
                return AdapterOperationResult.failure("Test failed: " + e.getMessage());
            } finally {
                if (testClient != null) {
                    try { testClient.disconnect(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Test 3: Pattern matching test
        testResults.add(executeTest(() -> {
            FTPClient testClient = null;
            try {
                testClient = createFtpClient();
                connectClient(testClient);
                testClient.changeWorkingDirectory(config.getSourceDirectory());
                
                FTPFile[] files = testClient.listFiles();
                long matchingFiles = files != null ? 
                        Arrays.stream(files)
                                .filter(file -> file.isFile())
                                .filter(this::matchesFilePattern)
                                .count() : 0;
                
                return AdapterOperationResult.success("Pattern Matching: Found " + matchingFiles + " files matching configured patterns");
            } catch (Exception e) {
                return AdapterOperationResult.failure("Test failed: " + e.getMessage());
            } finally {
                if (testClient != null) {
                    try { testClient.disconnect(); } catch (Exception ignored) {}
                }
            }
        }));
        
        // Combine all test results
        boolean allPassed = testResults.stream().allMatch(AdapterOperationResult::isSuccess);
        if (allPassed) {
            return AdapterOperationResult.success("All FTP connection tests passed");
        } else {
            String failedTests = testResults.stream()
                    .filter(r -> !r.isSuccess())
                    .map(AdapterOperationResult::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown failures");
            return AdapterOperationResult.failure("Some FTP tests failed: " + failedTests);
        }
    }
    
    protected AdapterOperationResult performSend(Object payload, Map<String, Object> headers) throws Exception {
        // For FTP Sender (inbound), "send" means polling/retrieving files FROM FTP server
        return pollForFiles();
    }
    
    private AdapterOperationResult pollForFiles() throws Exception {
        List<Map<String, Object>> processedFiles = new ArrayList<>();
        FTPClient client = null;
        
        try {
            client = getOrCreateConnection();
            
            // Change to source directory
            if (!client.changeWorkingDirectory(config.getSourceDirectory())) {
                throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                        "Cannot access source directory: " + config.getSourceDirectory());
            }
            
            // List files
            FTPFile[] files = client.listFiles();
            if (files == null) {
                log.warn("No files returned from FTP server directory listing");
                return AdapterOperationResult.success("No files found in directory");
            }
            
            // Filter and sort files
            List<FTPFile> eligibleFiles = Arrays.stream(files)
                    .filter(FTPFile::isFile)
                    .filter(this::matchesFilePattern)
                    .filter(this::shouldProcessFile)
                    .collect(Collectors.toList());
            
            sortFiles(eligibleFiles);
            
            // Process files (respecting any configured limits)
            for (FTPFile file : eligibleFiles) {
                try {
                    if (shouldProcessFile(file)) {
                        Map<String, Object> fileData = processFile(client, file);
                        if (fileData != null) {
                            processedFiles.add(fileData);
                            handlePostProcessing(client, file);
                            
                            // Mark as processed
                            this.processedFiles.put(file.getName(), String.valueOf(System.currentTimeMillis()));
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing FTP file: {}", file.getName(), e);
                    
                    if (!config.isContinueOnError()) {
                        throw new AdapterException.ProcessingException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                                "FTP file processing failed for " + file.getName() + ": " + e.getMessage(), e);
                    }
                }
            }
            
        } finally {
            if ("per-file-transfer".equals(config.getConnectionMode()) && client != null) {
                disconnectClient(client);
            }
        }
        
        log.info("FTP sender adapter polled {} files from server", processedFiles.size());
        
        return AdapterOperationResult.success(String.format("Retrieved %d files from FTP server", processedFiles.size()));
    }
    
    private Map<String, Object> processFile(FTPClient client, FTPFile file) throws Exception {
        // Check file age
        if (config.getMinFileAge() > 0) {
            long fileAge = System.currentTimeMillis() - file.getTimestamp().getTimeInMillis();
            if (fileAge < config.getMinFileAge()) {
                log.debug("FTP file {} is too young, skipping", file.getName());
                return null;
            }
        }
        
        // Size validation
        long fileSize = file.getSize();
        if (fileSize > config.getMaxFileSize()) {
            log.debug("FTP file {} size {} exceeds maximum {}, skipping", 
                    file.getName(), fileSize, config.getMaxFileSize());
            return null;
        }
        
        // Handle empty files
        if (fileSize == 0) {
            return handleEmptyFile(file);
        }
        
        // Download file content
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileName", file.getName());
        fileData.put("fileSize", fileSize);
        fileData.put("lastModified", file.getTimestamp().getTime());
        fileData.put("ftpPath", config.getSourceDirectory() + "/" + file.getName());
        
        // Download file content
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            client.setFileType(FTP.BINARY_FILE_TYPE);
            
            boolean success = client.retrieveFile(file.getName(), baos);
            if (!success) {
                throw new AdapterException.ProcessingException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                        "Failed to download file: " + file.getName() + ", FTP reply: " + client.getReplyString());
            }
            
            byte[] content = baos.toByteArray();
            
            if (config.isLogFileContent()) {
                // Convert to string for logging
                String contentStr = new String(content, 
                        config.getFileEncoding() != null ? 
                                config.getFileEncoding() : "UTF-8");
                fileData.put("content", contentStr);
            } else {
                fileData.put("content", content);
            }
            
            // Generate checksum if configured
            if (config.isValidateFileIntegrity()) {
                String checksum = generateChecksum(content);
                fileData.put("checksum", checksum);
                
                // Check for duplicates
                if (config.isEnableDuplicateHandling()) {
                    if (isDuplicate(file.getName(), checksum)) {
                        log.debug("FTP file {} is a duplicate, skipping", file.getName());
                        return null;
                    }
                }
            }
        }
        
        return fileData;
    }
    private void handlePostProcessing(FTPClient client, FTPFile file) throws Exception {
        String processingMode = config.getProcessingMode();
        
        switch (processingMode.toLowerCase()) {
            case "delete":
                boolean deleted = client.deleteFile(file.getName());
                if (!deleted) {
                    log.warn("Failed to delete processed FTP file: {}, reply: {}", 
                            file.getName(), client.getReplyString());
                } else {
                    log.debug("Deleted processed FTP file: {}", file.getName());
                }
                break;
                
            case "archive":
                if (config.getArchiveDirectory() != null) {
                    String archivePath = config.getArchiveDirectory() + "/" + file.getName();
                    boolean renamed = client.rename(file.getName(), archivePath);
                    if (!renamed) {
                        log.warn("Failed to archive FTP file: {} to {}, reply: {}", 
                                file.getName(), archivePath, client.getReplyString());
                    } else {
                        log.debug("Archived FTP file to: {}", archivePath);
                    }
                }
                break;
                
            case "move":
                if (config.getProcessedDirectory() != null) {
                    String movePath = config.getProcessedDirectory() + "/" + file.getName();
                    boolean renamed = client.rename(file.getName(), movePath);
                    if (!renamed) {
                        log.warn("Failed to move FTP file: {} to {}, reply: {}", 
                                file.getName(), movePath, client.getReplyString());
                    } else {
                        log.debug("Moved FTP file to: {}", movePath);
                    }
                }
                break;
                
            default:
                log.debug("No post-processing configured for FTP file: {}", file.getName());
        }
    }
    
    private boolean shouldProcessFile(FTPFile file) {
        // Check if already processed
        if (processedFiles.containsKey(file.getName())) {
            return false;
        }
        
        // Check exclusion patterns
        if (exclusionPattern != null && exclusionPattern.matcher(file.getName()).matches()) {
            return false;
        }
        
        return true;
    }
    
    private boolean matchesFilePattern(FTPFile file) {
        String fileName = file.getName();
        
        // Simple file name match
        if (config.getFileName() != null && !config.getFileName().isEmpty() && !"*".equals(config.getFileName())) {
            if (config.getFileName().contains("*") || config.getFileName().contains("?")) {
                // Simple glob pattern
                String regex = config.getFileName()
                        .replace(".", "\\.")
                        .replace("*", ".*")
                        .replace("?", ".");
                return fileName.matches(regex);
            } else {
                return fileName.equals(config.getFileName());
            }
        }
        
        // If no specific pattern, match all files
        return true;
    }
    
    private void sortFiles(List<FTPFile> files) {
        String sorting = config.getSorting();
        if (sorting == null || "none".equals(sorting)) {
            return;
        }
        
        switch (sorting.toLowerCase()) {
            case "name":
                files.sort(Comparator.comparing(FTPFile::getName));
                break;
            case "date":
                files.sort(Comparator.comparing(FTPFile::getTimestamp));
                break;
            case "size":
                files.sort(Comparator.comparing(FTPFile::getSize));
                break;
        }
    }
    
    private Map<String, Object> handleEmptyFile(FTPFile file) throws Exception {
        String handling = config.getEmptyFileHandling();
        
        switch (handling.toLowerCase()) {
            case "ignore":
                log.debug("Ignoring empty FTP file: {}", file.getName());
                return null;
            case "error":
                throw new AdapterException.ValidationException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                        "Empty file not allowed: " + file.getName());
            case "process":
            default:
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("fileName", file.getName());
                fileData.put("fileSize", 0L);
                fileData.put("lastModified", file.getTimestamp().getTime());
                fileData.put("ftpPath", config.getSourceDirectory() + "/" + file.getName());
                fileData.put("content", "");
                return fileData;
        }
    }
    
    private String generateChecksum(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getChecksumAlgorithm());
        digest.update(content);
        
        StringBuilder result = new StringBuilder();
        for (byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private boolean isDuplicate(String fileName, String checksum) {
        // Simple duplicate detection based on checksum
        return processedFiles.containsValue(checksum);
    }
    
    private FTPClient getOrCreateConnection() throws Exception {
        if ("permanently".equals(config.getConnectionMode())) {
            if (ftpClient == null || !ftpClient.isConnected()) {
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
        if (ftpClient != null) {
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
        
        return client;
    }
    
    private void connectClient(FTPClient client) throws Exception {
        // Connect to server
        int port = Integer.parseInt(config.getPort());
        client.connect(config.getServerAddress(), port);
        
        // Check connection reply
        int reply = client.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            client.disconnect();
            throw new AdapterException.ConnectionException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                    "FTP server refused connection: " + client.getReplyString());
        }
        
        // Login
        boolean loginSuccess = client.login(config.getUserName(), config.getPassword());
        if (!loginSuccess) {
            client.disconnect();
            throw new AdapterException.AuthenticationException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                    "FTP login failed: " + client.getReplyString());
        }
        
        // Configure passive mode
        if (config.isEnablePassiveMode()) {
            client.enterLocalPassiveMode();
        } else {
            client.enterLocalActiveMode();
        }
        
        // Set binary mode for file transfers
        client.setFileType(FTP.BINARY_FILE_TYPE);
        
        log.debug("Successfully connected to FTP server: {}:{}", 
                config.getServerAddress(), config.getPort());
    }
    
    private void disconnectFromFtp() {
        if (ftpClient != null) {
            disconnectClient(ftpClient);
            ftpClient = null;
        }
    }
    
    private void disconnectClient(FTPClient client) {
        if (client != null && client.isConnected()) {
            try {
                client.logout();
                client.disconnect();
            } catch (Exception e) {
                log.warn("Error disconnecting from FTP server", e);
            }
        }
    }
    
    private void validateConfiguration() throws AdapterException.ConfigurationException {
        if (config.getServerAddress() == null || config.getServerAddress().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, "FTP server address is required");
        }
        if (config.getUserName() == null || config.getUserName().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, "FTP username is required");
        }
        if (config.getPassword() == null) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, "FTP password is required");
        }
        if (config.getSourceDirectory() == null || config.getSourceDirectory().trim().isEmpty()) {
            throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, "FTP source directory is required");
        }
    }
    
    private void initializePatterns() throws Exception {
        // Initialize exclusion pattern if configured
        if (config.getExclusionMask() != null && !config.getExclusionMask().trim().isEmpty()) {
            try {
                exclusionPattern = Pattern.compile(config.getExclusionMask());
            } catch (Exception e) {
                throw new AdapterException.ConfigurationException(AdapterConfiguration.AdapterTypeEnum.FTP, 
                        "Invalid exclusion pattern: " + config.getExclusionMask(), e);
            }
        }
    }
    
    @Override
    public String getConfigurationSummary() {
        return String.format("FTP Sender (Inbound): %s:%s, User: %s, Dir: %s, Polling: %sms, Processing: %s", 
                config.getServerAddress(),
                config.getPort(),
                config.getUserName(),
                config.getSourceDirectory(),
                config.getPollingInterval(),
                config.getProcessingMode());
    }
    
    // Missing AbstractAdapter methods
    @Override
    protected AdapterOperationResult performStart() {
        return AdapterOperationResult.success("Started");
    }
    
    @Override
    protected AdapterOperationResult performStop() {
        return AdapterOperationResult.success("Stopped");
    }
    
    protected AdapterOperationResult performReceive() throws Exception {
        return pollForFiles();
    }
    
    protected AdapterOperationResult performReceive(Object criteria) throws Exception {
        return pollForFiles();
    }
    
    protected long getPollingIntervalMs() {
        return config.getPollingInterval() != null ? Long.parseLong(config.getPollingInterval()) : 30000L;
    }
    
    @Override
    public com.integrixs.adapters.domain.model.AdapterMetadata getMetadata() {
        return com.integrixs.adapters.domain.model.AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.FTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.SENDER)
                .description("FTP Sender adapter implementation")
                .version("1.0.0")
                .supportsBatch(false)
                .supportsAsync(true)
                .build();
    }
    
    // SenderAdapterPort implementation
    @Override
    public AdapterOperationResult fetch(com.integrixs.adapters.domain.model.FetchRequest request) {
        try {
            return performSend(request.getParameters() != null ? request.getParameters().get("payload") : null, request.getParameters());
        } catch (Exception e) {
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }
    
    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(com.integrixs.adapters.domain.model.FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }
    
    private AdapterOperationResult executeTest(java.util.concurrent.Callable<AdapterOperationResult> test) {
        try {
            return test.call();
        } catch (Exception e) {
            return AdapterOperationResult.failure("Test execution failed: " + e.getMessage());
        }
    }
    
    @Override
    public void startListening(com.integrixs.adapters.domain.port.SenderAdapterPort.DataReceivedCallback callback) {
        // Not implemented for FTP adapter
        throw new UnsupportedOperationException("FTP adapter does not support push-based listening");
    }
    
    @Override
    public void stopListening() {
        // Not implemented for FTP adapter
    }
    
    @Override
    public boolean isListening() {
        return false;
    }
    
    public void startPolling(long intervalMillis) {
        // Implement if needed
        throw new UnsupportedOperationException("Polling not implemented");
    }
    
    public void stopPolling() {
        // Implement if needed
    }
    
    public void registerDataCallback(com.integrixs.adapters.domain.port.SenderAdapterPort.DataReceivedCallback callback) {
        // Implement if needed
    }
    
    public AdapterOperationResult fetchBatch(List<FetchRequest> requests) {
        // For FTP adapter, batch fetch is same as single fetch
        return fetch(requests.get(0));
    }
    
    public CompletableFuture<AdapterOperationResult> fetchBatchAsync(List<FetchRequest> requests) {
        return CompletableFuture.supplyAsync(() -> fetchBatch(requests));
    }
    
    public boolean supportsBatchOperations() {
        return true;
    }
    
    public int getMaxBatchSize() {
        return 100; // Default batch size
    }



    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.FTP;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.SENDER;
    }

}
