package com.integrixs.backend.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * External service call logger for tracking API interactions.
 * Logs requests, responses, and performance metrics for external services.
 */
@Component
public class ExternalServiceLogger {

    private static final Logger log = LoggerFactory.getLogger(ExternalServiceLogger.class);


    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    // Service call statistics
    private final ConcurrentHashMap<String, ServiceStats> serviceStats = new ConcurrentHashMap<>();

    public ExternalServiceLogger(ObjectMapper objectMapper, MeterRegistry meterRegistry) {
        this.objectMapper = objectMapper;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Log external service request
     */
    public String logRequest(String serviceType, String serviceName, String method, String endpoint,
                           Map<String, String> headers, Object body) {
        String requestId = generateRequestId();

        try {
            ExternalServiceCall call = new ExternalServiceCall();
            call.setRequestId(requestId);
            call.setServiceType(serviceType);
            call.setServiceName(serviceName);
            call.setMethod(method);
            call.setEndpoint(endpoint);
            call.setHeaders(sanitizeHeaders(headers));
            call.setRequestBody(sanitizeBody(body));
            call.setCorrelationId(MDC.get("correlationId"));
            call.setFlowId(MDC.get("flowId"));
            call.setUserId(MDC.get("userId"));
            call.setTimestamp(Instant.now());

            // Extract host for statistics
            String host = extractHost(endpoint);
            call.setHost(host);

            log.info("#EXT.SERVICE.REQUEST# {}# {}# {}# {}# {}# {}# {}",
                serviceType,
                serviceName,
                method,
                endpoint,
                requestId,
                call.getCorrelationId(),
                objectMapper.writeValueAsString(call)
           );

            // Record metrics
            meterRegistry.counter("integrix.external.requests",
                "service", serviceName,
                "method", method,
                "host", host
           ).increment();

        } catch(Exception e) {
            log.error("Failed to log external service request", e);
        }

        return requestId;
    }

    /**
     * Log external service response
     */
    public void logResponse(String requestId, String serviceType, String serviceName,
                          int statusCode, Map<String, String> headers, Object body, long durationMs) {
        try {
            ExternalServiceResponse response = new ExternalServiceResponse();
            response.setRequestId(requestId);
            response.setServiceType(serviceType);
            response.setServiceName(serviceName);
            response.setStatusCode(statusCode);
            response.setHeaders(sanitizeHeaders(headers));
            response.setResponseBody(sanitizeBody(body));
            response.setDurationMs(durationMs);
            response.setTimestamp(Instant.now());
            response.setSuccess(statusCode >= 200 && statusCode < 300);

            // Update statistics
            String statsKey = serviceName + ":" + extractHost(null);
            serviceStats.computeIfAbsent(statsKey, k -> new ServiceStats(serviceName))
                .recordCall(response.isSuccess(), durationMs);

            // Log based on response status
            if(statusCode >= 500) {
                log.error("#EXT.SERVICE.RESPONSE.ERROR# {}# {}# {}# {}ms# {}# {}",
                    serviceType,
                    serviceName,
                    statusCode,
                    durationMs,
                    requestId,
                    objectMapper.writeValueAsString(response)
               );
            } else if(statusCode >= 400) {
                log.warn("#EXT.SERVICE.RESPONSE.CLIENT_ERROR# {}# {}# {}# {}ms# {}# {}",
                    serviceType,
                    serviceName,
                    statusCode,
                    durationMs,
                    requestId,
                    objectMapper.writeValueAsString(response)
               );
            } else if(durationMs > 5000) {
                log.warn("#EXT.SERVICE.RESPONSE.SLOW# {}# {}# {}# {}ms# {}",
                    serviceType,
                    serviceName,
                    statusCode,
                    durationMs,
                    requestId
               );
            } else {
                log.info("#EXT.SERVICE.RESPONSE# {}# {}# {}# {}ms# {}",
                    serviceType,
                    serviceName,
                    statusCode,
                    durationMs,
                    requestId
               );
            }

            // Record metrics
            meterRegistry.counter("integrix.external.responses",
                "service", serviceName,
                "status", String.valueOf(statusCode),
                "success", String.valueOf(response.isSuccess())
           ).increment();

            meterRegistry.timer("integrix.external.duration",
                "service", serviceName,
                "status", String.valueOf(statusCode)
           ).record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);

        } catch(Exception e) {
            log.error("Failed to log external service response", e);
        }
    }

