package com.integrixs.adapters.core;

import com.integrixs.adapters.domain.model.AdapterConfiguration;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.integrixs.shared.exceptions.AdapterException;

/**
 * Abstract base implementation for inbound adapters.
 */
public abstract class AbstractInboundAdapter extends AbstractAdapter implements InboundAdapter {

    private ExecutorService asyncExecutor;

    protected AbstractInboundAdapter(AdapterConfiguration.AdapterTypeEnum adapterType) {
        super(adapterType);
    }

    @Override
    protected void doInitialize() throws Exception {
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, getAdapterType() + " - sender - async");
            t.setDaemon(true);
            return t;
        });
        doSenderInitialize();
    }

    @Override
    protected void doDestroy() throws Exception {
        if(asyncExecutor != null && !asyncExecutor.isShutdown()) {
            asyncExecutor.shutdown();
        }
        doSenderDestroy();
    }

    @Override
    public AdapterResult send(Object payload) throws AdapterException {
        return send(payload, new HashMap<>());
    }

    @Override
    public void sendAsync(Object payload, AdapterCallback callback) throws AdapterException {
        validateReady();

        if(payload == null) {
            throw new AdapterException("Payload cannot be null");
        }

        if(callback == null) {
            throw new AdapterException("Callback cannot be null");
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                return send(payload);
            } catch(AdapterException e) {
                return AdapterResult.failure("Async send failed: " + e.getMessage(), e);
            }
        }, asyncExecutor).handle((result, throwable) -> {
            if(throwable != null) {
                AdapterResult failure = AdapterResult.failure("Unexpected async error", throwable);
                callback.onFailure(failure);
                return failure;
            } else if(result != null) {
                if(result.isSuccess()) {
                    callback.onSuccess(result);
                } else {
                    callback.onFailure(result);
                }
                return result;
            }
            return null;
        });
    }

    @Override
    public AdapterResult sendBatch(Collection<Object> payloads) throws AdapterException {
        validateReady();

        if(payloads == null || payloads.isEmpty()) {
            throw new AdapterException("Payloads collection cannot be null or empty");
        }

        return executeTimedOperation("sendBatch", () -> doSendBatch(payloads));
    }

    /**
     * Default batch implementation that sends items individually.
     * Subclasses can override for more efficient batch processing.
     */
    protected AdapterResult doSendBatch(Collection<Object> payloads) throws Exception {
        int successCount = 0;
        int failureCount = 0;
        StringBuilder errors = new StringBuilder();

        for(Object payload : payloads) {
            try {
                AdapterResult result = doSend(payload, new HashMap<>());
                if(result.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                    if(errors.length() > 0) errors.append("; ");
                    errors.append("Item failed: ").append(result.getMessage());
                }
            } catch(Exception e) {
                failureCount++;
                if(errors.length() > 0) errors.append("; ");
                errors.append("Item error: ").append(e.getMessage());
            }
        }

        AdapterResult result;
        if(failureCount == 0) {
            result = AdapterResult.success(null, String.format("Batch completed: %d items sent successfully", successCount));
        } else if(successCount == 0) {
            result = AdapterResult.failure(String.format("Batch failed: all %d items failed. Errors: %s", failureCount, errors));
        } else {
            result = new AdapterResult();
            result.setStatus(AdapterResult.Status.PARTIAL_SUCCESS);
            result.setMessage(String.format("Batch partially completed: %d successful, %d failed. Errors: %s",
                    successCount, failureCount, errors));
        }

        result.addMetadata("successCount", successCount);
        result.addMetadata("failureCount", failureCount);
        result.addMetadata("totalCount", payloads.size());

        return result;
    }

    // Abstract methods for subclasses to implement

    /**
     * Perform sender - specific initialization.
     */
    protected abstract void doSenderInitialize() throws Exception;

    /**
     * Perform sender - specific cleanup.
     */
    protected abstract void doSenderDestroy() throws Exception;

    /**
     * Send a single payload with headers.
     */
    protected abstract AdapterResult doSend(Object payload, Map<String, Object> headers) throws Exception;
}
