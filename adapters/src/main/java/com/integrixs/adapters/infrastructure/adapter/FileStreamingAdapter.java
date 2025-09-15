package com.integrixs.adapters.infrastructure.adapter;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.integrixs.adapters.config.FileOutboundAdapterConfig;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.domain.model.AdapterOperationResult;
import com.integrixs.adapters.domain.model.SendRequest;
import com.integrixs.adapters.domain.port.StreamingAdapterPort;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * File adapter implementation with streaming support
 * Extends the regular FileOutboundAdapter to add streaming capabilities
 */
public class FileStreamingAdapter extends FileOutboundAdapter implements StreamingAdapterPort {
    private static final Logger log = LoggerFactory.getLogger(FileStreamingAdapter.class);


    public FileStreamingAdapter(FileOutboundAdapterConfig config) {
        super(config);
    }

    @Override
    public AdapterOperationResult send(SendRequest request) {
        try {
            // Generate filename
            String filename = generateFilename();
            Path filePath = targetDirectory.resolve(filename);

            // Handle file mode
            OpenOption[] options = getOpenOptions();

            // Write data
            Object payload = request.getPayload();
            if(payload instanceof byte[]) {
                Files.write(filePath, (byte[]) payload, options);
            } else if(payload instanceof String) {
                Files.writeString(filePath, (String) payload, options);
            } else if(payload instanceof InputStream) {
                try(InputStream is = (InputStream) payload) {
                    Files.copy(is, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                Files.writeString(filePath, payload.toString(), options);
            }

            log.info("File written successfully: {}", filePath);
            return AdapterOperationResult.success("File written: " + filename);

        } catch(Exception e) {
            log.error("Failed to write file", e);
            return AdapterOperationResult.failure("Failed to write file: " + e.getMessage());
        }
    }

    @Override
    public AdapterOperationResult sendBatch(List<SendRequest> requests) {
        int successCount = 0;
        int failureCount = 0;

        for(SendRequest request : requests) {
            AdapterOperationResult result = send(request);
            if(result.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        return AdapterOperationResult.success(
            String.format("Batch completed: %d success, %d failures", successCount, failureCount)
       );
    }

    @Override
    public WritableByteChannel getWritableChannel(Map<String, Object> parameters) {
        try {
            // Generate filename
            String filename = generateFilename();
            Path filePath = targetDirectory.resolve(filename);

            // Get open options
            OpenOption[] options = getOpenOptions();

            log.info("Opening WritableByteChannel for file: {}", filePath);

            // Open file channel
            FileChannel channel = FileChannel.open(filePath, options);

            // Wrap in a channel that logs progress
            return new LoggingWritableByteChannel(channel, filePath.toString());

        } catch(Exception e) {
            log.error("Failed to create WritableByteChannel", e);
            throw new RuntimeException("Failed to create WritableByteChannel: " + e.getMessage(), e);
        }
    }

    @Override
    public OutputStream getOutputStream(Map<String, Object> parameters) {
        try {
            // Generate filename
            String filename = generateFilename();
            Path filePath = targetDirectory.resolve(filename);

            // Get open options
            OpenOption[] options = getOpenOptions();

            log.info("Opening OutputStream for file: {}", filePath);

            // Create output stream
            OutputStream fos = Files.newOutputStream(filePath, options);

            // Wrap in a buffered stream for better performance
            return new BufferedOutputStream(fos, getStreamBufferSize());

        } catch(Exception e) {
            log.error("Failed to create OutputStream", e);
            throw new RuntimeException("Failed to create OutputStream: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsStreaming() {
        return true;
    }

    @Override
    public int getStreamBufferSize() {
        return 16384; // 16KB buffer
    }

    @Override
    public boolean supportsWritableByteChannel() {
        return true;
    }

    private String generateFilename() throws Exception {
        // Use parent class method to generate filename with pattern support
        return super.generateFileName(null);
    }


    private OpenOption[] getOpenOptions() {
        String mode = config.getFileConstructionMode() != null ? config.getFileConstructionMode() : "overwrite";
        return switch(mode.toLowerCase()) {
            case "append" -> new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.APPEND};
            case "create_new" -> new OpenOption[] {StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE};
            default -> new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.WRITE,
                                        StandardOpenOption.TRUNCATE_EXISTING};
        };
    }


    /**
     * WritableByteChannel wrapper that logs progress
     */
    private static class LoggingWritableByteChannel implements WritableByteChannel {
        private final FileChannel channel;
        private final String filename;
        private long bytesWritten = 0;
        private long lastLogTime = System.currentTimeMillis();

        public LoggingWritableByteChannel(FileChannel channel, String filename) {
            this.channel = channel;
            this.filename = filename;
        }

        @Override
        public int write(ByteBuffer src) throws IOException {
            int written = channel.write(src);
            bytesWritten += written;

            // Log progress every 5 seconds
            long now = System.currentTimeMillis();
            if(now - lastLogTime > 5000) {
                log.debug("Streaming progress for {}: {} bytes written", filename, bytesWritten);
                lastLogTime = now;
            }

            return written;
        }

        @Override
        public boolean isOpen() {
            return channel.isOpen();
        }

        @Override
        public void close() throws IOException {
            log.info("Closing stream for {}: Total {} bytes written", filename, bytesWritten);
            channel.close();
        }
    }
}
