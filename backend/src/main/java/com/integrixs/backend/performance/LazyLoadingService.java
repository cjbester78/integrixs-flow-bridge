package com.integrixs.backend.performance;

import com.integrixs.data.model.Message;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Service for lazy loading of large message payloads.
 * Provides streaming and compression for efficient memory usage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LazyLoadingService {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Value("${payload.storage.path:./data/payloads}")
    private String payloadStoragePath;
    
    @Value("${payload.size.threshold:1048576}") // 1MB default
    private long payloadSizeThreshold;
    
    @Value("${payload.compression.enabled:true}")
    private boolean compressionEnabled;
    
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    
    /**
     * Store large payload externally and return reference.
     */
    @Transactional
    public String storeLargePayload(String messageId, String payload) {
        if (payload == null || payload.length() < payloadSizeThreshold) {
            return payload; // Store inline for small payloads
        }
        
        try {
            // Generate unique filename
            String filename = messageId + "_" + System.currentTimeMillis() + ".payload";
            if (compressionEnabled) {
                filename += ".gz";
            }
            
            Path filePath = Paths.get(payloadStoragePath, filename);
            
            // Ensure directory exists
            Files.createDirectories(filePath.getParent());
            
            // Write payload to file
            if (compressionEnabled) {
                writeCompressedPayload(filePath, payload);
            } else {
                Files.writeString(filePath, payload, StandardCharsets.UTF_8);
            }
            
            log.debug("Stored large payload for message {} to file: {}", messageId, filename);
            
            // Return reference marker
            return "FILE:" + filename;
            
        } catch (IOException e) {
            log.error("Failed to store large payload for message: {}", messageId, e);
            // Fallback to inline storage
            return payload;
        }
    }
    
    /**
     * Load payload lazily, either from database or file system.
     */
    @Cacheable(value = "payloadCache", key = "#messageId")
    public String loadPayload(String messageId, String payloadReference) {
        if (payloadReference == null || !payloadReference.startsWith("FILE:")) {
            return payloadReference; // Return inline payload
        }
        
        try {
            String filename = payloadReference.substring(5); // Remove "FILE:" prefix
            Path filePath = Paths.get(payloadStoragePath, filename);
            
            if (!Files.exists(filePath)) {
                log.warn("Payload file not found for message {}: {}", messageId, filename);
                return null;
            }
            
            // Read payload from file
            if (filename.endsWith(".gz")) {
                return readCompressedPayload(filePath);
            } else {
                return Files.readString(filePath, StandardCharsets.UTF_8);
            }
            
        } catch (IOException e) {
            log.error("Failed to load payload for message: {}", messageId, e);
            return null;
        }
    }
    
    /**
     * Stream large payload without loading into memory.
     */
    public void streamPayload(String messageId, String payloadReference, OutputStream outputStream) throws IOException {
        if (payloadReference == null || !payloadReference.startsWith("FILE:")) {
            // Write inline payload
            outputStream.write(payloadReference.getBytes(StandardCharsets.UTF_8));
            return;
        }
        
        String filename = payloadReference.substring(5);
        Path filePath = Paths.get(payloadStoragePath, filename);
        
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Payload file not found: " + filename);
        }
        
        // Stream from file
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            if (filename.endsWith(".gz")) {
                try (GZIPInputStream gzipInput = new GZIPInputStream(inputStream)) {
                    gzipInput.transferTo(outputStream);
                }
            } else {
                inputStream.transferTo(outputStream);
            }
        }
    }
    
    /**
     * Asynchronously load payload.
     */
    public CompletableFuture<String> loadPayloadAsync(String messageId, String payloadReference) {
        return CompletableFuture.supplyAsync(() -> loadPayload(messageId, payloadReference), executorService);
    }
    
    /**
     * Get payload size without loading content.
     */
    public long getPayloadSize(String payloadReference) {
        if (payloadReference == null || !payloadReference.startsWith("FILE:")) {
            return payloadReference != null ? payloadReference.length() : 0;
        }
        
        try {
            String filename = payloadReference.substring(5);
            Path filePath = Paths.get(payloadStoragePath, filename);
            
            if (Files.exists(filePath)) {
                if (filename.endsWith(".gz")) {
                    // For compressed files, we need to read the original size from metadata
                    // or decompress to get actual size
                    return estimateDecompressedSize(filePath);
                } else {
                    return Files.size(filePath);
                }
            }
        } catch (IOException e) {
            log.error("Failed to get payload size", e);
        }
        
        return 0;
    }
    
    /**
     * Clean up old payload files.
     */
    @Transactional
    public void cleanupOldPayloads(int daysToKeep) {
        try {
            long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24L * 60 * 60 * 1000);
            
            Files.walk(Paths.get(payloadStoragePath))
                .filter(Files::isRegularFile)
                .filter(path -> {
                    try {
                        return Files.getLastModifiedTime(path).toMillis() < cutoffTime;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.debug("Deleted old payload file: {}", path.getFileName());
                    } catch (IOException e) {
                        log.error("Failed to delete payload file: {}", path, e);
                    }
                });
                
        } catch (IOException e) {
            log.error("Failed to cleanup old payloads", e);
        }
    }
    
    /**
     * Write compressed payload to file.
     */
    private void writeCompressedPayload(Path filePath, String payload) throws IOException {
        try (OutputStream fileOutput = Files.newOutputStream(filePath);
             GZIPOutputStream gzipOutput = new GZIPOutputStream(fileOutput)) {
            gzipOutput.write(payload.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    /**
     * Read compressed payload from file.
     */
    private String readCompressedPayload(Path filePath) throws IOException {
        try (InputStream fileInput = Files.newInputStream(filePath);
             GZIPInputStream gzipInput = new GZIPInputStream(fileInput);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            gzipInput.transferTo(outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }
    
    /**
     * Estimate decompressed size of GZIP file.
     */
    private long estimateDecompressedSize(Path filePath) throws IOException {
        // GZIP stores original size in last 4 bytes of file
        try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
            if (raf.length() < 4) {
                return 0;
            }
            raf.seek(raf.length() - 4);
            int b1 = raf.read();
            int b2 = raf.read();
            int b3 = raf.read();
            int b4 = raf.read();
            return ((long)(b4 & 0xFF) << 24) | ((b3 & 0xFF) << 16) | ((b2 & 0xFF) << 8) | (b1 & 0xFF);
        }
    }
    
    /**
     * Check if payload should be stored externally.
     */
    public boolean shouldStoreExternally(String payload) {
        return payload != null && payload.length() > payloadSizeThreshold;
    }
}