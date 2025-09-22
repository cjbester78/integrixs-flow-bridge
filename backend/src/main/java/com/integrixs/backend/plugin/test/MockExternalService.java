package com.integrixs.backend.plugin.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock external service for plugin testing
 */
public class MockExternalService {

    private static final Logger log = LoggerFactory.getLogger(MockExternalService.class);


    private HttpServer server;
    private final int port;
    private final Map<String, List<ReceivedMessage>> receivedMessages = new ConcurrentHashMap<>();
    private final Map<String, MockResponse> responses = new ConcurrentHashMap<>();
    private final AtomicInteger requestCount = new AtomicInteger(0);

    public MockExternalService() {
        this(0); // Use random available port
    }

    public MockExternalService(int port) {
        this.port = port;
    }

    /**
     * Start the mock service
     */
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());

        // Setup endpoints
        server.createContext("/api/test", new TestHandler());
        server.createContext("/api/send", new SendHandler());
        server.createContext("/api/receive", new ReceiveHandler());
        server.createContext("/api/batch", new BatchHandler());
        server.createContext("/health", new HealthHandler());

        server.start();
        log.info("Mock service started on port {}", getPort());
    }

    /**
     * Stop the mock service
     */
    public void stop() {
        if(server != null) {
            server.stop(0);
            log.info("Mock service stopped");
        }
    }

    /**
     * Get the actual port being used
     */
    public int getPort() {
        return server != null ? server.getAddress().getPort() : port;
    }

    /**
     * Get the base URL
     */
    public String getBaseUrl() {
        return "http://localhost:" + getPort();
    }

    /**
     * Set a mock response for a path
     */
    public void setResponse(String path, MockResponse response) {
        responses.put(path, response);
    }

    /**
     * Get received messages for a path
     */
    public List<ReceivedMessage> getReceivedMessages(String path) {
        return receivedMessages.getOrDefault(path, new ArrayList<>());
    }

    /**
     * Clear all received messages
     */
    public void clearReceivedMessages() {
        receivedMessages.clear();
    }

    /**
     * Get request count
     */
    public int getRequestCount() {
        return requestCount.get();
    }

    /**
     * Reset request count
     */
    public void resetRequestCount() {
        requestCount.set(0);
    }

    private class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();

            String response = " {\"status\":\"ok\",\"timestamp\":" + System.currentTimeMillis() + "}";
            exchange.getResponseHeaders().add("Content - Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());

            try(OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private class SendHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();

            if(!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }

            // Read request body
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            // Store received message
            ReceivedMessage message = ReceivedMessage.builder()
                    .path("/api/send")
                    .headers(exchange.getRequestHeaders())
                    .body(body)
                    .timestamp(System.currentTimeMillis())
                    .build();

            receivedMessages.computeIfAbsent("/api/send", k -> new ArrayList<>()).add(message);

            // Return response
            MockResponse mockResponse = responses.get("/api/send");
            if(mockResponse != null) {
                exchange.sendResponseHeaders(mockResponse.getStatusCode(),
                    mockResponse.getBody().length());
                try(OutputStream os = exchange.getResponseBody()) {
                    os.write(mockResponse.getBody().getBytes(StandardCharsets.UTF_8));
                }
            } else {
                String response = " {\"id\":\"" + UUID.randomUUID() + "\",\"status\":\"received\"}";
                exchange.getResponseHeaders().add("Content - Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try(OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

    private class ReceiveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();

            // Generate some test data
            String response = "[" +
                " {\"id\":\"msg-1\",\"data\":\"test data 1\",\"timestamp\":" + System.currentTimeMillis() + "}," +
                " {\"id\":\"msg-2\",\"data\":\"test data 2\",\"timestamp\":" + System.currentTimeMillis() + "}" +
                "]";

            exchange.getResponseHeaders().add("Content - Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());

            try(OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private class BatchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();

            if(!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, 0);
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            ReceivedMessage message = ReceivedMessage.builder()
                    .path("/api/batch")
                    .headers(exchange.getRequestHeaders())
                    .body(body)
                    .timestamp(System.currentTimeMillis())
                    .build();

            receivedMessages.computeIfAbsent("/api/batch", k -> new ArrayList<>()).add(message);

            // Return batch response
            String response = " {\"processed\":3,\"successful\":3,\"failed\":0}";
            exchange.getResponseHeaders().add("Content - Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());

            try(OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    private class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            requestCount.incrementAndGet();

            String response = " {\"status\":\"healthy\",\"uptime\":" +
                System.currentTimeMillis() + "}";

            exchange.getResponseHeaders().add("Content - Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());

            try(OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes(StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * Mock response configuration
     */
    public static class MockResponse {
        private int statusCode;
        private String body;
        private Map<String, String> headers;

        // Getters and Setters
        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        // Builder pattern
        public static MockResponseBuilder builder() {
            return new MockResponseBuilder();
        }

        public static class MockResponseBuilder {
            private int statusCode;
            private String body;
            private Map<String, String> headers;

            public MockResponseBuilder statusCode(int statusCode) {
                this.statusCode = statusCode;
                return this;
            }

            public MockResponseBuilder body(String body) {
                this.body = body;
                return this;
            }

            public MockResponseBuilder headers(Map<String, String> headers) {
                this.headers = headers;
                return this;
            }

            public MockResponse build() {
                MockResponse response = new MockResponse();
                response.statusCode = this.statusCode;
                response.body = this.body;
                response.headers = this.headers;
                return response;
            }
        }
    }

    /**
     * Received message
     */
    public static class ReceivedMessage {
        private String path;
        private Map<String, List<String>> headers;
        private String body;
        private long timestamp;

        // Getters and Setters
        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        // Builder pattern
        public static ReceivedMessageBuilder builder() {
            return new ReceivedMessageBuilder();
        }

        public static class ReceivedMessageBuilder {
            private String path;
            private Map<String, List<String>> headers;
            private String body;
            private long timestamp;

            public ReceivedMessageBuilder path(String path) {
                this.path = path;
                return this;
            }

            public ReceivedMessageBuilder headers(Map<String, List<String>> headers) {
                this.headers = headers;
                return this;
            }

            public ReceivedMessageBuilder body(String body) {
                this.body = body;
                return this;
            }

            public ReceivedMessageBuilder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public ReceivedMessage build() {
                ReceivedMessage message = new ReceivedMessage();
                message.path = this.path;
                message.headers = this.headers;
                message.body = this.body;
                message.timestamp = this.timestamp;
                return message;
            }
        }
    }
}
