package com.integrixs.backend.resilience;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for classifying errors and exceptions to determine appropriate handling strategies.
 * Provides intelligent error categorization for better resilience decisions.
 */
@Service("backendErrorClassificationService")
public class ErrorClassificationService {

    // Cache for classification results

    private static final Logger log = LoggerFactory.getLogger(ErrorClassificationService.class);

    private final Map<String, ErrorClassification> classificationCache = new ConcurrentHashMap<>();

    // Patterns for error message analysis
    private static final Pattern TIMEOUT_PATTERN = Pattern.compile("(?i)(timeout|timed out|time out)", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONNECTION_PATTERN = Pattern.compile("(?i)(connection|connect|refused|reset|closed)", Pattern.CASE_INSENSITIVE);
    private static final Pattern AUTHENTICATION_PATTERN = Pattern.compile("(?i)(auth|authentication|unauthorized|forbidden|401|403)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RATE_LIMIT_PATTERN = Pattern.compile("(?i)(rate limit|too many requests|429)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RESOURCE_PATTERN = Pattern.compile("(?i)(out of memory|disk full|no space|resource)", Pattern.CASE_INSENSITIVE);

    /**
     * Classify an exception to determine its category and handling strategy.
     */
    public ErrorClassification classify(Exception exception) {
        String cacheKey = exception.getClass().getName() + ":" +
                         (exception.getMessage() != null ? exception.getMessage().hashCode() : 0);

        return classificationCache.computeIfAbsent(cacheKey, k -> performClassification(exception));
    }

    private ErrorClassification performClassification(Exception exception) {
        ErrorClassification classification = new ErrorClassification();
        classification.setExceptionClass(exception.getClass().getName());
        classification.setMessage(exception.getMessage());

        // Determine error category
        ErrorCategory category = determineCategory(exception);
        classification.setCategory(category);

        // Determine severity
        ErrorSeverity severity = determineSeverity(exception, category);
        classification.setSeverity(severity);

        // Determine if retryable
        boolean retryable = isRetryable(exception, category);
        classification.setRetryable(retryable);

        // Determine if circuit breaker should open
        boolean circuitBreakerCandidate = isCircuitBreakerCandidate(category, severity);
        classification.setCircuitBreakerCandidate(circuitBreakerCandidate);

        // Get recovery suggestions
        List<String> suggestions = getRecoverySuggestions(exception, category);
        classification.setRecoverySuggestions(suggestions);

        // Estimate recovery time
        long estimatedRecoveryTime = estimateRecoveryTime(category, severity);
        classification.setEstimatedRecoveryTimeMs(estimatedRecoveryTime);

        log.debug("Classified error: {} as {} with severity {}",
                 exception.getClass().getSimpleName(), category, severity);

        return classification;
    }

    private ErrorCategory determineCategory(Exception exception) {
        // Check by exception type first
        if(exception instanceof SocketTimeoutException ||
            exception instanceof java.util.concurrent.TimeoutException) {
            return ErrorCategory.TIMEOUT;
        }

        if(exception instanceof ConnectException ||
            exception instanceof UnknownHostException ||
            exception instanceof java.net.SocketException) {
            return ErrorCategory.CONNECTION;
        }

        if(exception instanceof SecurityException) {
            return ErrorCategory.AUTHENTICATION;
        }

        if(exception instanceof IllegalArgumentException ||
            exception instanceof IllegalStateException) {
            return ErrorCategory.VALIDATION;
        }

        if(exception instanceof java.nio.file.FileSystemException) {
            return ErrorCategory.RESOURCE;
        }

        if(exception instanceof SQLException) {
            return analyzeSQLException((SQLException) exception);
        }

        if(exception instanceof IOException) {
            return analyzeIOException((IOException) exception);
        }

        // Analyze message patterns
        String message = exception.getMessage();
        if(message != null) {
            if(TIMEOUT_PATTERN.matcher(message).find()) return ErrorCategory.TIMEOUT;
            if(CONNECTION_PATTERN.matcher(message).find()) return ErrorCategory.CONNECTION;
            if(AUTHENTICATION_PATTERN.matcher(message).find()) return ErrorCategory.AUTHENTICATION;
            if(RATE_LIMIT_PATTERN.matcher(message).find()) return ErrorCategory.RATE_LIMIT;
            if(RESOURCE_PATTERN.matcher(message).find()) return ErrorCategory.RESOURCE;
        }

        // Check HTTP status codes in runtime exceptions
        if(exception instanceof RuntimeException && message != null) {
            if(message.contains("404")) return ErrorCategory.NOT_FOUND;
            if(message.contains("500") || message.contains("502") || message.contains("503")) {
                return ErrorCategory.SERVICE_UNAVAILABLE;
            }
        }

        return ErrorCategory.UNKNOWN;
    }

    private ErrorCategory analyzeSQLException(SQLException sqlEx) {
        String sqlState = sqlEx.getSQLState();
        if(sqlState != null) {
            if(sqlState.startsWith("08")) return ErrorCategory.CONNECTION;
            if(sqlState.startsWith("22")) return ErrorCategory.VALIDATION;
            if(sqlState.startsWith("23")) return ErrorCategory.CONSTRAINT_VIOLATION;
            if(sqlState.startsWith("40")) return ErrorCategory.TRANSACTION;
            if(sqlState.startsWith("53") || sqlState.startsWith("54")) return ErrorCategory.RESOURCE;
        }
        return ErrorCategory.DATABASE;
    }

    private ErrorCategory analyzeIOException(IOException ioEx) {
        String message = ioEx.getMessage();
        if(message != null) {
            if(message.contains("Permission denied") || message.contains("Access denied")) {
                return ErrorCategory.PERMISSION;
            }
            if(message.contains("No space") || message.contains("Disk full")) {
                return ErrorCategory.RESOURCE;
            }
            if(message.contains("File not found") || message.contains("No such file")) {
                return ErrorCategory.NOT_FOUND;
            }
        }
        return ErrorCategory.IO;
    }

    private ErrorSeverity determineSeverity(Exception exception, ErrorCategory category) {
        // Critical errors
        if(category == ErrorCategory.RESOURCE ||
            category == ErrorCategory.PERMISSION) {
            return ErrorSeverity.CRITICAL;
        }

        // High severity
        if(category == ErrorCategory.AUTHENTICATION ||
            category == ErrorCategory.DATABASE ||
            category == ErrorCategory.TRANSACTION) {
            return ErrorSeverity.HIGH;
        }

        // Medium severity
        if(category == ErrorCategory.CONNECTION ||
            category == ErrorCategory.TIMEOUT ||
            category == ErrorCategory.SERVICE_UNAVAILABLE) {
            return ErrorSeverity.MEDIUM;
        }

        // Low severity
        if(category == ErrorCategory.VALIDATION ||
            category == ErrorCategory.NOT_FOUND ||
            category == ErrorCategory.RATE_LIMIT) {
            return ErrorSeverity.LOW;
        }

        return ErrorSeverity.MEDIUM;
    }

    private boolean isRetryable(Exception exception, ErrorCategory category) {
        // Non - retryable categories
        if(category == ErrorCategory.VALIDATION ||
            category == ErrorCategory.AUTHENTICATION ||
            category == ErrorCategory.PERMISSION ||
            category == ErrorCategory.NOT_FOUND ||
            category == ErrorCategory.CONSTRAINT_VIOLATION) {
            return false;
        }

        // Generally retryable categories
        if(category == ErrorCategory.TIMEOUT ||
            category == ErrorCategory.CONNECTION ||
            category == ErrorCategory.SERVICE_UNAVAILABLE ||
            category == ErrorCategory.RATE_LIMIT ||
            category == ErrorCategory.TRANSACTION) {
            return true;
        }

        // Resource errors - only retry if temporary
        if(category == ErrorCategory.RESOURCE) {
            String message = exception.getMessage();
            return message != null && message.contains("temporary");
        }

        return false;
    }

    private boolean isCircuitBreakerCandidate(ErrorCategory category, ErrorSeverity severity) {
        // Circuit breaker for service - level issues
        return(category == ErrorCategory.SERVICE_UNAVAILABLE ||
                category == ErrorCategory.CONNECTION ||
                category == ErrorCategory.TIMEOUT ||
                category == ErrorCategory.RESOURCE) &&
               (severity == ErrorSeverity.HIGH || severity == ErrorSeverity.CRITICAL);
    }

    private List<String> getRecoverySuggestions(Exception exception, ErrorCategory category) {
        List<String> suggestions = new ArrayList<>();

        switch(category) {
            case TIMEOUT:
                suggestions.add("Increase timeout configuration");
                suggestions.add("Check network latency");
                suggestions.add("Verify target service performance");
                break;

            case CONNECTION:
                suggestions.add("Verify network connectivity");
                suggestions.add("Check firewall rules");
                suggestions.add("Validate connection parameters");
                suggestions.add("Ensure target service is running");
                break;

            case AUTHENTICATION:
                suggestions.add("Verify credentials");
                suggestions.add("Check authentication token expiry");
                suggestions.add("Review access permissions");
                break;

            case RATE_LIMIT:
                suggestions.add("Implement request throttling");
                suggestions.add("Add exponential backoff");
                suggestions.add("Consider batch processing");
                break;

            case RESOURCE:
                suggestions.add("Check system resources(memory, disk)");
                suggestions.add("Implement resource cleanup");
                suggestions.add("Consider scaling resources");
                break;

            case DATABASE:
                suggestions.add("Check database connectivity");
                suggestions.add("Verify connection pool settings");
                suggestions.add("Review query performance");
                break;

            case VALIDATION:
                suggestions.add("Review input data validation");
                suggestions.add("Check data format requirements");
                suggestions.add("Verify API contract");
                break;

            default:
                suggestions.add("Check application logs for details");
                suggestions.add("Review error message for specific guidance");
        }

        return suggestions;
    }

    private long estimateRecoveryTime(ErrorCategory category, ErrorSeverity severity) {
        // Base recovery time by category(in milliseconds)
        long baseTime = switch(category) {
            case TIMEOUT -> 30000; // 30 seconds
            case CONNECTION -> 60000; // 1 minute
            case SERVICE_UNAVAILABLE -> 300000; // 5 minutes
            case RATE_LIMIT -> 60000; // 1 minute
            case RESOURCE -> 600000; // 10 minutes
            case TRANSACTION -> 5000; // 5 seconds
            default -> 10000; // 10 seconds
        };

        // Adjust by severity
        return switch(severity) {
            case CRITICAL -> baseTime * 2;
            case HIGH ->(long) (baseTime * 1.5);
            case MEDIUM -> baseTime;
            case LOW -> baseTime / 2;
        };
    }

    /**
     * Error categories for classification.
     */
    public enum ErrorCategory {
        TIMEOUT,
        CONNECTION,
        AUTHENTICATION,
        RATE_LIMIT,
        VALIDATION,
        NOT_FOUND,
        PERMISSION,
        RESOURCE,
        DATABASE,
        TRANSACTION,
        CONSTRAINT_VIOLATION,
        SERVICE_UNAVAILABLE,
        IO,
        UNKNOWN
    }

    /**
     * Error severity levels.
     */
    public enum ErrorSeverity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    /**
     * Error classification result.
     */
        public static class ErrorClassification {
        private String exceptionClass;
        private String message;
        private ErrorCategory category;
        private ErrorSeverity severity;
        private boolean retryable;
        private boolean circuitBreakerCandidate;
        private List<String> recoverySuggestions;
        private long estimatedRecoveryTimeMs;

        // Getters and Setters
        public String getExceptionClass() {
            return exceptionClass;
        }

        public void setExceptionClass(String exceptionClass) {
            this.exceptionClass = exceptionClass;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ErrorCategory getCategory() {
            return category;
        }

        public void setCategory(ErrorCategory category) {
            this.category = category;
        }

        public ErrorSeverity getSeverity() {
            return severity;
        }

        public void setSeverity(ErrorSeverity severity) {
            this.severity = severity;
        }

        public boolean isRetryable() {
            return retryable;
        }

        public void setRetryable(boolean retryable) {
            this.retryable = retryable;
        }

        public boolean isCircuitBreakerCandidate() {
            return circuitBreakerCandidate;
        }

        public void setCircuitBreakerCandidate(boolean circuitBreakerCandidate) {
            this.circuitBreakerCandidate = circuitBreakerCandidate;
        }

        public List<String> getRecoverySuggestions() {
            return recoverySuggestions;
        }

        public void setRecoverySuggestions(List<String> recoverySuggestions) {
            this.recoverySuggestions = recoverySuggestions;
        }

        public long getEstimatedRecoveryTimeMs() {
            return estimatedRecoveryTimeMs;
        }

        public void setEstimatedRecoveryTimeMs(long estimatedRecoveryTimeMs) {
            this.estimatedRecoveryTimeMs = estimatedRecoveryTimeMs;
        }
    }
}