    /**
     * Log external service error
     */
    public void logError(String requestId, String serviceType, String serviceName,
                        String endpoint, Exception error, long durationMs) {
        try {
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("requestId", requestId);
            errorDetails.put("serviceType", serviceType);
            errorDetails.put("serviceName", serviceName);
            errorDetails.put("endpoint", endpoint);
            errorDetails.put("errorType", error.getClass().getName());
            errorDetails.put("errorMessage", error.getMessage());
            errorDetails.put("durationMs", durationMs);
            errorDetails.put("correlationId", MDC.get("correlationId"));

            // Update statistics
            String statsKey = serviceName + ":" + extractHost(endpoint);
            serviceStats.computeIfAbsent(statsKey, k -> new ServiceStats(serviceName))
                .recordCall(false, durationMs);

            log.error("#EXT.SERVICE.ERROR# {}# {}# {}# {}ms# {}# {}",
                serviceType,
                serviceName,
                error.getClass().getSimpleName(),
                durationMs,
                requestId,
                objectMapper.writeValueAsString(errorDetails),
                error
           );

            // Record error metric
            meterRegistry.counter("integrix.external.errors",
                "service", serviceName,
                "error", error.getClass().getSimpleName()
           ).increment();

        } catch(Exception e) {
            log.error("Failed to log external service error", e);
        }
    }

    /**
     * Log circuit breaker state change
     */
    public void logCircuitBreakerStateChange(String serviceName, String fromState, String toState, String reason) {
        log.warn("#EXT.SERVICE.CIRCUIT_BREAKER# {}# {}# {}# {}# {}",
            serviceName,
            fromState,
            toState,
            reason,
            MDC.get("correlationId")
       );

        meterRegistry.counter("integrix.circuit_breaker.transitions",
            "service", serviceName,
            "from", fromState,
            "to", toState
       ).increment();
    }

    /**
     * Log retry attempt
     */
    public void logRetryAttempt(String requestId, String serviceName, int attemptNumber, String reason) {
        log.info("#EXT.SERVICE.RETRY# {}# {}# {}# {}# {}",
            serviceName,
            requestId,
            attemptNumber,
            reason,
            MDC.get("correlationId")
       );

        meterRegistry.counter("integrix.external.retries",
            "service", serviceName,
            "attempt", String.valueOf(attemptNumber)
       ).increment();
    }

    /**
     * Get service statistics
     */
    public Map<String, ServiceStats> getServiceStats() {
        return new HashMap<>(serviceStats);
    }

    /**
     * Log current statistics
     */
    public void logStatistics() {
        log.info("#EXT.SERVICE.STATS.START# External Service Statistics Report");

        serviceStats.forEach((key, stats) -> {
            double successRate = stats.getSuccessRate();
            log.info("#EXT.SERVICE.STATS# {}#calls = {}#success = {}%#avgTime = {}ms#maxTime = {}ms#errors = {}",
                stats.getServiceName(),
                stats.getTotalCalls(),
                String.format("%.2f", successRate),
                stats.getAverageResponseTime(),
                stats.getMaxResponseTime(),
                stats.getErrorCount()
           );
        });

        log.info("#EXT.SERVICE.STATS.END# Total services: {}", serviceStats.size());
    }

    /**
     * Generate unique request ID
     */
    private String generateRequestId() {
        return "EXT-" + System.currentTimeMillis() + "-" +
               Thread.currentThread().getId() + "-" +
               (int)(Math.random() * 1000);
    }

    /**
     * Extract host from endpoint URL
     */
    private String extractHost(String endpoint) {
        if(endpoint == null) return "unknown";

        try {
            URI uri = new URI(endpoint);
            return uri.getHost() != null ? uri.getHost() : "unknown";
        } catch(Exception e) {
            return "unknown";
        }
    }

