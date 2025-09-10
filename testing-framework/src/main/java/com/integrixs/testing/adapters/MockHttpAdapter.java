package com.integrixs.testing.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Mock HTTP adapter for testing
 */
public class MockHttpAdapter implements MockAdapter {
    
    private final Map<String, MockResponse> responses = new ConcurrentHashMap<>();
    private final List<HttpRequest> capturedRequests = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong callCount = new AtomicLong();
    private final AtomicLong errorCount = new AtomicLong();
    private final AtomicLong totalResponseTime = new AtomicLong();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private long delay = 0;
    private String error = null;
    private boolean captureRequests = false;
    private Consumer<HttpRequest> requestHandler;
    
    /**
     * Add a response for any request
     */
    public void addResponse(int status, String body) {
        MockResponse response = new MockResponse();
        response.status = status;
        response.body = body;
        response.headers = new HashMap<>();
        responses.put("*", response);
    }
    
    /**
     * Add JSON response
     */
    public void addJsonResponse(int status, Object object) {
        try {
            String json = objectMapper.writeValueAsString(object);
            MockResponse response = new MockResponse();
            response.status = status;
            response.body = json;
            response.headers = new HashMap<>();
            response.headers.put("Content-Type", "application/json");
            responses.put("*", response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }
    
    /**
     * Add response for specific endpoint
     */
    public void addResponse(String method, String path, int status, String body) {
        String key = method + " " + path;
        MockResponse response = new MockResponse();
        response.status = status;
        response.body = body;
        response.headers = new HashMap<>();
        responses.put(key, response);
    }
    
    /**
     * Add response header
     */
    public void addResponseHeader(String key, String value) {
        responses.values().forEach(response -> response.headers.put(key, value));
    }
    
    /**
     * Set response delay
     */
    public void setDelay(long milliseconds) {
        this.delay = milliseconds;
    }
    
    /**
     * Set error to simulate
     */
    public void setError(String error) {
        this.error = error;
    }
    
    /**
     * Enable request capture
     */
    public void setCaptureRequests(boolean capture) {
        this.captureRequests = capture;
    }
    
    /**
     * Set request handler
     */
    public void setRequestHandler(Consumer<HttpRequest> handler) {
        this.requestHandler = handler;
    }
    
    /**
     * Get captured requests
     */
    public List<HttpRequest> getCapturedRequests() {
        return new ArrayList<>(capturedRequests);
    }
    
    /**
     * Handle HTTP request
     */
    public MockResponse handleRequest(HttpRequest request) {
        long startTime = System.currentTimeMillis();
        callCount.incrementAndGet();
        
        try {
            if (captureRequests) {
                capturedRequests.add(request);
            }
            
            if (requestHandler != null) {
                requestHandler.accept(request);
            }
            
            if (error != null) {
                errorCount.incrementAndGet();
                throw new RuntimeException(error);
            }
            
            if (delay > 0) {
                Thread.sleep(delay);
            }
            
            // Find matching response
            String key = request.method + " " + request.path;
            MockResponse response = responses.get(key);
            if (response == null) {
                response = responses.get("*");
            }
            
            if (response == null) {
                response = new MockResponse();
                response.status = 404;
                response.body = "Not Found";
                response.headers = new HashMap<>();
            }
            
            return response;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Request interrupted", e);
        } finally {
            long responseTime = System.currentTimeMillis() - startTime;
            totalResponseTime.addAndGet(responseTime);
        }
    }
    
    @Override
    public void reset() {
        responses.clear();
        capturedRequests.clear();
        callCount.set(0);
        errorCount.set(0);
        totalResponseTime.set(0);
        error = null;
        delay = 0;
    }
    
    @Override
    public long getCallCount() {
        return callCount.get();
    }
    
    @Override
    public long getErrorCount() {
        return errorCount.get();
    }
    
    @Override
    public double getAverageResponseTime() {
        long count = callCount.get();
        return count > 0 ? (double) totalResponseTime.get() / count : 0;
    }
    
    @Override
    public void verify() {
        // Verification logic can be added here
    }
    
    /**
     * HTTP Request representation
     */
    public static class HttpRequest {
        private String method;
        private String path;
        private Map<String, String> headers;
        private Map<String, String> queryParams;
        private String body;
        
        // Getters and setters
        public String getMethod() {
            return method;
        }
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
        
        public Map<String, String> getQueryParams() {
            return queryParams;
        }
        
        public void setQueryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
    }
    
    /**
     * Mock response
     */
    private static class MockResponse {
        private int status;
        private String body;
        private Map<String, String> headers;
    }
}