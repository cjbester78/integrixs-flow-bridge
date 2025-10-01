package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.config.SftpInboundAdapterConfig;

import com.jcraft.jsch.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.List;import java.util.concurrent.ConcurrentHashMap;
import java.util.List;import java.util.regex.Pattern;
import java.util.List;import java.util.stream.Collectors;
import java.util.List;import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * SFTP Sender Adapter implementation for SFTP file polling and retrieval(INBOUND).
 * Follows middleware convention: Inbound = receives data FROM external systems.
 * Supports SFTP connections, file polling, pattern matching, and SSH authentication.
 */
public class SftpInboundAdapter extends AbstractAdapter implements InboundAdapterPort {

    private final SftpInboundAdapterConfig config;
    private final Map<String, String> processedFiles = new ConcurrentHashMap<>();
    private Pattern filePattern;
    private Pattern exclusionPattern;
    private Session sshSession;
    private ChannelSftp sftpChannel;

    // Polling mechanism fields
    private final AtomicBoolean polling = new AtomicBoolean(false);
    private ScheduledExecutorService pollingExecutor;
    private InboundAdapterPort.DataReceivedCallback dataCallback;

    public SftpInboundAdapter(SftpInboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing SFTP inbound adapter(inbound) with server: {}: {}",
                config.getServerAddress(), config.getPort());

        try {
            validateConfiguration();
            initializePatterns();

            // For per - file - transfer mode, we don't maintain persistent connection
            if("permanently".equals(config.getConnectionMode())) {
                connectToSftp();
            }
        } catch(Exception e) {
            log.error("Error during initialization", e);
            return AdapterOperationResult.failure("Initialization error: " + e.getMessage());
        }

        log.info("SFTP inbound adapter initialized successfully");
        return AdapterOperationResult.success("Initialized successfully");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying SFTP inbound adapter");

        // Stop polling if active
        stopPolling();

        disconnectFromSftp();
        processedFiles.clear();
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