    /**
     * Sanitize headers to remove sensitive information
     */
    private Map<String, String> sanitizeHeaders(Map<String, String> headers) {
        if(headers == null) return null;

        Map<String, String> sanitized = new HashMap<>();
        headers.forEach((key, value) -> {
            String lowerKey = key.toLowerCase();
            if(lowerKey.contains("authorization") ||
                lowerKey.contains("api - key") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("token")) {
                sanitized.put(key, "[REDACTED]");
            } else {
                sanitized.put(key, value);
            }
        });

        return sanitized;
    }

    /**
     * Sanitize request/response body
     */
    private Object sanitizeBody(Object body) {
        if(body == null) return null;

        try {
            // Limit body size for logging
            String bodyStr = objectMapper.writeValueAsString(body);
            if(bodyStr.length() > 1000) {
                return bodyStr.substring(0, 1000) + "... [TRUNCATED]";
            }
            return body;
        } catch(Exception e) {
            return "[SERIALIZATION_ERROR]";
        }
    }

    /**
     * External service call model
     */
    private static class ExternalServiceCall {
        private String requestId;
        private String serviceType;
        private String serviceName;
        private String method;
        private String endpoint;
        private String host;
        private Map<String, String> headers;
        private Object requestBody;
        private String correlationId;
        private String flowId;
        private String userId;
        private Instant timestamp;

        // Getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }

        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }

        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }

        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }

        public Object getRequestBody() { return requestBody; }
        public void setRequestBody(Object requestBody) { this.requestBody = requestBody; }

        public String getCorrelationId() { return correlationId; }
        public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

        public String getFlowId() { return flowId; }
        public void setFlowId(String flowId) { this.flowId = flowId; }

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }

    /**
     * External service response model
     */
    private static class ExternalServiceResponse {
        private String requestId;
        private String serviceType;
        private String serviceName;
        private int statusCode;
        private Map<String, String> headers;
        private Object responseBody;
        private long durationMs;
        private boolean success;
        private Instant timestamp;

        // Getters and setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }

        public String getServiceType() { return serviceType; }
        public void setServiceType(String serviceType) { this.serviceType = serviceType; }

        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }

        public int getStatusCode() { return statusCode; }
        public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }

        public Object getResponseBody() { return responseBody; }
        public void setResponseBody(Object responseBody) { this.responseBody = responseBody; }

        public long getDurationMs() { return durationMs; }
        public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public Instant getTimestamp() { return timestamp; }
        public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    }

    /**
     * Service statistics
     */
    public static class ServiceStats {
        private final String serviceName;
        private final AtomicLong totalCalls = new AtomicLong(0);
        private final AtomicLong successfulCalls = new AtomicLong(0);
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);
        private final AtomicLong maxResponseTime = new AtomicLong(0);
        private volatile Instant lastCallTime;

        public ServiceStats(String serviceName) {
            this.serviceName = serviceName;
        }

        public void recordCall(boolean success, long responseTime) {
            totalCalls.incrementAndGet();
            if(success) {
                successfulCalls.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }

            totalResponseTime.addAndGet(responseTime);

            // Update max response time
            long currentMax;
            do {
                currentMax = maxResponseTime.get();
            } while(responseTime > currentMax && !maxResponseTime.compareAndSet(currentMax, responseTime));

            lastCallTime = Instant.now();
        }

        public String getServiceName() { return serviceName; }
        public long getTotalCalls() { return totalCalls.get(); }
        public long getSuccessfulCalls() { return successfulCalls.get(); }
        public long getErrorCount() { return errorCount.get(); }
        public long getMaxResponseTime() { return maxResponseTime.get(); }
        public Instant getLastCallTime() { return lastCallTime; }

        public double getSuccessRate() {
            long total = totalCalls.get();
            return total > 0 ? (double) successfulCalls.get() / total * 100 : 100.0;
        }

        public double getAverageResponseTime() {
            long total = totalCalls.get();
            return total > 0 ? (double) totalResponseTime.get() / total : 0;
        }
    }
}
