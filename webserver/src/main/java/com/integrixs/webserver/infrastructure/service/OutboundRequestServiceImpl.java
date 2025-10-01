package com.integrixs.webserver.infrastructure.service;

import com.integrixs.webserver.domain.model.OutboundRequest;
import com.integrixs.webserver.domain.model.OutboundResponse;
import com.integrixs.webserver.domain.service.HttpClientService;
import com.integrixs.webserver.domain.service.OutboundRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Implementation of outbound request service
 */
@Service
public class OutboundRequestServiceImpl implements OutboundRequestService {

    private static final Logger logger = LoggerFactory.getLogger(OutboundRequestServiceImpl.class);

    private final HttpClientService httpClientService;
    private final ExecutorService executorService;
    private final Map<String, CompletableFuture<OutboundResponse>> activeRequests;

    public OutboundRequestServiceImpl(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
        this.executorService = Executors.newCachedThreadPool();
        this.activeRequests = new ConcurrentHashMap<>();
    }

    @Override
    public OutboundResponse executeRequest(OutboundRequest request) {
        logger.info("Executing outbound request {}: {} {}",
            request.getRequestId(), request.getHttpMethod(), request.getTargetUrl());

        // Validate request first
        if(!validateRequest(request)) {
            return OutboundResponse.failure(request.getRequestId(), 400, "Invalid request");
        }

        // Execute with retry logic if configured
        if(request.getRetryConfig() != null && request.getRetryConfig().getMaxRetries() > 0) {
            return executeWithRetry(request);
        } else {
            return executeSingleRequest(request);
        }
    }

    @Override
    public void executeRequestAsync(OutboundRequest request, ResponseCallback callback) {
        logger.info("Executing async outbound request {}", request.getRequestId());

        CompletableFuture<OutboundResponse> future = CompletableFuture
            .supplyAsync(() -> executeRequest(request), executorService)
            .whenComplete((response, throwable) -> {
                activeRequests.remove(request.getRequestId());

                if(throwable != null) {
                    callback.onError(request.getRequestId(), throwable.getMessage());
                } else {
                    callback.onSuccess(response);
                }
            });

        activeRequests.put(request.getRequestId(), future);
    }

    @Override
    public boolean validateRequest(OutboundRequest request) {
        if(request == null) return false;
        if(request.getTargetUrl() == null || request.getTargetUrl().isEmpty()) return false;
        if(request.getHttpMethod() == null) return false;
        if(request.getRequestType() == null) return false;

        // Validate URL format
        try {
            new java.net.URL(request.getTargetUrl());
        } catch(Exception e) {
            logger.warn("Invalid URL: {}", request.getTargetUrl());
            return false;
        }

        return true;
    }

    @Override
    public Object transformRequestPayload(OutboundRequest request, String targetFormat) {
        // Delegate to transformation service
        // For now, return payload as - is
        return request.getPayload();
    }

    @Override
    public Object transformResponsePayload(OutboundResponse response, String targetFormat) {
        // Delegate to transformation service
        // For now, return response body as - is
        return response.getResponseBody();
    }

    @Override
    public boolean cancelRequest(String requestId) {
        CompletableFuture<OutboundResponse> future = activeRequests.get(requestId);
        if(future != null && !future.isDone()) {
            boolean cancelled = future.cancel(true);
            if(cancelled) {
                activeRequests.remove(requestId);
                logger.info("Cancelled request {}", requestId);
            }
            return cancelled;
        }
        return false;
    }

    @Override
    public String getRequestStatus(String requestId) {
        CompletableFuture<OutboundResponse> future = activeRequests.get(requestId);
        if(future == null) {
            return "COMPLETED";
        } else if(future.isCancelled()) {
            return "CANCELLED";
        } else if(future.isDone()) {
            return "COMPLETED";
        } else {
            return "IN_PROGRESS";
        }
    }

    private OutboundResponse executeSingleRequest(OutboundRequest request) {
        switch(request.getRequestType()) {
            case SOAP_SERVICE:
                return httpClientService.executeSoapCall(request);
            case GRAPHQL:
                return httpClientService.executeGraphQLQuery(request);
            case REST_API:
            case WEBHOOK:
            default:
                return httpClientService.executeRestCall(request);
        }
    }

    private OutboundResponse executeWithRetry(OutboundRequest request) {
        OutboundRequest.RetryConfig retryConfig = request.getRetryConfig();
        int maxRetries = retryConfig.getMaxRetries();
        long delay = retryConfig.getRetryDelayMillis();

        OutboundResponse.RetryInfo.RetryInfoBuilder retryInfo = OutboundResponse.RetryInfo.builder()
                .attemptCount(0)
                .wasRetried(false);

        for(int attempt = 0; attempt <= maxRetries; attempt++) {
            if(attempt > 0) {
                logger.info("Retry attempt {} for request {}", attempt, request.getRequestId());
                retryInfo.attemptCount(attempt).wasRetried(true);

                try {
                    Thread.sleep(delay);
                    if(retryConfig.isExponentialBackoff()) {
                        delay = (long) (delay * retryConfig.getBackoffMultiplier());
                    }
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return OutboundResponse.failure(request.getRequestId(), 500, "Request interrupted");
                }
            }

            OutboundResponse response = executeSingleRequest(request);

            // Check if retry is needed
            if(response.isSuccessful() || !shouldRetry(response)) {
                if(attempt > 0) {
                    response.withRetryInfo(retryInfo.build());
                }
                return response;
            }

            retryInfo.lastRetryReason(response.getErrorMessage());
            retryInfo.lastRetryTime(java.time.LocalDateTime.now());
        }

        // Max retries exhausted
        OutboundResponse finalResponse = OutboundResponse.failure(
            request.getRequestId(),
            500,
            "Max retries exhausted"
       );
        finalResponse.withRetryInfo(retryInfo.build());
        return finalResponse;
    }

    private boolean shouldRetry(OutboundResponse response) {
        // Retry on 5xx errors and timeouts
        if(response.getStatusCode() >= 500 || response.getStatusCode() == -1) {
            return true;
        }

        // Retry on specific error codes
        if("TIMEOUT".equals(response.getErrorCode()) ||
            "CONNECTION_REFUSED".equals(response.getErrorCode())) {
            return true;
        }

        return false;
    }
}
