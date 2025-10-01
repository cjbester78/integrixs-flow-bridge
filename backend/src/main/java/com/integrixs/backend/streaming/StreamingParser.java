package com.integrixs.backend.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Interface for streaming parsers
 */
public interface StreamingParser<T> {

    /**
     * Parse input stream and process elements
     * @param inputStream The input stream to parse
     * @param elementProcessor Consumer to process each element
     * @param progressCallback Optional progress callback
     * @throws IOException if parsing fails
     */
    void parse(InputStream inputStream,
              Consumer<T> elementProcessor,
              ProgressCallback progressCallback) throws IOException;

    /**
     * Get the type of elements this parser produces
     */
    Class<T> getElementType();

    /**
     * Check if this parser can handle the given content type
     */
    boolean canHandle(String contentType);

    /**
     * Progress callback for streaming operations
     */
    @FunctionalInterface
    interface ProgressCallback {
        void onProgress(long bytesRead, long totalBytes);
    }
}
