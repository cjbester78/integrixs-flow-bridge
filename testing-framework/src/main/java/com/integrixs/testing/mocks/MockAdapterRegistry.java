package com.integrixs.testing.mocks;

import com.integrixs.testing.adapters.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing mock adapters
 */
public class MockAdapterRegistry {
    
    private static final MockAdapterRegistry INSTANCE = new MockAdapterRegistry();
    
    private final Map<String, MockAdapter> mockAdapters = new ConcurrentHashMap<>();
    private final Map<String, Object> defaultMocks = new ConcurrentHashMap<>();
    
    private MockAdapterRegistry() {
        // Private constructor for singleton
    }
    
    public static MockAdapterRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register a mock adapter
     */
    public void registerMockAdapter(String name, MockAdapter adapter) {
        mockAdapters.put(name, adapter);
    }
    
    /**
     * Get a mock adapter
     */
    public MockAdapter getMockAdapter(String name) {
        return mockAdapters.get(name);
    }
    
    /**
     * Get a typed mock adapter
     */
    @SuppressWarnings("unchecked")
    public <T extends MockAdapter> T getMockAdapter(String name, Class<T> type) {
        MockAdapter adapter = mockAdapters.get(name);
        if (adapter != null && type.isAssignableFrom(adapter.getClass())) {
            return (T) adapter;
        }
        return null;
    }
    
    /**
     * Register default mock implementations
     */
    public void registerDefaultMocks() {
        // HTTP Mock
        MockHttpAdapter httpMock = new MockHttpAdapter();
        httpMock.addResponse(200, "{\"status\":\"ok\"}");
        registerMockAdapter("http", httpMock);
        
        // File Mock
        MockFileAdapter fileMock = new MockFileAdapter();
        fileMock.addFile("/test/sample.txt", "Sample content");
        registerMockAdapter("file", fileMock);
        
        // Database Mock
        MockDatabaseAdapter dbMock = new MockDatabaseAdapter();
        dbMock.addQueryResult("SELECT * FROM users", new Object[][]{
           {"1", "John Doe", "john@example.com"},
           {"2", "Jane Smith", "jane@example.com"}
        });
        registerMockAdapter("database", dbMock);
        
        // Message Queue Mock
        MockMessageQueueAdapter mqMock = new MockMessageQueueAdapter();
        mqMock.createQueue("test-queue");
        mqMock.createTopic("test-topic");
        registerMockAdapter("messageQueue", mqMock);
        
        // SOAP Mock
        MockSoapAdapter soapMock = new MockSoapAdapter();
        soapMock.addOperationResponse("GetWeather", 
            "<weather><temperature>25</temperature><condition>Sunny</condition></weather>");
        registerMockAdapter("soap", soapMock);
        
        // FTP Mock
        MockFtpAdapter ftpMock = new MockFtpAdapter();
        ftpMock.addFile("/remote/test.txt", "Remote file content");
        ftpMock.addUser("testuser", "testpass");
        registerMockAdapter("ftp", ftpMock);
    }
    
    /**
     * Reset all mock adapters
     */
    public void reset() {
        mockAdapters.values().forEach(MockAdapter::reset);
    }
    
    /**
     * Clear all mock adapters
     */
    public void clear() {
        mockAdapters.clear();
    }
    
    /**
     * Create a scoped registry for a test
     */
    public ScopedMockRegistry createScope(String testName) {
        return new ScopedMockRegistry(testName, this);
    }
    
    /**
     * Scoped mock registry for test isolation
     */
    public static class ScopedMockRegistry {
        private final String scope;
        private final MockAdapterRegistry parent;
        private final Map<String, MockAdapter> scopedMocks = new ConcurrentHashMap<>();
        
        public ScopedMockRegistry(String scope, MockAdapterRegistry parent) {
            this.scope = scope;
            this.parent = parent;
        }
        
        public void registerMockAdapter(String name, MockAdapter adapter) {
            String scopedName = scope + "." + name;
            scopedMocks.put(name, adapter);
            parent.registerMockAdapter(scopedName, adapter);
        }
        
        public MockAdapter getMockAdapter(String name) {
            MockAdapter scoped = scopedMocks.get(name);
            if (scoped != null) {
                return scoped;
            }
            return parent.getMockAdapter(name);
        }
        
        public void cleanup() {
            scopedMocks.forEach((name, adapter) -> {
                adapter.reset();
                parent.mockAdapters.remove(scope + "." + name);
            });
            scopedMocks.clear();
        }
    }
    
    /**
     * Mock adapter statistics
     */
    public Map<String, MockAdapterStats> getStatistics() {
        Map<String, MockAdapterStats> stats = new ConcurrentHashMap<>();
        
        mockAdapters.forEach((name, adapter) -> {
            MockAdapterStats adapterStats = new MockAdapterStats();
            adapterStats.setName(name);
            adapterStats.setType(adapter.getClass().getSimpleName());
            adapterStats.setCallCount(adapter.getCallCount());
            adapterStats.setErrorCount(adapter.getErrorCount());
            adapterStats.setAverageResponseTime(adapter.getAverageResponseTime());
            stats.put(name, adapterStats);
        });
        
        return stats;
    }
    
    public static class MockAdapterStats {
        private String name;
        private String type;
        private long callCount;
        private long errorCount;
        private double averageResponseTime;
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public long getCallCount() {
            return callCount;
        }
        
        public void setCallCount(long callCount) {
            this.callCount = callCount;
        }
        
        public long getErrorCount() {
            return errorCount;
        }
        
        public void setErrorCount(long errorCount) {
            this.errorCount = errorCount;
        }
        
        public double getAverageResponseTime() {
            return averageResponseTime;
        }
        
        public void setAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
        }
    }
}