package com.integrixs.backend.service;

import com.integrixs.backend.api.dto.request.ConnectionTestRequest;
import com.integrixs.backend.api.dto.response.ConnectionTestResponse;
import com.integrixs.backend.api.dto.response.ConnectionDiagnostic;
import com.integrixs.shared.enums.AdapterType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.client.ResourceAccessException;
import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.net.Socket;
import java.net.URI;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import javax.net.ssl.SSLSocketFactory;
import jakarta.jms.ConnectionFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.JMSException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AdapterConnectionTestService {

    private static final Logger log = LoggerFactory.getLogger(AdapterConnectionTestService.class);


    private final RestTemplate restTemplate;

    public AdapterConnectionTestService(@Qualifier("connectionTestRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ConnectionTestResponse testConnection(ConnectionTestRequest request) {
        log.info("Testing connection for adapter type: {} with name: {}",
                request.getAdapterType(), request.getAdapterName());

        long startTime = System.currentTimeMillis();
        List<ConnectionDiagnostic> diagnostics = new ArrayList<>();
        boolean success = false;
        String message = "";
        Map<String, Object> metadata = new HashMap<>();

        try {
            switch(request.getAdapterType()) {
                case REST:
                    return testRestConnection(request, diagnostics);
                case SOAP:
                    return testSoapConnection(request, diagnostics);
                case JDBC:
                    return testDatabaseConnection(request, diagnostics);
                case FILE:
                    return testFileConnection(request, diagnostics);
                case IBMMQ:
                    return testIbmmqConnection(request, diagnostics);
                case RABBITMQ:
                    return testRabbitMqConnection(request, diagnostics);
                case KAFKA:
                    return testKafkaConnection(request, diagnostics);
                case SFTP:
                    return testSftpConnection(request, diagnostics);
                case EMAIL:
                    return testEmailConnection(request, diagnostics);
                default:
                    log.warn("Unsupported adapter type: {}", request.getAdapterType());
                    return createFailureResponse("Unsupported adapter type: " + request.getAdapterType(), diagnostics, System.currentTimeMillis());
            }
        } catch(Exception e) {
            log.error("Connection test failed", e);
            diagnostics.add(ConnectionDiagnostic.builder()
                    .step("Connection Test")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Connection test failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - startTime)
                    .build());

            return ConnectionTestResponse.builder()
                    .success(false)
                    .message("Connection test failed: " + e.getMessage())
                    .diagnostics(diagnostics)
                    .duration(System.currentTimeMillis() - startTime)
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private ConnectionTestResponse testRestConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> config = request.getConfiguration();
        String endpoint = (String) config.get("endpoint");
        String method = (String) config.getOrDefault("method", "GET");

        if(endpoint == null || endpoint.isEmpty()) {
            return createFailureResponse("Endpoint URL is required", diagnostics, startTime);
        }

        // Step 1: Validate URL
        diagnostics.add(testUrlValidity(endpoint));

        // Step 2: DNS Resolution
        diagnostics.add(testDnsResolution(endpoint));

        // Step 3: Port Connectivity
        diagnostics.add(testPortConnectivity(endpoint));

        // Step 4: HTTP Request
        ConnectionDiagnostic httpTest = testHttpRequest(endpoint, method, config);
        diagnostics.add(httpTest);

        boolean success = diagnostics.stream().allMatch(d -> d.getStatus() == ConnectionDiagnostic.Status.SUCCESS);

        return ConnectionTestResponse.builder()
                .success(success)
                .message(success ? "REST connection test successful" : "REST connection test failed")
                .diagnostics(diagnostics)
                .duration(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .metadata(Map.of(
                        "endpoint", endpoint,
                        "method", method,
                        "responseTime", httpTest.getDuration() + "ms"
               ))
                .build();
    }

    private ConnectionTestResponse testSoapConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> config = request.getConfiguration();
        String wsdlUrl = (String) config.get("wsdlUrl");
        String endpoint = (String) config.get("endpoint");

        if(wsdlUrl == null || wsdlUrl.isEmpty()) {
            return createFailureResponse("WSDL URL is required", diagnostics, startTime);
        }

        // Step 1: Validate WSDL URL
        diagnostics.add(testUrlValidity(wsdlUrl));

        // Step 2: Fetch WSDL
        ConnectionDiagnostic wsdlTest = testWsdlFetch(wsdlUrl);
        diagnostics.add(wsdlTest);

        // Step 3: Test SOAP endpoint if provided
        if(endpoint != null && !endpoint.isEmpty()) {
            diagnostics.add(testSoapEndpoint(endpoint, config));
        }

        boolean success = diagnostics.stream().allMatch(d -> d.getStatus() == ConnectionDiagnostic.Status.SUCCESS);

        return ConnectionTestResponse.builder()
                .success(success)
                .message(success ? "SOAP connection test successful" : "SOAP connection test failed")
                .diagnostics(diagnostics)
                .duration(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .metadata(Map.of("wsdlUrl", wsdlUrl))
                .build();
    }

    private ConnectionTestResponse testDatabaseConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> config = request.getConfiguration();
        String jdbcUrl = (String) config.get("jdbcUrl");
        String username = (String) config.get("username");
        String password = (String) config.get("password");
        String driverClassName = (String) config.get("driverClassName");

        if(jdbcUrl == null || jdbcUrl.isEmpty()) {
            return createFailureResponse("JDBC URL is required", diagnostics, startTime);
        }

        // Step 1: Validate JDBC URL format
        diagnostics.add(testJdbcUrlFormat(jdbcUrl));

        // Step 2: Test database connection
        ConnectionDiagnostic dbTest = testDatabaseConnectivity(jdbcUrl, username, password, driverClassName);
        diagnostics.add(dbTest);

        boolean success = diagnostics.stream().allMatch(d -> d.getStatus() == ConnectionDiagnostic.Status.SUCCESS);

        return ConnectionTestResponse.builder()
                .success(success)
                .message(success ? "Database connection test successful" : "Database connection test failed")
                .diagnostics(diagnostics)
                .duration(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .metadata(dbTest.getDetails())
                .build();
    }

    private ConnectionTestResponse testIbmmqConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> config = request.getConfiguration();
        String brokerUrl = (String) config.get("brokerUrl");
        String username = (String) config.get("username");
        String password = (String) config.get("password");

        if(brokerUrl == null || brokerUrl.isEmpty()) {
            return createFailureResponse("Broker URL is required", diagnostics, startTime);
        }

        // Test IBM MQ connection
        ConnectionDiagnostic ibmmqTest = testIbmmqConnectivity(brokerUrl, username, password);
        diagnostics.add(ibmmqTest);

        boolean success = ibmmqTest.getStatus() == ConnectionDiagnostic.Status.SUCCESS;

        return ConnectionTestResponse.builder()
                .success(success)
                .message(success ? "IBM MQ connection test successful" : "IBM MQ connection test failed")
                .diagnostics(diagnostics)
                .duration(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .metadata(Map.of("brokerUrl", brokerUrl))
                .build();
    }

    private ConnectionTestResponse testRabbitMqConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        long startTime = System.currentTimeMillis();
        Map<String, Object> config = request.getConfiguration();
        String host = (String) config.get("host");
        Integer port = (Integer) config.getOrDefault("port", 5672);
        String username = (String) config.getOrDefault("username", "guest");
        String password = (String) config.getOrDefault("password", "guest");
        String virtualHost = (String) config.getOrDefault("virtualHost", "/");

        if(host == null || host.isEmpty()) {
            return createFailureResponse("Host is required", diagnostics, startTime);
        }

        // Test RabbitMQ connection
        ConnectionDiagnostic rabbitTest = testRabbitMqConnectivity(host, port, username, password, virtualHost);
        diagnostics.add(rabbitTest);

        boolean success = rabbitTest.getStatus() == ConnectionDiagnostic.Status.SUCCESS;

        return ConnectionTestResponse.builder()
                .success(success)
                .message(success ? "RabbitMQ connection test successful" : "RabbitMQ connection test failed")
                .diagnostics(diagnostics)
                .duration(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .metadata(Map.of("host", host, "port", port, "virtualHost", virtualHost))
                .build();
    }

    // Helper methods for specific tests

    private ConnectionDiagnostic testUrlValidity(String url) {
        long start = System.currentTimeMillis();
        try {
            URI uri = new URI(url);
            if(uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException("Invalid URL format");
            }
            return ConnectionDiagnostic.builder()
                    .step("URL Validation")
                    .status(ConnectionDiagnostic.Status.SUCCESS)
                    .message("URL is valid")
                    .duration(System.currentTimeMillis() - start)
                    .details(Map.of("url", url, "scheme", uri.getScheme(), "host", uri.getHost()))
                    .build();
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("URL Validation")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Invalid URL: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private ConnectionDiagnostic testDnsResolution(String url) {
        long start = System.currentTimeMillis();
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            java.net.InetAddress[] addresses = java.net.InetAddress.getAllByName(host);

            return ConnectionDiagnostic.builder()
                    .step("DNS Resolution")
                    .status(ConnectionDiagnostic.Status.SUCCESS)
                    .message("DNS resolution successful")
                    .duration(System.currentTimeMillis() - start)
                    .details(Map.of(
                            "host", host,
                            "resolvedAddresses", Arrays.toString(addresses)
                   ))
                    .build();
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("DNS Resolution")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("DNS resolution failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private ConnectionDiagnostic testPortConnectivity(String url) {
        long start = System.currentTimeMillis();
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() :
                      ("https".equals(uri.getScheme()) ? 443 : 80);

            try(Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 5000);
                return ConnectionDiagnostic.builder()
                        .step("Port Connectivity")
                        .status(ConnectionDiagnostic.Status.SUCCESS)
                        .message("Port " + port + " is reachable")
                        .duration(System.currentTimeMillis() - start)
                        .details(Map.of("host", host, "port", port))
                        .build();
            }
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("Port Connectivity")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Port connectivity failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private ConnectionDiagnostic testHttpRequest(String endpoint, String method, Map<String, Object> config) {
        long start = System.currentTimeMillis();
        try {
            HttpHeaders headers = new HttpHeaders();

            // Add authentication if provided
            if(config.containsKey("authType")) {
                String authType = (String) config.get("authType");
                if("BASIC".equals(authType)) {
                    String username = (String) config.get("username");
                    String password = (String) config.get("password");
                    if(username != null && password != null) {
                        String auth = username + ":" + password;
                        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                        headers.add("Authorization", "Basic " + encodedAuth);
                    }
                } else if("BEARER".equals(authType)) {
                    String token = (String) config.get("token");
                    if(token != null) {
                        headers.add("Authorization", "Bearer " + token);
                    }
                }
            }

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
           );

            return ConnectionDiagnostic.builder()
                    .step("HTTP Request")
                    .status(ConnectionDiagnostic.Status.SUCCESS)
                    .message("HTTP request successful")
                    .duration(System.currentTimeMillis() - start)
                    .details(Map.of(
                            "statusCode", response.getStatusCodeValue(),
                            "headers", response.getHeaders().size(),
                            "responseSize", response.getBody() != null ? response.getBody().length() : 0
                   ))
                    .build();
        } catch(ResourceAccessException e) {
            return ConnectionDiagnostic.builder()
                    .step("HTTP Request")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Connection timeout or refused: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("HTTP Request")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("HTTP request failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private ConnectionDiagnostic testWsdlFetch(String wsdlUrl) {
        long start = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(wsdlUrl, String.class);
            String wsdlContent = response.getBody();

            if(wsdlContent == null || !wsdlContent.contains("definitions")) {
                throw new IllegalArgumentException("Invalid WSDL content");
            }

            return ConnectionDiagnostic.builder()
                    .step("WSDL Fetch")
                    .status(ConnectionDiagnostic.Status.SUCCESS)
                    .message("WSDL fetched successfully")
                    .duration(System.currentTimeMillis() - start)
                    .details(Map.of(
                            "contentLength", wsdlContent.length(),
                            "containsDefinitions", true
                   ))
                    .build();
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("WSDL Fetch")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Failed to fetch WSDL: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private ConnectionDiagnostic testSoapEndpoint(String endpoint, Map<String, Object> config) {
        long start = System.currentTimeMillis();
        // Simplified SOAP endpoint test - in production, would use proper SOAP client
        return testHttpRequest(endpoint, "POST", config);
    }

    private ConnectionDiagnostic testJdbcUrlFormat(String jdbcUrl) {
        long start = System.currentTimeMillis();
        try {
            if(!jdbcUrl.startsWith("jdbc:")) {
                throw new IllegalArgumentException("JDBC URL must start with 'jdbc:'");
            }

            return ConnectionDiagnostic.builder()
                    .step("JDBC URL Validation")
                    .status(ConnectionDiagnostic.Status.SUCCESS)
                    .message("JDBC URL format is valid")
                    .duration(System.currentTimeMillis() - start)
                    .build();
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("JDBC URL Validation")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Invalid JDBC URL: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        }
    }

    private ConnectionDiagnostic testDatabaseConnectivity(String jdbcUrl, String username,
                                                         String password, String driverClassName) {
        long start = System.currentTimeMillis();
        Connection connection = null;
        try {
            DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
            dataSourceBuilder.url(jdbcUrl);

            if(username != null) dataSourceBuilder.username(username);
            if(password != null) dataSourceBuilder.password(password);
            if(driverClassName != null) dataSourceBuilder.driverClassName(driverClassName);

            DataSource dataSource = dataSourceBuilder.build();
            connection = dataSource.getConnection();

            DatabaseMetaData metaData = connection.getMetaData();

            return ConnectionDiagnostic.builder()
                    .step("Database Connection")
                    .status(ConnectionDiagnostic.Status.SUCCESS)
                    .message("Database connection successful")
                    .duration(System.currentTimeMillis() - start)
                    .details(Map.of(
                            "databaseProduct", metaData.getDatabaseProductName(),
                            "databaseVersion", metaData.getDatabaseProductVersion(),
                            "driverName", metaData.getDriverName(),
                            "driverVersion", metaData.getDriverVersion()
                   ))
                    .build();
        } catch(SQLException e) {
            return ConnectionDiagnostic.builder()
                    .step("Database Connection")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Database connection failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .details(Map.of("sqlState", e.getSQLState(), "errorCode", e.getErrorCode()))
                    .build();
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("Database Connection")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("Database connection failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch(SQLException e) {
                    log.warn("Failed to close database connection", e);
                }
            }
        }
    }

    private ConnectionDiagnostic testIbmmqConnectivity(String brokerUrl, String username, String password) {
        long start = System.currentTimeMillis();
        jakarta.jms.Connection connection = null;
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setBrokerURL(brokerUrl);
            if(username != null) connectionFactory.setUserName(username);
            if(password != null) connectionFactory.setPassword(password);

            connection = connectionFactory.createConnection();
            connection.start();

            return ConnectionDiagnostic.builder()
                    .step("IBM MQ Connection")
                    .status(ConnectionDiagnostic.Status.SUCCESS)
                    .message("IBM MQ connection successful")
                    .duration(System.currentTimeMillis() - start)
                    .details(Map.of("brokerUrl", brokerUrl))
                    .build();
        } catch(JMSException e) {
            return ConnectionDiagnostic.builder()
                    .step("IBM MQ Connection")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("IBM MQ connection failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        } finally {
            if(connection != null) {
                try {
                    connection.close();
                } catch(JMSException e) {
                    log.warn("Failed to close JMS connection", e);
                }
            }
        }
    }

    private ConnectionDiagnostic testRabbitMqConnectivity(String host, int port, String username,
                                                         String password, String virtualHost) {
        long start = System.currentTimeMillis();
        try {
            com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVirtualHost(virtualHost);
            factory.setConnectionTimeout(5000);

            try(com.rabbitmq.client.Connection connection = factory.newConnection()) {
                return ConnectionDiagnostic.builder()
                        .step("RabbitMQ Connection")
                        .status(ConnectionDiagnostic.Status.SUCCESS)
                        .message("RabbitMQ connection successful")
                        .duration(System.currentTimeMillis() - start)
                        .details(Map.of(
                                "serverVersion", connection.getServerProperties().get("version"),
                                "cluster", connection.getServerProperties().get("cluster_name")
                       ))
                        .build();
            }
        } catch(Exception e) {
            return ConnectionDiagnostic.builder()
                    .step("RabbitMQ Connection")
                    .status(ConnectionDiagnostic.Status.FAILED)
                    .message("RabbitMQ connection failed: " + e.getMessage())
                    .duration(System.currentTimeMillis() - start)
                    .build();
        }
    }

    // Placeholder implementations for other adapter types

    private ConnectionTestResponse testKafkaConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        return createFailureResponse("Kafka connection testing not yet implemented", diagnostics, System.currentTimeMillis());
    }

    private ConnectionTestResponse testFileConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        return createFailureResponse("File connection testing not yet implemented", diagnostics, System.currentTimeMillis());
    }

    private ConnectionTestResponse testSftpConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        return createFailureResponse("SFTP connection testing not yet implemented", diagnostics, System.currentTimeMillis());
    }

    private ConnectionTestResponse testEmailConnection(ConnectionTestRequest request, List<ConnectionDiagnostic> diagnostics) {
        return createFailureResponse("Email connection testing not yet implemented", diagnostics, System.currentTimeMillis());
    }

    private ConnectionTestResponse createFailureResponse(String message, List<ConnectionDiagnostic> diagnostics, long startTime) {
        diagnostics.add(ConnectionDiagnostic.builder()
                .step("Validation")
                .status(ConnectionDiagnostic.Status.FAILED)
                .message(message)
                .duration(System.currentTimeMillis() - startTime)
                .build());

        return ConnectionTestResponse.builder()
                .success(false)
                .message(message)
                .diagnostics(diagnostics)
                .duration(System.currentTimeMillis() - startTime)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
