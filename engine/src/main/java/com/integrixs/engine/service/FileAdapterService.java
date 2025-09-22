package com.integrixs.engine.service;

import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.exceptions.AdapterException;
import com.integrixs.adapters.config.FileInboundAdapterConfig;
import com.integrixs.adapters.config.FileOutboundAdapterConfig;

/**
 * FileAdapterService handles file - based adapter operations using the new separated architecture.
 * Follows middleware convention: Inbound = receives FROM files(inbound), Outbound = sends TO files(outbound)
 */
public interface FileAdapterService {

    /**
     * Read files from directory using inbound adapter(inbound operation)
     */
    AdapterResult readFiles(FileInboundAdapterConfig config) throws AdapterException;

    /**
     * Write data to file using outbound adapter(outbound operation)
     */
    AdapterResult writeFile(FileOutboundAdapterConfig config, Object data) throws AdapterException;

    /**
     * Start polling for files with callback
     */
    void startFilePolling(FileInboundAdapterConfig config, FilePollingCallback callback) throws AdapterException;

    /**
     * Stop file polling
     */
    void stopFilePolling(FileInboundAdapterConfig config) throws AdapterException;

    /**
     * Test file adapter configuration
     */
    boolean testFileAdapter(Object config) throws AdapterException;

    /**
     * Callback interface for file polling operations
     */
    interface FilePollingCallback {
        void onFilesFound(AdapterResult result);
        void onError(AdapterException error);
    }
}
