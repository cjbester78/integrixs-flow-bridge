package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.OutboundAdapterPort;
import com.integrixs.adapters.config.FileOutboundAdapterConfig;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletableFuture;

/**
 * File Receiver Adapter implementation for file creation and writing(OUTBOUND).
 * Follows middleware convention: Outbound = sends data TO external systems.
 * Supports file creation, atomic writes, batching, backup, and validation.
 */
public class FileOutboundAdapter extends AbstractAdapter implements OutboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(FileOutboundAdapter.class);


    protected final FileOutboundAdapterConfig config;
    protected Path targetDirectory;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    private final List<Object> batchBuffer = new ArrayList<>();
    private long lastBatchFlush = System.currentTimeMillis();

    public FileOutboundAdapter(FileOutboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing File outbound adapter(outbound) with directory: {}", config.getTargetDirectory());

        try {
            validateConfiguration();
            initializeDirectory();
        } catch(Exception e) {
            log.error("Failed to initialize File outbound adapter", e);
            return AdapterOperationResult.failure("Initialization failed: " + e.getMessage());
        }
        log.info("File outbound adapter initialized successfully");
        return AdapterOperationResult.success("File outbound adapter initialized successfully");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying File outbound adapter");
        // Flush any remaining batch data
        if(config.isEnableBatching() && !batchBuffer.isEmpty()) {
            try {
                flushBatch();
        } catch(Exception e) {
                log.warn("Error flushing batch during shutdown", e);
            }
        }
        batchBuffer.clear();
        return AdapterOperationResult.success("File outbound adapter shutdown successfully");
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
        Map<String, AdapterOperationResult> testResults = new HashMap<>();

        // Test 1: Directory accessibility
        try {
            if(!Files.exists(targetDirectory)) {
                testResults.put("test" + testResults.size(), AdapterOperationResult.failure(
                        "Directory does not exist: " + targetDirectory));
            } else if(!Files.isDirectory(targetDirectory)) {
                testResults.put("test" + testResults.size(), AdapterOperationResult.failure(
                        "Path is not a directory: " + targetDirectory));
            } else if(!Files.isWritable(targetDirectory)) {
                testResults.put("test" + testResults.size(), AdapterOperationResult.failure(
                        "Directory is not writable: " + targetDirectory));
            } else {
                testResults.put("test" + testResults.size(), AdapterOperationResult.success(
                        "Target directory is accessible and writable"));
            }
        } catch(Exception e) {
            testResults.put("test" + testResults.size(), AdapterOperationResult.failure(
                    "Failed to access directory: " + e.getMessage()));
        }

        // Test 2: Test file creation
        try {
            String testFileName = "test_file_" + System.currentTimeMillis() + ".tmp";
            Path testFile = targetDirectory.resolve(testFileName);
            Files.write(testFile, "test content".getBytes());

            if(Files.exists(testFile)) {
                Files.deleteIfExists(testFile);
                testResults.put("test" + testResults.size(), AdapterOperationResult.success(
                        "Successfully created and deleted test file"));
            } else {
                testResults.put("test" + testResults.size(), AdapterOperationResult.failure(
                        "Test file was not created"));
            }
        } catch(Exception e) {
            testResults.put("test" + testResults.size(), AdapterOperationResult.failure(
                    "Failed to create test file: " + e.getMessage()));
        }

        // Test 3: Backup and temp directories validation
        try {
            validateSupportDirectories();
            testResults.put("test" + testResults.size(), AdapterOperationResult.success(
                    "All configured support directories are accessible"));
        } catch(Exception e) {
            testResults.put("test" + testResults.size(), AdapterOperationResult.failure(
                    "Support directory validation failed: " + e.getMessage()));
        }

        boolean allPassed = testResults.values().stream().allMatch(AdapterOperationResult::isSuccess);
        return allPassed ?
            AdapterOperationResult.success(testResults) :
            AdapterOperationResult.failure("Some connection tests failed");
    }

    protected AdapterOperationResult performReceive(Object payload) throws Exception {
        // For File Receiver(outbound), this method writes data TO files
        if(config.isEnableBatching()) {
            return addToBatch(payload);
        } else {
            return writeToFile(payload);
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
                return AdapterOperationResult.success(
                        String.format("Added to batch(%d/%d items)",
                                batchBuffer.size(),
                                config.getBatchSize() != null ? config.getBatchSize() : Integer.MAX_VALUE));
            }
        }
    }

    private AdapterOperationResult flushBatch() throws Exception {
        synchronized(batchBuffer) {
            if(batchBuffer.isEmpty()) {
                return AdapterOperationResult.success("No items in batch to flush");
            }

            List<Object> itemsToWrite = new ArrayList<>(batchBuffer);
            batchBuffer.clear();
            lastBatchFlush = System.currentTimeMillis();

            return writeBatchToFile(itemsToWrite);
        }
    }

    private AdapterOperationResult writeBatchToFile(List<Object> items) throws Exception {
        String fileName = generateBatchFileName();
        Path targetFile = targetDirectory.resolve(fileName);
        return writeItemsToFile(targetFile, items, true);
    }

    private AdapterOperationResult writeToFile(Object payload) throws Exception {
        String fileName = generateFileName(payload);
        Path targetFile = targetDirectory.resolve(fileName);
        return writeItemsToFile(targetFile, Arrays.asList(payload), false);
    }

    private AdapterOperationResult writeItemsToFile(Path targetFile, List<Object> items, boolean isBatch) throws Exception {
        Path writeTarget = targetFile;
        long bytesWritten = 0;

        try {
            // Check if file already exists and handle accordingly
            if(Files.exists(targetFile) && !config.isOverwriteExistingFile()) {
                if("create".equals(config.getFileConstructionMode())) {
                    throw new AdapterException("File already exists: " + targetFile);
                }
            }

            // Use temporary file for atomic write if configured
            if(config.isUseAtomicWrite()) {
                String tempDir = config.getTemporaryDirectory();
                if(tempDir != null) {
                    writeTarget = Paths.get(tempDir).resolve(targetFile.getFileName() + ".tmp");
                } else {
                    writeTarget = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
                }
            }

            // Create backup if file exists and backup is configured
            if(Files.exists(targetFile) && config.getBackupDirectory() != null) {
                createBackup(targetFile);
            }

            // Write the file
            if("append".equals(config.getFileConstructionMode()) && Files.exists(writeTarget)) {
                bytesWritten = appendToFile(writeTarget, items);
            } else {
                bytesWritten = writeNewFile(writeTarget, items);
            }

            // Move from temporary location if atomic write
            if(config.isUseAtomicWrite() && !writeTarget.equals(targetFile)) {
                Files.move(writeTarget, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            // Set file permissions if configured
            setFilePermissions(targetFile);

            // Generate checksum if configured
            if(config.isGenerateChecksum()) {
                generateChecksumFile(targetFile);
            }

            // Validate file creation
            if(config.isValidateFileCreation()) {
                validateFileCreation(targetFile, bytesWritten);
            }

            log.info("File outbound adapter wrote {} bytes to file: {}", bytesWritten, targetFile);

            String message = isBatch ?
                    String.format("Successfully wrote batch of %d items(%d bytes) to file", items.size(), bytesWritten) :
                    String.format("Successfully wrote %d bytes to file", bytesWritten);

            AdapterOperationResult result = AdapterOperationResult.success(message);
            result.addMetadata("fileName", targetFile.getFileName().toString());
            result.addMetadata("filePath", targetFile.toAbsolutePath().toString());
            result.addMetadata("bytesWritten", bytesWritten);
            result.addMetadata("itemCount", items.size());

            return result;
        } catch(Exception e) {
            // Clean up temporary file on error
            if(config.isUseAtomicWrite() && !writeTarget.equals(targetFile)) {
                try {
                    Files.deleteIfExists(writeTarget);
                } catch(Exception cleanupEx) {
                    log.warn("Failed to clean up temporary file: {}", writeTarget, cleanupEx);
                }
            }
            throw e;
        }
    }

    private long writeNewFile(Path file, List<Object> items) throws Exception {
        long bytesWritten = 0;

        try(BufferedWriter writer = createWriter(file)) {
            // Write header if configured
            if(config.isIncludeHeaders() && config.getHeaderTemplate() != null) {
                String header = processTemplate(config.getHeaderTemplate(), null);
                writer.write(header);
                writer.write(getLineEnding());
                bytesWritten += header.getBytes(getCharset()).length + getLineEnding().getBytes(getCharset()).length;
            }

            // Write items
            for(int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                String content = convertToString(item);

                if(content != null && !content.isEmpty() ||
                    !"skip".equals(config.getEmptyMessageHandling())) {

                    writer.write(content);
                    bytesWritten += content.getBytes(getCharset()).length;

                    // Add record separator except for last item
                    if(i < items.size() - 1 || config.getFooterTemplate() != null) {
                        writer.write(config.getRecordSeparator());
                        bytesWritten += config.getRecordSeparator().getBytes(getCharset()).length;
                    }
                }
            }

            // Write footer if configured
            if(config.getFooterTemplate() != null) {
                String footer = processTemplate(config.getFooterTemplate(), null);
                writer.write(footer);
                bytesWritten += footer.getBytes(getCharset()).length;
            }
        }

        return bytesWritten;
    }

    private long appendToFile(Path file, List<Object> items) throws Exception {
        long bytesWritten = 0;

        try(BufferedWriter writer = Files.newBufferedWriter(file, getCharset(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            for(Object item : items) {
                String content = convertToString(item);

                if(content != null && !content.isEmpty() ||
                    !"skip".equals(config.getEmptyMessageHandling())) {

                    writer.write(content);
                    writer.write(config.getRecordSeparator());
                    bytesWritten += content.getBytes(getCharset()).length +
                                   config.getRecordSeparator().getBytes(getCharset()).length;
                }
            }
        }

        return bytesWritten;
    }

    private BufferedWriter createWriter(Path file) throws Exception {
        if(config.isUseBufferedWriter()) {
            return Files.newBufferedWriter(file, getCharset(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } else {
            return new BufferedWriter(Files.newBufferedWriter(file, getCharset(),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    config.getBufferSize());
        }
    }

    private String convertToString(Object item) throws Exception {
        if(item == null) {
            return handleEmptyMessage();
        }

        if(item instanceof String) {
            return(String) item;
        }

        if(item instanceof byte[]) {
            return new String((byte[]) item, getCharset());
        }

        if(item instanceof Map || item instanceof Collection) {
            // Convert to JSON or formatted string based on configuration
            return item.toString(); // Simple implementation
        }

        return item.toString();
    }

    private String handleEmptyMessage() throws Exception {
        String handling = config.getEmptyMessageHandling();

        switch(handling.toLowerCase()) {
            case "skip":
                return null;
            case "error":
                throw new AdapterException("Empty message not allowed");
        }

        // Generate default filename
        return "file_" + System.currentTimeMillis() + ".txt";
    }

    private String generateBatchFileName() {
        String pattern = config.getBatchFileNamingPattern();
        if(pattern != null && !pattern.isEmpty()) {
            return processTemplate(pattern, null);
        }

        // Generate default batch filename
        return String.format("batch_%s_%d.txt",
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()),
                batchCounter.incrementAndGet());
    }


    protected String generateFileName(Object payload) throws Exception {
        // Check if a specific target file name is configured
        if(config.getTargetFileName() != null && !config.getTargetFileName().isEmpty()) {
            return config.getTargetFileName();
        }

        // Use file name pattern if configured
        if(config.getFileNamePattern() != null && !config.getFileNamePattern().isEmpty()) {
            return processTemplate(config.getFileNamePattern(), payload);
        }

        // Check if payload has filename hint
        if(payload instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) payload;
            if(map.containsKey("filename")) {
                return String.valueOf(map.get("filename"));
            }
        }

        // Generate default filename
        return String.format("file_%s.txt",
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now()));
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

    private void createBackup(Path file) throws Exception {
        if(config.getBackupDirectory() != null) {
            Path backupDir = Paths.get(config.getBackupDirectory());
            Files.createDirectories(backupDir);

            String backupFileName = file.getFileName().toString() + config.getBackupFileExtension();
            Path backupFile = backupDir.resolve(backupFileName);

            Files.copy(file, backupFile, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Created backup: {}", backupFile);

            // Manage backup file count
            cleanupOldBackups(backupDir, file.getFileName().toString());
        }
    }

    private void cleanupOldBackups(Path backupDir, String baseFileName) throws Exception {
        String backupPattern = baseFileName + config.getBackupFileExtension();

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(backupDir,
                path -> path.getFileName().toString().startsWith(backupPattern))) {

            List<Path> backupFiles = new ArrayList<>();
            stream.forEach(backupFiles::add);

            if(backupFiles.size() > config.getMaxBackupFiles()) {
                // Sort by modification time and delete oldest
                backupFiles.sort(Comparator.comparing(path -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch(IOException e) {
                        return null;
                    }
                }));

                int filesToDelete = backupFiles.size() - config.getMaxBackupFiles();
                for(int i = 0; i < filesToDelete; i++) {
                    Files.deleteIfExists(backupFiles.get(i));
                    log.debug("Deleted old backup: {}", backupFiles.get(i));
                }
            }
        }
    }

    private void setFilePermissions(Path file) throws Exception {
        if(config.getFilePermissions() != null && !System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString(config.getFilePermissions());
                Files.setPosixFilePermissions(file, permissions);
            } catch(Exception e) {
                log.warn("Failed to set file permissions: {}", config.getFilePermissions(), e);
            }
        }
    }

    private void generateChecksumFile(Path file) throws Exception {
        String checksum = calculateChecksum(file);
        Path checksumFile = file.resolveSibling(file.getFileName() + config.getChecksumFileExtension());

        Files.write(checksumFile, (checksum + " " + file.getFileName().toString()).getBytes());
        log.debug("Generated checksum file: {}", checksumFile);
    }

    private String calculateChecksum(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(config.getChecksumAlgorithm());

        try(InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        StringBuilder result = new StringBuilder();
        for(byte b : digest.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private void validateFileCreation(Path file, long expectedBytes) throws Exception {
        if(!Files.exists(file)) {
            throw new AdapterException(
                    "File was not created: " + file);
        }

        if(!Files.isRegularFile(file)) {
            throw new AdapterException(
                    "Created path is not a regular file: " + file);
        }

        if(config.getMaxFileSize() != Long.MAX_VALUE && Files.size(file) > config.getMaxFileSize()) {
            throw new AdapterException("File size exceeds maximum allowed size");
        }
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getTargetDirectory() == null || config.getTargetDirectory().trim().isEmpty()) {
            throw new AdapterException("Target directory is required", null);
        }

        if(config.isEnableBatching()) {
            if(config.getBatchStrategy() == null) {
                throw new AdapterException("Batch strategy is required when batching is enabled", null);
            }
            if(config.getBatchTimeoutMs() <= 0L) {
                config.setBatchTimeoutMs(60000L); // Default to 1 minute
            }
        }
    }

    private void initializeDirectory() throws Exception {
        targetDirectory = Paths.get(config.getTargetDirectory());

        if(config.isCreateFileDirectory()) {
            Files.createDirectories(targetDirectory);
        }

        if(!Files.exists(targetDirectory)) {
            throw new AdapterException(
                    "Target directory does not exist: " + targetDirectory);
        }

        if(!Files.isDirectory(targetDirectory)) {
            throw new AdapterException(
                    "Target path is not a directory: " + targetDirectory);
        }

        if(!Files.isWritable(targetDirectory)) {
            throw new AdapterException(
                    "Target directory is not writable: " + targetDirectory);
        }
    }

    private void validateSupportDirectories() throws Exception {
        String[] dirs = {
            config.getBackupDirectory(),
            config.getTemporaryDirectory(),
            config.getErrorDirectory()
        };

        for(String dir : dirs) {
            if(dir != null && !dir.trim().isEmpty()) {
                Path path = Paths.get(dir);
                Files.createDirectories(path); // Create if doesn't exist

                if(!Files.isDirectory(path)) {
                    throw new AdapterException(
                            "Support path is not a directory: " + path);
                }

                if(!Files.isWritable(path)) {
                    throw new AdapterException(
                            "Support directory is not writable: " + path);
                }
            }
        }
    }


    private java.nio.charset.Charset getCharset() {
        if(config.getFileEncoding() != null && !config.getFileEncoding().isEmpty()) {
            try {
                return java.nio.charset.Charset.forName(config.getFileEncoding());
            } catch(Exception e) {
                log.warn("Invalid charset: {}, using UTF-8", config.getFileEncoding());
            }
        }
        return java.nio.charset.StandardCharsets.UTF_8;
    }

    private String getLineEnding() {
        String lineEnding = config.getLineEnding();
        if(lineEnding != null) {
            return lineEnding;
        }
        // Default to system line separator
        return System.lineSeparator();
    }

    protected long getPollingIntervalMs() {
        // File receivers typically don't poll, they write files
        return 0;
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("File Receiver(Outbound): %s, Pattern: %s, Construction: %s, Batching: %s",
                config.getTargetDirectory(),
                config.getTargetFileName() != null ? config.getTargetFileName() :
                    (config.getFileNamePattern() != null ? config.getFileNamePattern() : "Generated"),
                config.getFileConstructionMode(),
                config.isEnableBatching() ? "Enabled" : "Disabled");
    }

    // OutboundAdapterPort implementation
    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            return performReceive(request.getPayload());
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
        return true; // File receiver supports batch operations
    }

    @Override
    public int getMaxBatchSize() {
        return config.getBatchSize() != null ? config.getBatchSize() : 100;
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.FILE)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.OUTBOUND)
                .description("File Receiver Adapter - writes files to local/network file system")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
        }

    protected AdapterOperationResult performSend(Object payload, Map<String, Object> headers) throws Exception {
        // Implementation depends on adapter type
        return performSend(payload, headers);
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.FILE;
    }

    @Override
    protected AdapterConfiguration.AdapterModeEnum getAdapterMode() {
        return AdapterConfiguration.AdapterModeEnum.OUTBOUND;
    }

}