        // Test 1: Basic SFTP connectivity
        testResults.add(executeTest(() -> {

            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();

                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();

                if(testSession.isConnected() && testChannel.isConnected()) {
                    return AdapterOperationResult.success("Test passed");
                } else {
                    return AdapterOperationResult.success("Test");
                }
            } catch(Exception e) {
                return AdapterOperationResult.failure("Test failed: " + e.getMessage());
            } finally {
                if(testChannel != null && testChannel.isConnected()) {
                    testChannel.disconnect();
                }
                if(testSession != null && testSession.isConnected()) {
                    testSession.disconnect();
                }
            }
        }));

        // Test 2: Directory access and permissions
        testResults.add(executeTest(() -> {

            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();
                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();

                // Test directory access
                testChannel.cd(config.getSourceDirectory());

                // Test file listing
                @SuppressWarnings("unchecked")
                Vector<ChannelSftp.LsEntry> files = testChannel.ls(".");
                int fileCount = files != null ? files.size() : 0;

                return AdapterOperationResult.success("Test passed");
            } catch(Exception e) {
                return AdapterOperationResult.failure("Test failed: " + e.getMessage());
            } finally {
                if(testChannel != null && testChannel.isConnected()) {
                    testChannel.disconnect();
                }
                if(testSession != null && testSession.isConnected()) {
                    testSession.disconnect();
                }
            }
        }));

        // Test 3: Pattern matching test
        testResults.add(executeTest(() -> {

            Session testSession = null;
            ChannelSftp testChannel = null;
            try {
                testSession = createSession();
                testSession.connect();
                testChannel = (ChannelSftp) testSession.openChannel("sftp");
                testChannel.connect();
                testChannel.cd(config.getSourceDirectory());

                @SuppressWarnings("unchecked")
                Vector<ChannelSftp.LsEntry> files = testChannel.ls(".");
                long matchingFiles = files != null ?
                        files.stream()
                                .filter(entry -> !entry.getAttrs().isDir())
                                .filter(entry -> matchesFilePattern(entry.getFilename()))
                                .count() : 0;

                return AdapterOperationResult.success("Test passed");
            } catch(Exception e) {
                return AdapterOperationResult.failure("Test failed: " + e.getMessage());
            } finally {
                if(testChannel != null && testChannel.isConnected()) {
                    testChannel.disconnect();
                }
                if(testSession != null && testSession.isConnected()) {
                    testSession.disconnect();
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

    // InboundAdapterPort implementation
    @Override
    public AdapterOperationResult fetch(FetchRequest request) {
        try {
            return pollForFiles();
        } catch(Exception e) {
            return AdapterOperationResult.failure("Fetch failed: " + e.getMessage());
        }
    }

    @Override
    public CompletableFuture<AdapterOperationResult> fetchAsync(FetchRequest request) {
        return CompletableFuture.supplyAsync(() -> fetch(request));
    }

    public AdapterOperationResult fetchBatch(List<FetchRequest> requests) {
        // For SFTP, batch fetch is same as single fetch
        return fetch(requests.get(0));
    }

    public CompletableFuture<AdapterOperationResult> fetchBatchAsync(List<FetchRequest> requests) {
        return CompletableFuture.supplyAsync(() -> fetchBatch(requests));
    }

    public boolean supportsBatchOperations() {
        return true;
    }

    public int getMaxBatchSize() {
        // Use configured value if available, otherwise use default
        return config.getMaxBatchSize() != null ? config.getMaxBatchSize() : 100;
    }

    protected AdapterOperationResult performFetch(Object criteria) throws Exception {
        // For SFTP Sender(inbound), "send" means polling/retrieving files FROM SFTP server
        return pollForFiles();
    }

    private AdapterOperationResult pollForFiles() throws Exception {
        List<Map<String, Object>> processedFiles = new ArrayList<>();
        Session session = null;
        ChannelSftp channel = null;

        try {
            // Get or create connection
            if("permanently".equals(config.getConnectionMode())) {
                session = sshSession;
                channel = sftpChannel;
                if(session == null || !session.isConnected() || channel == null || !channel.isConnected()) {
                    connectToSftp();
                    session = sshSession;
                    channel = sftpChannel;
                }
            } else {
                session = createSession();
                session.connect();
                channel = (ChannelSftp) session.openChannel("sftp");
                channel.connect();
            }

            // Change to source directory
            channel.cd(config.getSourceDirectory());

            // List files
            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> files = channel.ls(".");
            if(files == null) {
                log.warn("No files returned from SFTP server directory listing");
                return AdapterOperationResult.success(Collections.emptyList(), "No files found in directory");
            }

            // Filter and sort files
            List<ChannelSftp.LsEntry> eligibleFiles = files.stream()
                    .filter(entry -> !entry.getAttrs().isDir())
                    .filter(entry -> !".".equals(entry.getFilename()) && !"..".equals(entry.getFilename()))
                    .filter(entry -> matchesFilePattern(entry.getFilename()))
                    .filter(this::shouldProcessFile)
                    .collect(Collectors.toList());

            sortFiles(eligibleFiles);

            // Process files
            for(ChannelSftp.LsEntry entry : eligibleFiles) {
                try {
                    if(shouldProcessFile(entry)) {
                        Map<String, Object> fileData = processFile(channel, entry);
                        if(fileData != null) {
                            processedFiles.add(fileData);
                            handlePostProcessing(channel, entry);

                            // Mark as processed
                            this.processedFiles.put(entry.getFilename(), String.valueOf(System.currentTimeMillis()));
                        }
                    }
                } catch(Exception e) {
                    log.error("Error processing SFTP file: {}", entry.getFilename(), e);

                    if(!config.isContinueOnError()) {
                        throw new AdapterException("SFTP file processing failed for " + entry.getFilename() + ": " + e.getMessage(), e);
                    }
                }
            }

        } finally {
            if("per - file - transfer".equals(config.getConnectionMode())) {
                if(channel != null && channel.isConnected()) {
                    channel.disconnect();
                }
                if(session != null && session.isConnected()) {
                    session.disconnect();
                }
            }
        }

        log.info("SFTP inbound adapter polled {} files from server", processedFiles.size());

        return AdapterOperationResult.success(processedFiles,
                String.format("Retrieved %d files from SFTP server", processedFiles.size()));
    }
    private Map<String, Object> processFile(ChannelSftp channel, ChannelSftp.LsEntry entry) throws Exception {
        SftpATTRS attrs = entry.getAttrs();

        // Check file age
        if(config.getMinFileAge() > 0) {
            long fileAge = System.currentTimeMillis() - (attrs.getMTime() * 1000L);
            if(fileAge < config.getMinFileAge()) {
                log.debug("SFTP file {} is too young, skipping", entry.getFilename());
                return null;
            }
        }

        // Size validation
        long fileSize = attrs.getSize();
        if(fileSize > config.getMaxFileSize()) {
            log.debug("SFTP file {} size {} exceeds maximum {}, skipping",
                    entry.getFilename(), fileSize, config.getMaxFileSize());
            return null;
        }

        // Handle empty files
        if(fileSize == 0) {
            return handleEmptyFile(entry);
        }

        // Download file content
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileName", entry.getFilename());
        fileData.put("fileSize", fileSize);
        fileData.put("lastModified", new Date(attrs.getMTime() * 1000L));
        fileData.put("sftpPath", config.getSourceDirectory() + "/" + entry.getFilename());
        fileData.put("permissions", attrs.getPermissionsString());

        // Download file content
        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            channel.get(entry.getFilename(), baos);

            byte[] content = baos.toByteArray();

            if(config.isLogFileContent()) {
                // Convert to string for logging
                String contentStr = new String(content,
                        config.getFileEncoding() != null ?
                                config.getFileEncoding() : "UTF-8");
                fileData.put("content", contentStr);
            } else {
                fileData.put("content", content);
            }

            // Generate checksum if configured
            if(config.isValidateFileIntegrity()) {
                String checksum = generateChecksum(content);
                fileData.put("checksum", checksum);

                // Check for duplicates
                if(config.isEnableDuplicateHandling()) {
                    if(isDuplicate(entry.getFilename(), checksum)) {
                        log.debug("SFTP file {} is a duplicate, skipping", entry.getFilename());
                        return null;
                    }
                }
            }
        }

        return fileData;
    }

    private void handlePostProcessing(ChannelSftp channel, ChannelSftp.LsEntry entry) throws Exception {
        String processingMode = config.getProcessingMode();
        String fileName = entry.getFilename();

        switch(processingMode.toLowerCase()) {
            case "delete":
                channel.rm(fileName);
                log.debug("Deleted processed SFTP file: {}", fileName);
                break;

            case "archive":
                if(config.getArchiveDirectory() != null) {
                    String archivePath = config.getArchiveDirectory() + "/" + fileName;
                    channel.rename(fileName, archivePath);
                    log.debug("Archived SFTP file to: {}", archivePath);
                }
                break;

            case "move":
                if(config.getProcessedDirectory() != null) {
                    String movePath = config.getProcessedDirectory() + "/" + fileName;
                    channel.rename(fileName, movePath);
                    log.debug("Moved SFTP file to: {}", movePath);
                }
                break;

            default:
                log.debug("No post - processing configured for SFTP file: {}", fileName);
        }
    }

    private boolean shouldProcessFile(ChannelSftp.LsEntry entry) {
        String fileName = entry.getFilename();

        // Check if already processed
        if(processedFiles.containsKey(fileName)) {
            return false;
        }

        // Check exclusion patterns
        if(exclusionPattern != null && exclusionPattern.matcher(fileName).matches()) {
            return false;
        }

        return true;
    }

    private boolean matchesFilePattern(String fileName) {
        // Simple file name match
        if(config.getFileName() != null && !config.getFileName().isEmpty() && !"*".equals(config.getFileName())) {
            if(config.getFileName().contains("*") || config.getFileName().contains("?")) {
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

    private void sortFiles(List<ChannelSftp.LsEntry> files) {
        String sorting = config.getSorting();
        if(sorting == null || "none".equals(sorting)) {
            return;
        }

        switch(sorting.toLowerCase()) {
            case "name":
                files.sort(Comparator.comparing(ChannelSftp.LsEntry::getFilename));
                break;
            case "date":
                files.sort(Comparator.comparing(entry -> entry.getAttrs().getMTime()));
                break;
            case "size":
                files.sort(Comparator.comparing(entry -> entry.getAttrs().getSize()));
                break;
        }
    }

    private Map<String, Object> handleEmptyFile(ChannelSftp.LsEntry entry) throws Exception {
        String handling = config.getEmptyFileHandling();

        switch(handling.toLowerCase()) {
            case "ignore":
                log.debug("Ignoring empty SFTP file: {}", entry.getFilename());
                return null;
            case "error":
                throw new AdapterException("Empty file not allowed: " + entry.getFilename());
            case "process":
            default:
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("fileName", entry.getFilename());
                fileData.put("fileSize", 0L);
                fileData.put("lastModified", new Date(entry.getAttrs().getMTime() * 1000L));
                fileData.put("sftpPath", config.getSourceDirectory() + "/" + entry.getFilename());
                fileData.put("content", "");
                return fileData;
        }
    }

    private String generateChecksum(byte[] content) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getChecksumAlgorithm());
        digest.update(content);

        StringBuilder result = new StringBuilder();
        for(byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private boolean isDuplicate(String fileName, String checksum) {
        // Simple duplicate detection based on checksum
        return processedFiles.containsValue(checksum);
    }

    private void connectToSftp() throws Exception {
        if(sshSession != null) {
            disconnectFromSftp();
        }

        sshSession = createSession();
        sshSession.connect();

        sftpChannel = (ChannelSftp) sshSession.openChannel("sftp");
        sftpChannel.connect();
    }

    private Session createSession() throws Exception {
        JSch jsch = new JSch();

        // Configure SSH settings
        if(config.isLogSSHDebug()) {
            JSch.setLogger(new com.jcraft.jsch.Logger() {
                @Override
                public boolean isEnabled(int level) {
                    return true;
                }

                @Override
                public void log(int level, String message) {
                    log.debug("SSH: {}", message);
                }
            });
        }

        // Add private key if configured
        if("publickey".equals(config.getAuthenticationType()) ||
            config.getPreferredAuthentications().contains("publickey")) {
            if(config.getPrivateKey() != null) {
                if(config.getPassphrase() != null) {
                    jsch.addIdentity(config.getPrivateKey(), config.getPassphrase());
                } else {
                    jsch.addIdentity(config.getPrivateKey());
                }
            }
        }

        // Configure known hosts
        if(config.getKnownHostsFile() != null) {
            jsch.setKnownHosts(config.getKnownHostsFile());
        }

        // Create session
        int port = Integer.parseInt(config.getPort());
        Session session = jsch.getSession(config.getUserName(), config.getServerAddress(), port);

        // Set password if using password authentication
        if("password".equals(config.getAuthenticationType()) && config.getPassword() != null) {
            session.setPassword(config.getPassword());
        }

        // Configure session properties
        Properties sessionConfig = new Properties();

        // Host key verification
        switch(config.getHostKeyVerification().toLowerCase()) {
            case "strict":
                sessionConfig.put("StrictHostKeyChecking", "yes");
                break;
            case "relaxed":
                sessionConfig.put("StrictHostKeyChecking", "ask");
                break;
            case "disabled":
                sessionConfig.put("StrictHostKeyChecking", "no");
                break;
        }

        // Compression
        if(!"none".equals(config.getSshCompression())) {
            sessionConfig.put("compression.s2c", config.getSshCompression());
            sessionConfig.put("compression.c2s", config.getSshCompression());
        }

        // Preferred authentications
        if(config.getPreferredAuthentications() != null) {
            sessionConfig.put("PreferredAuthentications", config.getPreferredAuthentications());
        }

        // Cipher suites
        if(config.getCipherSuites() != null) {
            sessionConfig.put("cipher.s2c", config.getCipherSuites());
            sessionConfig.put("cipher.c2s", config.getCipherSuites());
        }

        // MAC algorithms
        if(config.getMacAlgorithms() != null) {
            sessionConfig.put("mac.s2c", config.getMacAlgorithms());
            sessionConfig.put("mac.c2s", config.getMacAlgorithms());
        }

        // Key exchange algorithms
        if(config.getKexAlgorithms() != null) {
            sessionConfig.put("kex", config.getKexAlgorithms());
        }

        session.setConfig(sessionConfig);

        // Set timeout
        int timeout = Integer.parseInt(config.getTimeout());
        session.setTimeout(timeout);

        return session;
    }

    private void disconnectFromSftp() {
        if(sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
            sftpChannel = null;
        }

        if(sshSession != null && sshSession.isConnected()) {
            sshSession.disconnect();
            sshSession = null;
        }
    }

    private void initializePatterns() {
        // Initialize file patterns if configured
        if(config.getFileName() != null && !config.getFileName().isEmpty()) {
            String pattern = config.getFileName()
                    .replace(".", "\\.")
                    .replace("*", ".*")
                    .replace("?", ".");
            filePattern = Pattern.compile(pattern);
        }

        // Initialize exclusion patterns if configured
        // TODO: Add getExclusionPattern() to SftpInboundAdapterConfig if needed
        // if(config.getExclusionPattern() != null && !config.getExclusionPattern().isEmpty()) {
        //     exclusionPattern = Pattern.compile(config.getExclusionPattern());
        // }
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getServerAddress() == null || config.getServerAddress().trim().isEmpty()) {
            throw new AdapterException("SFTP server address is required", null);
        }

        // Validate authentication configuration
        if("password".equals(config.getAuthenticationType()) && config.getPassword() == null) {
            throw new AdapterException("Password is required for password authentication", null);
        }

        if(config.getSourceDirectory() == null || config.getSourceDirectory().trim().isEmpty()) {
            throw new AdapterException("SFTP source directory is required", null);
        }
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("SFTP Sender(Inbound): %s:%s, User: %s, Dir: %s, Auth: %s, Polling: %sms(%s), Processing: %s",
                config.getServerAddress(),
                config.getPort(),
                config.getUserName(),
                config.getSourceDirectory(),
                config.getAuthenticationType(),
                config.getPollingInterval(),
                isPolling() ? "active" : "inactive",
                config.getProcessingMode());
    }
    private AdapterOperationResult executeTest(java.util.concurrent.Callable<AdapterOperationResult> test) {
        try {
            return test.call();
        } catch(Exception e) {
            return AdapterOperationResult.failure("Test execution failed: " + e.getMessage());
        }
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.SFTP)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("SFTP Inbound adapter - receives files from SFTP servers")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
    }


    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.SFTP;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.INBOUND;
    }

    @Override
    public void startListening(InboundAdapterPort.DataReceivedCallback callback) {
        // Not implemented for this adapter type
        log.debug("Push-based listening not supported by this adapter type");
    }

    @Override
    public void stopListening() {
        // Not implemented for this adapter type
    }

    @Override
    public boolean isListening() {
        return false;
    }

    public void startPolling(long intervalMillis) {
        if(polling.get()) {
            log.warn("SFTP polling already active");
            return;
        }

        log.info("Starting SFTP polling with interval: {} ms", intervalMillis);
        polling.set(true);

        // Create scheduled executor for polling
        pollingExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sftp - polling-" + config.getServerAddress());
            t.setDaemon(true);
            return t;
        });

        // Schedule polling task
        pollingExecutor.scheduleWithFixedDelay(() -> {
            if(!polling.get()) {
                return;
            }

            try {
                log.debug("Executing SFTP polling cycle");
                AdapterOperationResult result = pollForFiles();

                // If we have a callback and found files, notify
                if(dataCallback != null && result.isSuccess() && result.getData() != null) {
                    List<Map<String, Object>> files = (List<Map<String, Object>>) result.getData();
                    if(!files.isEmpty()) {
                        log.info("SFTP polling found {} files", files.size());
                        dataCallback.onDataReceived(files, result);
                    }
                }
            } catch(Exception e) {
                log.error("Error during SFTP polling", e);
                if(dataCallback != null) {
                    AdapterOperationResult errorResult = AdapterOperationResult.failure(
                        "Polling error: " + e.getMessage()
                   );
                    dataCallback.onDataReceived(null, errorResult);
                }
            }
        }, 0, intervalMillis, TimeUnit.MILLISECONDS);

        log.info("SFTP polling started successfully");
    }

    public void stopPolling() {
        if(polling.compareAndSet(true, false)) {
            log.info("Stopping SFTP polling");

            if(pollingExecutor != null) {
                pollingExecutor.shutdown();
                try {
                    if(!pollingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                        pollingExecutor.shutdownNow();
                    }
                } catch(InterruptedException e) {
                    log.warn("Interrupted while waiting for polling executor to shutdown");
                    pollingExecutor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                pollingExecutor = null;
            }

            log.info("SFTP polling stopped");
        }
    }

    public void registerDataCallback(InboundAdapterPort.DataReceivedCallback callback) {
        this.dataCallback = callback;
        log.debug("Data callback registered for SFTP adapter");
    }

    public boolean isPolling() {
        return polling.get();
    }
}
