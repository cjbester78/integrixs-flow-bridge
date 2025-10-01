package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.shared.exceptions.AdapterException;

import com.integrixs.adapters.domain.model.*;
import com.integrixs.adapters.domain.port.InboundAdapterPort;
import com.integrixs.adapters.config.FileInboundAdapterConfig;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * File Sender Adapter implementation for file system monitoring and processing(INBOUND).
 * Follows middleware convention: Inbound = receives data FROM external systems.
 * Supports directory polling, file filtering, duplicate detection, and incremental processing.
 */
public class FileInboundAdapter extends AbstractAdapter implements InboundAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(FileInboundAdapter.class);


    private final FileInboundAdapterConfig config;
    private final Map<String, String> processedFiles = new ConcurrentHashMap<>();
    private Pattern filePattern;
    private Pattern exclusionPattern;
    private Path sourceDirectory;

    public FileInboundAdapter(FileInboundAdapterConfig config) {
        super();
        this.config = config;
    }

    @Override
    protected AdapterOperationResult performInitialization() {
        log.info("Initializing File inbound adapter(inbound) with directory: {}", config.getSourceDirectory());

        try {
            validateConfiguration();
            initializeDirectory();
            initializePatterns();
        } catch(Exception e) {
            log.error("Failed to initialize File inbound adapter", e);
            return AdapterOperationResult.failure("Initialization failed: " + e.getMessage());
        }

        log.info("File inbound adapter initialized successfully");
        return AdapterOperationResult.success("File inbound adapter initialized successfully");
    }

    @Override
    protected AdapterOperationResult performShutdown() {
        log.info("Destroying File inbound adapter");
        processedFiles.clear();
        return AdapterOperationResult.success("File inbound adapter shutdown successfully");
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
        Map<String, Object> testResults = new HashMap<>();

        // Test 1: Directory accessibility
        try {
            if(!Files.exists(sourceDirectory)) {
                testResults.put("test1", AdapterOperationResult.failure(
                        "Source directory does not exist: " + sourceDirectory));
            } else if(!Files.isDirectory(sourceDirectory)) {
                testResults.put("test1", AdapterOperationResult.failure(
                        "Path is not a directory: " + sourceDirectory));
            } else if(!Files.isReadable(sourceDirectory)) {
                testResults.put("test1", AdapterOperationResult.failure(
                        "Directory is not readable: " + sourceDirectory));
            } else {
                testResults.put("test1", AdapterOperationResult.success(
                        "Source directory is accessible and readable"));
            }
        } catch(Exception e) {
            testResults.put("test1", AdapterOperationResult.failure(
                    "Failed to access directory: " + e.getMessage()));
        }

        // Test 2: File pattern validation
        try {
            long fileCount = Files.list(sourceDirectory)
                    .filter(Files::isRegularFile)
                    .filter(this::matchesFilePattern)
                    .count();
            testResults.put("test2", AdapterOperationResult.success(
                    "Found " + fileCount + " files matching configured patterns"));
        } catch(Exception e) {
            testResults.put("test2", AdapterOperationResult.failure(
                    "Failed to scan directory: " + e.getMessage()));
        }

        // Test 3: Processing directories validation
        if(config.getArchiveDirectory() != null || config.getMoveDirectory() != null ||
            config.getBackupDirectory() != null || config.getErrorDirectory() != null) {
            try {
                validateProcessingDirectories();
                testResults.put("test3", AdapterOperationResult.success(
                        "All configured processing directories are accessible"));
            } catch(Exception e) {
                testResults.put("test3", AdapterOperationResult.failure(
                        "Processing directory validation failed: " + e.getMessage()));
            }
        }

        boolean allPassed = testResults.values().stream()
                .allMatch(result -> result instanceof AdapterOperationResult &&
                        ((AdapterOperationResult) result).isSuccess());

        return allPassed ?
            AdapterOperationResult.success(testResults) :
            AdapterOperationResult.failure("Some tests failed");
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
        // For file adapter, batch fetch is same as single fetch
        return fetch(requests.get(0));
    }

    public CompletableFuture<AdapterOperationResult> fetchBatchAsync(List<FetchRequest> requests) {
        return CompletableFuture.supplyAsync(() -> fetchBatch(requests));
    }

    public boolean supportsBatchOperations() {
        return true; // File adapter can process multiple files in batch
    }

    public int getMaxBatchSize() {
        return config.getMaxFilesPerPoll();
    }

    protected long getPollingIntervalMs() {
        return config.getPollingInterval() != null ? config.getPollingInterval() : 60000L;
    }

    private AdapterOperationResult pollForFiles() throws Exception {
        List<Map<String, Object>> processedFilesList = new ArrayList<>();

        try(DirectoryStream<Path> stream = Files.newDirectoryStream(sourceDirectory)) {
            List<Path> availableFiles = new ArrayList<>();
            for(Path path : stream) {
                if(matchesFilePattern(path)) {
                    availableFiles.add(path);
                }
            }

            // Sort files based on configuration
            sortFiles(availableFiles);

            // Apply file limits
            int maxFiles = Math.min(availableFiles.size(), config.getMaxFilesPerPoll());

            for(int i = 0; i < maxFiles; i++) {
                Path file = availableFiles.get(i);

                try {
                    if(shouldProcessFile(file)) {
                        Map<String, Object> fileData = processFile(file);
                        if(fileData != null) {
                            processedFilesList.add(fileData);
                            handlePostProcessing(file);
                        }
                    }
                } catch(Exception e) {
                    log.error("Error processing file: {}", file, e);
                    handleFileError(file, e);

                    if(!config.isContinueOnError()) {
                        throw new AdapterException("File processing failed: " + e.getMessage(), e);
                    }
                }
            }
        }

        log.info("File inbound adapter polled {} files from directory", processedFilesList.size());
        return AdapterOperationResult.success(
                String.format("Retrieved %d files from directory", processedFilesList.size()));
    }

    private Map<String, Object> processFile(Path file) throws Exception {
        if(!Files.exists(file)) {
            return null; // File may have been processed by another instance
        }

        // Check file age
        if(config.getMinFileAge() > 0) {
            long fileAge = System.currentTimeMillis() - Files.getLastModifiedTime(file).toMillis();
            if(fileAge < config.getMinFileAge() * 1000L) {
                log.debug("File {} is too young, skipping", file);
                return null;
            }
        }

        // Acquire file lock if configured
        if(config.isUseFileLocking()) {
            if(!acquireFileLock(file)) {
                log.debug("Could not acquire lock for file {}, skipping", file);
                return null;
            }
        }

        try {
            // Handle empty files
            long fileSize = Files.size(file);
            if(fileSize == 0) {
                return handleEmptyFile(file);
            }

            // Size validation
            if(fileSize < config.getMinFileSize() || fileSize > config.getMaxFileSize()) {
                log.debug("File {} size {} is outside configured range, skipping", file, fileSize);
                return null;
            }

            // Read file content
            Map<String, Object> fileData = new HashMap<>();
            fileData.put("fileName", file.getFileName().toString());
            fileData.put("filePath", file.toAbsolutePath().toString());
            fileData.put("fileSize", fileSize);
            fileData.put("lastModified", Files.getLastModifiedTime(file).toInstant());

            // Read content based on configuration
            if(config.isLogFileContent()) {
                String content = Files.readString(file,
                        config.getFileEncoding() != null ?
                                java.nio.charset.Charset.forName(config.getFileEncoding()) :
                                java.nio.charset.StandardCharsets.UTF_8);
                fileData.put("content", content);
            } else {
                // For large files, just read as byte array or provide stream
                byte[] content = Files.readAllBytes(file);
                fileData.put("content", content);
            }

            // Generate checksum if configured
            if(config.isValidateFileIntegrity()) {
                String checksum = generateChecksum(file);
                fileData.put("checksum", checksum);

                // Check for duplicates
                if(config.isEnableDuplicateHandling()) {
                    if(isDuplicate(file, checksum)) {
                        handleDuplicateFile(file);
                        return null;
                    }
                }
            }

            // Mark as processed
            this.processedFiles.put(file.toString(),
                    config.isValidateFileIntegrity() ? (String) fileData.get("checksum") :
                            String.valueOf(System.currentTimeMillis()));

            return fileData;

        } finally {
            if(config.isUseFileLocking()) {
                releaseFileLock(file);
            }
        }
    }

    private void handlePostProcessing(Path file) throws Exception {
        String processingMode = config.getProcessingMode();

        switch(processingMode.toLowerCase()) {
            case "delete":
                Files.deleteIfExists(file);
                log.debug("Deleted processed file: {}", file);
                break;

            case "archive":
                if(config.getArchiveDirectory() != null) {
                    Path archiveDir = Paths.get(config.getArchiveDirectory());
                    Files.createDirectories(archiveDir);
                    Path targetPath = archiveDir.resolve(file.getFileName());
                    Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.debug("Archived file to: {}", targetPath);
                }
                break;

            case "move":
                if(config.getMoveDirectory() != null) {
                    Path moveDir = Paths.get(config.getMoveDirectory());
                    Files.createDirectories(moveDir);
                    Path targetPath = moveDir.resolve(file.getFileName());
                    Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.debug("Moved file to: {}", targetPath);
                }
                break;

            case "copy":
                if(config.getBackupDirectory() != null) {
                    Path backupDir = Paths.get(config.getBackupDirectory());
                    Files.createDirectories(backupDir);
                    Path targetPath = backupDir.resolve(file.getFileName());
                    Files.copy(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.debug("Copied file to: {}", targetPath);
                }
                break;

            default:
                log.debug("No post - processing configured for file: {}", file);
        }
    }

    private boolean shouldProcessFile(Path file) throws Exception {
        // Basic file checks
        if(!Files.isRegularFile(file)) {
            return false;
        }

        // Check if already processed(incremental processing)
        if(processedFiles.containsKey(file.toString())) {
            return false;
        }

        // Check exclusion patterns
        if(exclusionPattern != null && exclusionPattern.matcher(file.getFileName().toString()).matches()) {
            return false;
        }

        return true;
    }

    private boolean matchesFilePattern(Path file) {
        if(!Files.isRegularFile(file)) {
            return false;
        }

        String fileName = file.getFileName().toString();

        // Simple file name match
        if(config.getFileName() != null && !config.getFileName().isEmpty()) {
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

        // Regex pattern match
        if(filePattern != null) {
            return filePattern.matcher(fileName).matches();
        }

        return true; // If no pattern specified, match all files
    }

    private void sortFiles(List<Path> files) throws Exception {
        String sorting = config.getSorting();
        if(sorting == null || "none".equals(sorting)) {
            return;
        }

        switch(sorting.toLowerCase()) {
            case "name":
                files.sort(Comparator.comparing(path -> path.getFileName().toString()));
                break;

            case "date":
                files.sort(Comparator.comparing(path -> {
                    try {
                        return Files.getLastModifiedTime(path);
                    } catch(IOException e) {
                        return null;
                    }
                }));
                break;

            case "size":
                files.sort(Comparator.comparing(path -> {
                    try {
                        return Files.size(path);
                    } catch(IOException e) {
                        return 0L;
                    }
                }));
                break;
        }
    }

    private Map<String, Object> handleEmptyFile(Path file) throws Exception {
        String handling = config.getEmptyFileHandling();

        switch(handling.toLowerCase()) {
            case "ignore":
                log.debug("Ignoring empty file: {}", file);
                return null;

            case "error":
                throw new AdapterException("Empty file not allowed: " + file);

            case "process":
            default:
                Map<String, Object> fileData = new HashMap<>();
                fileData.put("fileName", file.getFileName().toString());
                fileData.put("filePath", file.toAbsolutePath().toString());
                fileData.put("fileSize", 0L);
                fileData.put("lastModified", Files.getLastModifiedTime(file).toInstant());
                fileData.put("content", "");
                return fileData;
        }
    }
    private String generateChecksum(Path file) throws Exception {
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

    private boolean isDuplicate(Path file, String checksum) {
        String strategy = config.getDuplicateDetectionStrategy();

        switch(strategy.toLowerCase()) {
            case "filename":
                return processedFiles.containsKey(file.getFileName().toString());

            case "checksum":
                return processedFiles.containsValue(checksum);

            case "content":
            default:
                return processedFiles.containsValue(checksum);
        }
    }

    private void handleDuplicateFile(Path file) throws Exception {
        if(config.getDuplicateDirectory() != null) {
            Path duplicateDir = Paths.get(config.getDuplicateDirectory());
            Files.createDirectories(duplicateDir);
            Path targetPath = duplicateDir.resolve(file.getFileName());
            Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Moved duplicate file to: {}", targetPath);
        }
    }

    private void handleFileError(Path file, Exception error) throws Exception {
        if(config.getErrorDirectory() != null) {
            try {
                Path errorDir = Paths.get(config.getErrorDirectory());
                Files.createDirectories(errorDir);
                Path targetPath = errorDir.resolve(file.getFileName());
                Files.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                log.debug("Moved error file to: {}", targetPath);
            } catch(Exception e) {
                log.warn("Failed to move error file: {}", file, e);
            }
        }
    }

    private boolean acquireFileLock(Path file) {
        try {
            // Simple file locking by creating .lock file
            Path lockFile = file.resolveSibling(file.getFileName() + config.getLockFileExtension());

            if(Files.exists(lockFile)) {
                // Check if lock is stale
                long lockAge = System.currentTimeMillis() - Files.getLastModifiedTime(lockFile).toMillis();
                if(lockAge < config.getFileLockTimeout()) {
                    return false; // Active lock
                }
                // Remove stale lock
                Files.deleteIfExists(lockFile);
            }

            // Create lock file
            Files.createFile(lockFile);
            return true;
        } catch(Exception e) {
            log.warn("Failed to acquire file lock for: {}", file, e);
            return false;
        }
    }

    private void releaseFileLock(Path file) {
        try {
            Path lockFile = file.resolveSibling(file.getFileName() + config.getLockFileExtension());
            Files.deleteIfExists(lockFile);
        } catch(Exception e) {
            log.warn("Failed to release file lock for: {}", file, e);
        }
    }

    private void validateConfiguration() throws AdapterException {
        if(config.getSourceDirectory() == null || config.getSourceDirectory().trim().isEmpty()) {
            throw new AdapterException("Source directory is required", null);
        }
    }

    private void initializeDirectory() throws Exception {
        sourceDirectory = Paths.get(config.getSourceDirectory());

        if(!Files.exists(sourceDirectory)) {
            throw new AdapterException("Source directory does not exist: " + sourceDirectory);
        }

        if(!Files.isDirectory(sourceDirectory)) {
            throw new AdapterException("Source path is not a directory: " + sourceDirectory);
        }

        if(!Files.isReadable(sourceDirectory)) {
            throw new AdapterException("Source directory is not readable: " + sourceDirectory);
        }
    }

    private void initializePatterns() throws Exception {
        // Initialize file pattern
        if(config.getFilePattern() != null && !config.getFilePattern().trim().isEmpty()) {
            try {
                filePattern = Pattern.compile(config.getFilePattern());
            } catch(Exception e) {
                throw new AdapterException("Invalid file pattern: " + config.getFilePattern(), e);
            }
        }

        // Initialize exclusion pattern
        if(config.getExclusionMask() != null && !config.getExclusionMask().trim().isEmpty()) {
            try {
                exclusionPattern = Pattern.compile(config.getExclusionMask());
            } catch(Exception e) {
                throw new AdapterException("Invalid exclusion pattern: " + config.getExclusionMask(), e);
            }
        }
    }


    private void validateProcessingDirectories() throws Exception {
        String[] dirs = {
            config.getArchiveDirectory(),
            config.getMoveDirectory(),
            config.getBackupDirectory(),
            config.getErrorDirectory(),
            config.getDuplicateDirectory()
        };

        for(String dir : dirs) {
            if(dir != null && !dir.trim().isEmpty()) {
                Path path = Paths.get(dir);
                Files.createDirectories(path); // Create if doesn't exist

                if(!Files.isDirectory(path)) {
                    throw new AdapterException("Processing path is not a directory: " + path);
                }

                if(!Files.isWritable(path)) {
                    throw new AdapterException("Processing directory is not writable: " + path);
                }
            }
        }
    }

    @Override
    public String getConfigurationSummary() {
        return String.format("File Sender(Inbound): %s, Pattern: %s, Polling: %dms, Processing: %s",
                config.getSourceDirectory(),
                config.getFileName() != null ? config.getFileName() :
                    (config.getFilePattern() != null ? config.getFilePattern() : "All files"),
                config.getPollingInterval() != null ? config.getPollingInterval() : 60000,
                config.getProcessingMode());
    }

    @Override
    public AdapterMetadata getMetadata() {
        return AdapterMetadata.builder()
                .adapterType(AdapterConfiguration.AdapterTypeEnum.FILE)
                .adapterMode(AdapterConfiguration.AdapterModeEnum.INBOUND)
                .description("File Sender Adapter - monitors and reads files from file system")
                .version("1.0.0")
                .supportsBatch(true)
                .supportsAsync(true)
                .build();
    }

    protected AdapterOperationResult performSend(Object payload, Map<String, Object> headers) throws Exception {
        // For File Sender(inbound), "send" means polling/reading files FROM file system
        return pollForFiles();
    }

    @Override
    protected AdapterConfiguration.AdapterTypeEnum getAdapterType() {
        return AdapterConfiguration.AdapterTypeEnum.FILE;
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
}
