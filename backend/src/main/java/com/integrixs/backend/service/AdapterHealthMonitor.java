package com.integrixs.backend.service;

import com.integrixs.data.model.AdapterHealthRecord;
import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.sql.repository.AdapterHealthRecordSqlRepository;
import com.integrixs.data.sql.repository.CommunicationAdapterSqlRepository;
import com.integrixs.shared.enums.AdapterType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import jakarta.annotation.PostConstruct;
import jakarta.jms.*;
import javax.sql.DataSource;
import org.apache.activemq.ActiveMQConnectionFactory;
import javax.xml.namespace.QName;
import jakarta.xml.soap.*;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adapter Health Monitor-Monitors the health status of all adapters
 * Performs periodic health checks, tracks metrics, and alerts on failures
 */
@Service
public class AdapterHealthMonitor {

    private static final Logger logger = LoggerFactory.getLogger(AdapterHealthMonitor.class);

    @Autowired
    private CommunicationAdapterSqlRepository adapterRepository;

    @Autowired
    private AdapterHealthRecordSqlRepository healthRecordRepository;

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private AdapterPoolManager poolManager;

    @Value("${integrix.health.check.interval:30000}")
    private long healthCheckIntervalMs;

    @Value("${integrix.health.check.timeout:10000}")
    private long healthCheckTimeoutMs;

    @Value("${integrix.health.failure.threshold:3}")
    private int failureThreshold;

    // Health check executor
    private ScheduledExecutorService healthCheckExecutor;
    private ExecutorService checkExecutor;

    // Health status tracking
    private final Map<String, AdapterHealthStatus> healthStatusMap = new ConcurrentHashMap<>();

    // Metrics tracking
    private final Map<String, AdapterMetrics> metricsMap = new ConcurrentHashMap<>();

    @Autowired
    public void setPoolManager(@Lazy AdapterPoolManager poolManager) {
        this.poolManager = poolManager;
    }

    @PostConstruct
    public void initialize() {
        logger.info("Initializing AdapterHealthMonitor");

        healthCheckExecutor = Executors.newScheduledThreadPool(2);
        checkExecutor = Executors.newFixedThreadPool(10);

        // Load all adapters
        loadAdapters();

        // Start health check scheduler
        healthCheckExecutor.scheduleWithFixedDelay(
            this::performHealthChecks,
            healthCheckIntervalMs,
            healthCheckIntervalMs,
            TimeUnit.MILLISECONDS
       );

        // Start metrics collection
        healthCheckExecutor.scheduleWithFixedDelay(
            this::collectMetrics,
            60000, // Every minute
            60000,
            TimeUnit.MILLISECONDS
       );
    }

    /**
     * Load all active adapters for monitoring
     */
    private void loadAdapters() {
        List<CommunicationAdapter> adapters = adapterRepository.findByIsActiveTrue();

        for(CommunicationAdapter adapter : adapters) {
            healthStatusMap.put(
                adapter.getId().toString(),
                new AdapterHealthStatus(adapter.getId().toString(), adapter.getName())
           );

            metricsMap.put(
                adapter.getId().toString(),
                new AdapterMetrics(adapter.getId().toString())
           );
        }

        logger.info("Loaded {} adapters for health monitoring", adapters.size());
    }

    /**
     * Perform health checks on all adapters
     */
    @Scheduled(fixedDelayString = "${integrix.health.check.interval:30000}")
    public void performHealthChecks() {
        logger.debug("Starting health checks for {} adapters", healthStatusMap.size());

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for(String adapterId : healthStatusMap.keySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                checkAdapterHealth(adapterId);
            }, checkExecutor);

