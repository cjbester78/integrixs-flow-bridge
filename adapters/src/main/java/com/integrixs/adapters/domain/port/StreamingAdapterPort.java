package com.integrixs.adapters.domain.port;

import java.io.OutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

/**
 * Domain port interface for streaming - capable adapters
 * Supports both OutputStream and WritableByteChannel for efficient data streaming
 */
public interface StreamingAdapterPort extends OutboundAdapterPort {

    /**
     * Get a WritableByteChannel for streaming data to the adapter
     * This is more efficient for large data transfers using NIO
     *
     * @param parameters Additional parameters for the stream
     * @return A WritableByteChannel for writing data
     * @throws UnsupportedOperationException if the adapter doesn't support streaming
     */
    WritableByteChannel getWritableChannel(Map<String, Object> parameters);

    /**
     * Get an OutputStream for streaming data to the adapter
     * This provides a simpler API for standard IO operations
     *
     * @param parameters Additional parameters for the stream
     * @return An OutputStream for writing data
     * @throws UnsupportedOperationException if the adapter doesn't support streaming
     */
    OutputStream getOutputStream(Map<String, Object> parameters);

    /**
     * Check if this adapter supports streaming operations
     *
     * @return true if streaming is supported
     */
    default boolean supportsStreaming() {
        return true;
    }

    /**
     * Get the preferred buffer size for streaming operations
     *
     * @return Buffer size in bytes
     */
    default int getStreamBufferSize() {
        return 8192; // 8KB default
    }

    /**
     * Check if the adapter supports WritableByteChannel
     * Some adapters might only support OutputStream
     *
     * @return true if WritableByteChannel is supported
     */
    default boolean supportsWritableByteChannel() {
        return true;
    }
}
