package com.integrixs.backend.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Configuration for retry policies across different adapter types.
 * Provides customized retry configurations based on adapter characteristics.
 */
@Configuration
public class RetryPolicyConfiguration {

    @Value("${resilience4j.retry.instances.default.maxAttempts:3}")
    private int defaultMaxAttempts;

    @Value("${resilience4j.retry.instances.default.waitDuration:1000}")
    private long defaultWaitDuration;

    @Bean
    public RetryRegistry retryRegistry() {
        Map<String, RetryConfig> configs = new HashMap<>();

        // Default configuration
        configs.put("default", createDefaultRetryConfig());

        // HTTP/REST adapters-quick retries with exponential backoff
        configs.put("http", createHttpRetryConfig());

        // Database adapters-fewer retries, longer waits
        configs.put("database", createDatabaseRetryConfig());

        // Message queue adapters-aggressive retries
        configs.put("messaging", createMessagingRetryConfig());

        // File transfer adapters-patient retries
        configs.put("file", createFileTransferRetryConfig());

        // SAP adapters-conservative retry approach
        configs.put("sap", createSapRetryConfig());

        return RetryRegistry.of(configs);
    }

    private RetryConfig createDefaultRetryConfig() {
        return RetryConfig.custom()
            .maxAttempts(defaultMaxAttempts)
            .waitDuration(Duration.ofMillis(defaultWaitDuration))
            .retryOnException(e -> !(e instanceof IllegalArgumentException))
            .build();
    }

    private RetryConfig createHttpRetryConfig() {
        return RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(io.github.resilience4j.core.IntervalFunction
                .ofExponentialBackoff(500, 2)) // 500ms, 1s, 2s
            .retryExceptions(
                IOException.class,
                TimeoutException.class,
                ConnectException.class,
                SocketTimeoutException.class
           )
            .ignoreExceptions(
                IllegalArgumentException.class,
                IllegalStateException.class
           )
            .retryOnResult(response -> {
                // Retry on 5xx server errors
                if(response instanceof Map) {
                    Integer status = (Integer) ((Map<?, ?>) response).get("status");
                    return status != null && status >= 500 && status < 600;
                }
                return false;
            })
            .build();
    }

    private RetryConfig createDatabaseRetryConfig() {
        return RetryConfig.custom()
            .maxAttempts(2) // Fewer retries for DB
            .intervalFunction(io.github.resilience4j.core.IntervalFunction
                .ofExponentialBackoff(2000, 1.5)) // 2s, 3s
            .retryExceptions(
                SQLException.class,
                org.springframework.dao.TransientDataAccessException.class,
                org.springframework.transaction.TransactionTimedOutException.class
           )
            .ignoreExceptions(
                org.springframework.dao.DataIntegrityViolationException.class,
                org.springframework.dao.DuplicateKeyException.class
           )
            .retryOnException(e -> {
                // Retry on connection/network issues
                if(e instanceof SQLException) {
                    String sqlState = ((SQLException) e).getSQLState();
                    // Connection errors typically start with "08"
                    return sqlState != null && sqlState.startsWith("08");
                }
                return true;
            })
            .build();
    }

    private RetryConfig createMessagingRetryConfig() {
        return RetryConfig.custom()
            .maxAttempts(5) // More retries for messaging
            .intervalFunction(io.github.resilience4j.core.IntervalFunction
                .ofExponentialBackoff(200, 2)) // 200ms, 400ms, 800ms, 1.6s, 3.2s
            .retryExceptions(
                org.springframework.messaging.MessagingException.class,
                java.io.IOException.class,
                java.lang.RuntimeException.class
           )
            .ignoreExceptions(
                java.lang.IllegalArgumentException.class,
                java.lang.IllegalStateException.class
           )
            .build();
    }

    private RetryConfig createFileTransferRetryConfig() {
        return RetryConfig.custom()
            .maxAttempts(4)
            .intervalFunction(io.github.resilience4j.core.IntervalFunction
                .ofExponentialBackoff(5000, 1.5)) // 5s, 7.5s, 11.25s, 16.875s
            .retryExceptions(
                IOException.class,
                ConnectException.class,
                org.apache.commons.net.ftp.FTPConnectionClosedException.class
           )
            .ignoreExceptions(
                java.io.FileNotFoundException.class,
                java.nio.file.AccessDeniedException.class
           )
            .retryOnException(e -> {
                // Don't retry on permission errors
                String message = e.getMessage();
                return message == null ||
                    (!message.contains("Permission denied") &&
                     !message.contains("Access denied"));
            })
            .build();
    }

    private RetryConfig createSapRetryConfig() {
        return RetryConfig.custom()
            .maxAttempts(2) // Conservative for SAP
            .intervalFunction(io.github.resilience4j.core.IntervalFunction
                .ofExponentialBackoff(10000, 1.2)) // 10s, 12s
            .retryOnException(e -> {
                // Check if it's a SAP exception by class name to avoid compile-time dependency
                if(e.getClass().getName().equals("com.sap.conn.jco.JCoException")) {
                    try {
                        // Use reflection to get the error group
                        java.lang.reflect.Method getGroupMethod = e.getClass().getMethod("getGroup");
                        int group = (int) getGroupMethod.invoke(e);
                        // Retry on communication errors(group 1xx)
                        return group >= 100 && group < 200;
                    } catch(Exception ex) {
                        // If reflection fails, don't retry
                        return false;
                    }
                }
                return false;
            })
            .build();
    }

    /**
     * Get retry instance for a specific adapter.
     */
    public Retry getRetry(RetryRegistry registry, String adapterType, String adapterId) {
        String configName = mapAdapterTypeToConfig(adapterType);
        String retryName = adapterType + "-" + adapterId;

        return registry.retry(retryName, configName);
    }

    public String mapAdapterTypeToConfig(String adapterType) {
        switch(adapterType.toUpperCase()) {
            case "HTTP":
            case "REST":
            case "SOAP":
            case "ODATA":
                return "http";

            case "JDBC":
                return "database";

            case "JMS":
            case "KAFKA":
                return "messaging";

            case "FTP":
            case "SFTP":
                return "file";

            case "RFC":
            case "IDOC":
                return "sap";

            default:
                return "default";
        }
    }
}