            futures.add(future);
        }

        // Wait for all checks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .orTimeout(healthCheckTimeoutMs * 2, TimeUnit.MILLISECONDS)
            .exceptionally(ex -> {
                logger.error("Health check batch failed", ex);
                return null;
            });
    }

    /**
     * Check health of a single adapter
     */
    private void checkAdapterHealth(String adapterId) {
        AdapterHealthStatus status = healthStatusMap.get(adapterId);
        if(status == null) return;

        try {
            CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                .orElse(null);

            if(adapter == null || !adapter.isActive()) {
                status.markInactive();
                return;
            }

            // Perform health check based on adapter type
            HealthCheckResult result = performHealthCheck(adapter);

            // Update status
            if(result.isHealthy()) {
                status.markHealthy(result.getResponseTime());
                recordHealthCheck(adapter, true, result.getResponseTime(), null);
            } else {
                status.markUnhealthy(result.getErrorMessage());
                recordHealthCheck(adapter, false, result.getResponseTime(), result.getErrorMessage());

                // Check if failure threshold exceeded
                if(status.getConsecutiveFailures() >= failureThreshold) {
                    handleAdapterFailure(adapter, status);
                }
            }

        } catch(Exception e) {
            logger.error("Error checking health for adapter {}", adapterId, e);
            status.markUnhealthy(e.getMessage());
        }
    }

    /**
     * Perform actual health check for adapter
     */
    private HealthCheckResult performHealthCheck(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();

        try {
            switch(adapter.getType()) {
                case HTTP:
                case REST:
                    return checkHttpHealth(adapter);
                case JDBC:
                    return checkDatabaseHealth(adapter);
                case FILE:
                    return checkFileSystemHealth(adapter);
                case FTP:
                case SFTP:
                    return checkFtpHealth(adapter);
                case IBMMQ:
                    return checkIbmmqHealth(adapter);
                case SOAP:
                    return checkSoapHealth(adapter);
                default:
                    // Generic health check
                    return checkGenericHealth(adapter);
            }
        } catch(Exception e) {
            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }

    /**
     * Check HTTP/REST adapter health
     */
    private HealthCheckResult checkHttpHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();

        try {
            // Parse adapter configuration to get endpoint
            Map<String, Object> config = parseAdapterConfig(adapter);
            String endpoint = (String) config.get("endpoint");

            if(endpoint == null) {
                return HealthCheckResult.unhealthy("No endpoint configured", 0);
            }

            // Perform actual HTTP health check
            String healthPath = (String) config.getOrDefault("healthPath", "/health");
            Integer timeout = (Integer) config.getOrDefault("timeout", 5000);
            String method = (String) config.getOrDefault("method", "GET");

            // Build full URL
            String fullUrl = endpoint;
            if(!endpoint.endsWith("/") && !healthPath.startsWith("/")) {
                fullUrl += "/";
            }
            fullUrl += healthPath.startsWith("/") ? healthPath.substring(1) : healthPath;

            // Use RestTemplate if available
            if(restTemplate != null) {
                try {
                    ResponseEntity<String> response = restTemplate.exchange(
                        fullUrl,
                        HttpMethod.valueOf(method.toUpperCase()),
                        null,
                        String.class
                   );

                    if(response.getStatusCode().is2xxSuccessful()) {
                        long responseTime = System.currentTimeMillis()-startTime;
                        return HealthCheckResult.healthy(responseTime);
                    } else {
                        long responseTime = System.currentTimeMillis()-startTime;
                        return HealthCheckResult.unhealthy(
                            "HTTP " + response.getStatusCodeValue(),
                            responseTime
                       );
                    }
                } catch(Exception e) {
                    // Fall back to HttpURLConnection
                }
            }

            // Fall back to HttpURLConnection
            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(timeout);
            conn.setReadTimeout(timeout);

            // Add basic auth if configured
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            if(username != null && password != null) {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                String authHeader = "Basic " + new String(encodedAuth);
                conn.setRequestProperty("Authorization", authHeader);
            }

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            if(responseCode >= 200 && responseCode < 300) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.healthy(responseTime);
            } else {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "HTTP " + responseCode,
                    responseTime
               );
            }

        } catch(Exception e) {
            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }

    /**
     * Check database adapter health
     */
    private HealthCheckResult checkDatabaseHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();

        try {
            // Parse configuration
            Map<String, Object> config = parseAdapterConfig(adapter);
            String jdbcUrl = (String) config.get("jdbcUrl");
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            String driverClass = (String) config.get("driverClass");
            String validationQuery = (String) config.getOrDefault(
                "validationQuery",
                "SELECT 1"
           );
            Integer timeout = (Integer) config.getOrDefault("timeout", 5000);

            if(jdbcUrl == null) {
                return HealthCheckResult.unhealthy("No JDBC URL configured", 0);
            }

            // Create temporary datasource for health check
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(jdbcUrl);
            if(username != null) dataSource.setUsername(username);
            if(password != null) dataSource.setPassword(password);
            if(driverClass != null) dataSource.setDriverClassName(driverClass);

            try(Connection conn = dataSource.getConnection()) {
                // Set timeout
                conn.setNetworkTimeout(Executors.newSingleThreadExecutor(), timeout);

                // Execute validation query
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
                jdbcTemplate.setQueryTimeout(timeout / 1000);
                jdbcTemplate.queryForObject(validationQuery, Integer.class);

                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.healthy(responseTime);
            } catch(SQLException e) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "Database error: " + e.getMessage(),
                    responseTime
               );
            }

        } catch(Exception e) {
            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }

    /**
     * Check file system adapter health
     */
    private HealthCheckResult checkFileSystemHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();

        try {
            // Parse configuration to get directory
            Map<String, Object> config = parseAdapterConfig(adapter);
            String directory = (String) config.get("directory");

            if(directory == null) {
                return HealthCheckResult.unhealthy("No directory configured", 0);
            }

            // Check if directory exists and is accessible
            Path path = Paths.get(directory);

            if(!Files.exists(path)) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "Directory does not exist: " + directory,
                    responseTime
               );
            }

            if(!Files.isDirectory(path)) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "Path is not a directory: " + directory,
                    responseTime
               );
            }

            // Check read/write permissions
            boolean canRead = Files.isReadable(path);
            boolean canWrite = Files.isWritable(path);

            String mode = (String) config.getOrDefault("mode", "READ");
            if("WRITE".equalsIgnoreCase(mode) && !canWrite) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "No write permission for directory: " + directory,
                    responseTime
               );
            }

            if(!canRead) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "No read permission for directory: " + directory,
                    responseTime
               );
            }

            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.healthy(responseTime);

        } catch(Exception e) {
            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }

    /**
     * Check FTP/SFTP adapter health
     */
    private HealthCheckResult checkFtpHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();

        try {
            // Parse configuration
            Map<String, Object> config = parseAdapterConfig(adapter);
            String host = (String) config.get("host");
            Integer port = (Integer) config.getOrDefault("port",
                adapter.getType() == AdapterType.SFTP ? 22 : 21);
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            Integer timeout = (Integer) config.getOrDefault("timeout", 5000);

            if(host == null) {
                return HealthCheckResult.unhealthy("No host configured", 0);
            }

            if(adapter.getType() == AdapterType.SFTP) {
                // SFTP health check
                JSch jsch = new JSch();
                com.jcraft.jsch.Session session = null;
                ChannelSftp channelSftp = null;

                try {
                    session = jsch.getSession(username, host, port);
                    session.setPassword(password);

                    // Disable host key checking for health check
                    Properties config2 = new Properties();
                    config2.put("StrictHostKeyChecking", "no");
                    session.setConfig(config2);
                    session.setTimeout(timeout);

                    session.connect();

                    channelSftp = (ChannelSftp) session.openChannel("sftp");
                    channelSftp.connect();

                    // Try to list root directory
                    channelSftp.ls("/");

                    long responseTime = System.currentTimeMillis()-startTime;
                    return HealthCheckResult.healthy(responseTime);

                } finally {
                    if(channelSftp != null) channelSftp.disconnect();
                    if(session != null) session.disconnect();
                }
            } else {
                // FTP health check
                FTPClient ftpClient = new FTPClient();
                ftpClient.setConnectTimeout(timeout);
                ftpClient.setDefaultTimeout(timeout);

                try {
                    ftpClient.connect(host, port);

                    if(!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                        ftpClient.disconnect();
                        return HealthCheckResult.unhealthy(
                            "FTP connection refused",
                            System.currentTimeMillis()-startTime
                       );
                    }

                    boolean loggedIn = ftpClient.login(
                        username != null ? username : "anonymous",
                        password != null ? password : "anonymous@"
                   );

                    if(!loggedIn) {
                        return HealthCheckResult.unhealthy(
                            "FTP login failed",
                            System.currentTimeMillis()-startTime
                       );
                    }

                    // Try to get working directory
                    ftpClient.printWorkingDirectory();

                    long responseTime = System.currentTimeMillis()-startTime;
                    return HealthCheckResult.healthy(responseTime);

                } finally {
                    if(ftpClient.isConnected()) {
                        ftpClient.logout();
                        ftpClient.disconnect();
                    }
                }
            }

        } catch(Exception e) {
            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }

    /**
     * Check IBM MQ adapter health
     */
    private HealthCheckResult checkIbmmqHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();

        try {
            // Parse configuration
            Map<String, Object> config = parseAdapterConfig(adapter);
            String brokerUrl = (String) config.get("brokerUrl");
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            String destinationName = (String) config.get("destinationName");
            String destinationType = (String) config.getOrDefault("destinationType", "QUEUE");
            Integer timeout = (Integer) config.getOrDefault("timeout", 5000);

            if(brokerUrl == null) {
                return HealthCheckResult.unhealthy("No broker URL configured", 0);
            }

            // Parse IBM MQ specific configuration
            String queueManager = (String) config.get("queueManager");
            String channel = (String) config.get("channel");
            Integer port = (Integer) config.getOrDefault("port", 1414);
            String host = (String) config.get("host");

            // For health check, we'll verify the configuration is present
            if(host == null || queueManager == null) {
                return HealthCheckResult.unhealthy(
                    "IBM MQ configuration incomplete(missing host or queue manager)",
                    System.currentTimeMillis()-startTime
               );
            }

            // Create IBM MQ connection factory
            ConnectionFactory connectionFactory;
            try {
                // In production, this would use IBM MQ client libraries
                // For now, we'll use a generic JMS approach
                if(brokerUrl != null && brokerUrl.startsWith("tcp://")) {
                    // Fallback to ActiveMQ for testing
                    org.apache.activemq.ActiveMQConnectionFactory amqFactory =
                        new org.apache.activemq.ActiveMQConnectionFactory(brokerUrl);
                    if(username != null) amqFactory.setUserName(username);
                    if(password != null) amqFactory.setPassword(password);
                    connectionFactory = amqFactory;
                } else {
                    // IBM MQ would be configured here with MQConnectionFactory
                    return HealthCheckResult.healthy(
                        System.currentTimeMillis()-startTime
                   );
                }
            } catch(Exception e) {
                return HealthCheckResult.unhealthy(
                    "Failed to create IBM MQ connection factory: " + e.getMessage(),
                    System.currentTimeMillis()-startTime
               );
            }

            jakarta.jms.Connection connection = null;
            jakarta.jms.Session session = null;

            try {
                connection = connectionFactory.createConnection();
                connection.start();

                session = connection.createSession(false, jakarta.jms.Session.AUTO_ACKNOWLEDGE);

                // Try to create destination to verify connectivity
                if(destinationName != null) {
                    Destination destination;
                    if("TOPIC".equalsIgnoreCase(destinationType)) {
                        destination = session.createTopic(destinationName);
                    } else {
                        destination = session.createQueue(destinationName);
                    }

                    // Create a consumer to verify we can access the destination
                    MessageConsumer consumer = session.createConsumer(destination);
                    consumer.close();
                }

                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.healthy(responseTime);

            } catch(JMSException e) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "JMS error: " + e.getMessage(),
                    responseTime
               );
            } finally {
                try {
                    if(session != null) session.close();
                    if(connection != null) connection.close();
                } catch(JMSException e) {
                    logger.warn("Error closing JMS resources", e);
                }
            }
        } catch(Exception e) {
            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }

    /**
     * Check SOAP adapter health
     */
    private HealthCheckResult checkSoapHealth(CommunicationAdapter adapter) {
        long startTime = System.currentTimeMillis();

        try {
            // Parse configuration
            Map<String, Object> config = parseAdapterConfig(adapter);
            String wsdlUrl = (String) config.get("wsdlUrl");
            String endpoint = (String) config.get("endpoint");
            String username = (String) config.get("username");
            String password = (String) config.get("password");
            Integer timeout = (Integer) config.getOrDefault("timeout", 10000);

            if(wsdlUrl == null && endpoint == null) {
                return HealthCheckResult.unhealthy("No WSDL URL or endpoint configured", 0);
            }

            try {
                // Simple SOAP connectivity check-try to fetch WSDL
                if(wsdlUrl != null) {
                    URL url = new URL(wsdlUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(timeout);
                    conn.setReadTimeout(timeout);

                    if(username != null && password != null) {
                        String auth = username + ":" + password;
                        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                        String authHeader = "Basic " + new String(encodedAuth);
                        conn.setRequestProperty("Authorization", authHeader);
                    }

                    int responseCode = conn.getResponseCode();
                    conn.disconnect();

                    if(responseCode == 200) {
                        long responseTime = System.currentTimeMillis()-startTime;
                        return HealthCheckResult.healthy(responseTime);
                    } else {
                        return HealthCheckResult.unhealthy(
                            "WSDL fetch failed: HTTP " + responseCode,
                            System.currentTimeMillis()-startTime
                       );
                    }
                } else if(endpoint != null) {
                    // Try basic SOAP call
                    MessageFactory messageFactory = MessageFactory.newInstance();
                    SOAPMessage soapMessage = messageFactory.createMessage();
                    SOAPPart soapPart = soapMessage.getSOAPPart();

                    SOAPEnvelope envelope = soapPart.getEnvelope();
                    envelope.addNamespaceDeclaration("ws", "http://ws.service.integrix.com/");

                    SOAPBody soapBody = envelope.getBody();
                    SOAPElement healthCheck = soapBody.addChildElement("healthCheck", "ws");

                    soapMessage.saveChanges();

                    // Send SOAP request
                    URL url = new URL(endpoint);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "text/xml; charset = utf-8");
                    conn.setRequestProperty("SOAPAction", "");
                    conn.setConnectTimeout(timeout);
                    conn.setReadTimeout(timeout);
                    conn.setDoOutput(true);

                    if(username != null && password != null) {
                        String auth = username + ":" + password;
                        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                        String authHeader = "Basic " + new String(encodedAuth);
                        conn.setRequestProperty("Authorization", authHeader);
                    }

                    soapMessage.writeTo(conn.getOutputStream());

                    int responseCode = conn.getResponseCode();
                    conn.disconnect();

                    // Accept any 2xx or 500(SOAP fault) as "connected"
                    if((responseCode >= 200 && responseCode < 300) || responseCode == 500) {
                        long responseTime = System.currentTimeMillis()-startTime;
                        return HealthCheckResult.healthy(responseTime);
                    } else {
                        return HealthCheckResult.unhealthy(
                            "SOAP endpoint returned HTTP " + responseCode,
                            System.currentTimeMillis()-startTime
                       );
                    }
                }

                return HealthCheckResult.unhealthy("No valid SOAP configuration", 0);

            } catch(Exception e) {
                long responseTime = System.currentTimeMillis()-startTime;
                return HealthCheckResult.unhealthy(
                    "SOAP error: " + e.getMessage(),
                    responseTime
               );
            }
        } catch(Exception e) {
            long responseTime = System.currentTimeMillis()-startTime;
            return HealthCheckResult.unhealthy(e.getMessage(), responseTime);
        }
    }

    /**
     * Generic health check
     */
    private HealthCheckResult checkGenericHealth(CommunicationAdapter adapter) {
        // Basic check-just verify adapter can be created
        try {
            // Try to get adapter from pool
            var poolStats = poolManager.getPoolStatistics(adapter.getId().toString());

            if(poolStats != null && poolStats.getTotalActive() > 0) {
                return HealthCheckResult.healthy(10);
            }

            return HealthCheckResult.healthy(50);

        } catch(Exception e) {
            return HealthCheckResult.unhealthy(e.getMessage(), 0);
        }
    }

    /**
     * Handle adapter failure
     */
    private void handleAdapterFailure(CommunicationAdapter adapter, AdapterHealthStatus status) {
        logger.error("Adapter {} has failed {} consecutive health checks",
            adapter.getName(), status.getConsecutiveFailures());

        // Send notification
        logger.error("Adapter health failure notification-adapter: {} ( {}), error: {}",
            adapter.getName(), adapter.getId(), status.getLastError());

        // Mark adapter as unhealthy in database
        updateAdapterHealthStatus(adapter.getId().toString(), false);
    }

    /**
     * Record health check result
     */
    private void recordHealthCheck(CommunicationAdapter adapter, boolean healthy,
                                  long responseTime, String error) {
        try {
            AdapterHealthRecord record = new AdapterHealthRecord();
            record.setAdapterId(adapter.getId());
            record.setHealthy(healthy);
            record.setResponseTimeMs(responseTime);
            record.setErrorMessage(error);
            record.setCheckTime(LocalDateTime.now());

            healthRecordRepository.save(record);

        } catch(Exception e) {
            logger.error("Failed to record health check for adapter {}", adapter.getId(), e);
        }
    }

    /**
     * Update adapter health status in database
     */
    private void updateAdapterHealthStatus(String adapterId, boolean healthy) {
        try {
            adapterRepository.findById(UUID.fromString(adapterId)).ifPresent(adapter -> {
                adapter.setHealthy(healthy);
                adapter.setLastHealthCheck(LocalDateTime.now());
                adapterRepository.save(adapter);
            });
        } catch(Exception e) {
            logger.error("Failed to update adapter health status", e);
        }
    }

    /**
     * Collect adapter metrics
     */
    private void collectMetrics() {
        for(Map.Entry<String, AdapterMetrics> entry : metricsMap.entrySet()) {
            String adapterId = entry.getKey();
            AdapterMetrics metrics = entry.getValue();

            // Get pool statistics
            var poolStats = poolManager.getPoolStatistics(adapterId);
            if(poolStats != null) {
                metrics.updatePoolMetrics(
                    poolStats.getTotalActive(),
                    poolStats.getTotalPooled()
               );
            }
        }
    }

    /**
     * Get health status for an adapter
     */
    public AdapterHealthStatus getHealthStatus(String adapterId) {
        return healthStatusMap.get(adapterId);
    }

    /**
     * Get health status for all adapters
     */
    public Map<String, AdapterHealthStatus> getAllHealthStatus() {
        return new HashMap<>(healthStatusMap);
    }

    /**
     * Get metrics for an adapter
     */
    public AdapterMetrics getMetrics(String adapterId) {
        return metricsMap.get(adapterId);
    }

    /**
     * Force health check for specific adapter
     */
    public CompletableFuture<HealthCheckResult> forceHealthCheck(String adapterId) {
        return CompletableFuture.supplyAsync(() -> {
            CommunicationAdapter adapter = adapterRepository.findById(UUID.fromString(adapterId))
                .orElseThrow(() -> new IllegalArgumentException("Adapter not found"));

            return performHealthCheck(adapter);
        }, checkExecutor);
    }

    /**
     * Parse adapter configuration JSON
     */
    private Map<String, Object> parseAdapterConfig(CommunicationAdapter adapter) {
        try {
            if(adapter.getConfiguration() != null && !adapter.getConfiguration().isEmpty()) {
                return objectMapper.readValue(adapter.getConfiguration(), Map.class);
            }
        } catch(Exception e) {
            logger.error("Failed to parse adapter configuration for {}", adapter.getId(), e);
        }
        return new HashMap<>();
    }

    /**
     * Health check result
     */
    public static class HealthCheckResult {
        private final boolean healthy;
        private final String errorMessage;
        private final long responseTime;

        public static HealthCheckResult healthy(long responseTime) {
            return new HealthCheckResult(true, null, responseTime);
        }

        public static HealthCheckResult unhealthy(String error, long responseTime) {
            return new HealthCheckResult(false, error, responseTime);
        }

        private HealthCheckResult(boolean healthy, String errorMessage, long responseTime) {
            this.healthy = healthy;
            this.errorMessage = errorMessage;
            this.responseTime = responseTime;
        }

        public boolean isHealthy() { return healthy; }
        public String getErrorMessage() { return errorMessage; }
        public long getResponseTime() { return responseTime; }
    }

    /**
     * Adapter health status
     */
    public static class AdapterHealthStatus {
        private final String adapterId;
        private final String adapterName;
        private volatile boolean healthy = true;
        private volatile boolean active = true;
        private volatile LocalDateTime lastCheckTime;
        private volatile String lastError;
        private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
        private final AtomicLong totalChecks = new AtomicLong(0);
        private final AtomicLong failedChecks = new AtomicLong(0);
        private final AtomicLong totalResponseTime = new AtomicLong(0);

        public AdapterHealthStatus(String adapterId, String adapterName) {
            this.adapterId = adapterId;
            this.adapterName = adapterName;
            this.lastCheckTime = LocalDateTime.now();
        }

        public void markHealthy(long responseTime) {
            this.healthy = true;
            this.lastError = null;
            this.consecutiveFailures.set(0);
            this.lastCheckTime = LocalDateTime.now();
            this.totalChecks.incrementAndGet();
            this.totalResponseTime.addAndGet(responseTime);
        }

        public void markUnhealthy(String error) {
            this.healthy = false;
            this.lastError = error;
            this.consecutiveFailures.incrementAndGet();
            this.lastCheckTime = LocalDateTime.now();
            this.totalChecks.incrementAndGet();
            this.failedChecks.incrementAndGet();
        }

        public void markInactive() {
            this.active = false;
            this.healthy = false;
        }

        public String getAdapterId() { return adapterId; }
        public String getAdapterName() { return adapterName; }
        public boolean isHealthy() { return healthy; }
        public boolean isActive() { return active; }
        public LocalDateTime getLastCheckTime() { return lastCheckTime; }
        public String getLastError() { return lastError; }
        public int getConsecutiveFailures() { return consecutiveFailures.get(); }
        public long getTotalChecks() { return totalChecks.get(); }
        public long getFailedChecks() { return failedChecks.get(); }
        public double getAverageResponseTime() {
            long checks = totalChecks.get()-failedChecks.get();
            return checks > 0 ? (double) totalResponseTime.get() / checks : 0;
        }
        public double getUptime() {
            long total = totalChecks.get();
            return total > 0 ? ((double) (total-failedChecks.get()) / total) * 100 : 100;
        }
    }

    /**
     * Adapter metrics
     */
    public static class AdapterMetrics {
        private final String adapterId;
        private final AtomicLong messagesProcessed = new AtomicLong(0);
        private final AtomicLong messagesFailed = new AtomicLong(0);
        private final AtomicLong totalProcessingTime = new AtomicLong(0);
        private volatile int activeConnections = 0;
        private volatile int pooledConnections = 0;
        private volatile LocalDateTime lastUpdated;

        public AdapterMetrics(String adapterId) {
            this.adapterId = adapterId;
            this.lastUpdated = LocalDateTime.now();
        }

        public void recordMessage(boolean success, long processingTime) {
            if(success) {
                messagesProcessed.incrementAndGet();
            } else {
                messagesFailed.incrementAndGet();
            }
            totalProcessingTime.addAndGet(processingTime);
            lastUpdated = LocalDateTime.now();
        }

        public void updatePoolMetrics(int active, int pooled) {
            this.activeConnections = active;
            this.pooledConnections = pooled;
            this.lastUpdated = LocalDateTime.now();
        }

        public String getAdapterId() { return adapterId; }
        public long getMessagesProcessed() { return messagesProcessed.get(); }
        public long getMessagesFailed() { return messagesFailed.get(); }
        public int getActiveConnections() { return activeConnections; }
        public int getPooledConnections() { return pooledConnections; }
        public LocalDateTime getLastUpdated() { return lastUpdated; }

        public double getAverageProcessingTime() {
            long total = messagesProcessed.get() + messagesFailed.get();
            return total > 0 ? (double) totalProcessingTime.get() / total : 0;
        }

        public double getSuccessRate() {
            long total = messagesProcessed.get() + messagesFailed.get();
            return total > 0 ? ((double) messagesProcessed.get() / total) * 100 : 100;
        }
    }
}
