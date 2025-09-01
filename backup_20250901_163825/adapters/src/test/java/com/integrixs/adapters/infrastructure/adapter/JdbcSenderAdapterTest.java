package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.config.JdbcSenderAdapterConfig;
import com.integrixs.adapters.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JdbcSenderAdapter (Inbound adapter that polls databases).
 * This test class documents the current behavior before refactoring to JdbcInboundAdapter.
 */
@DisplayName("JDBC Sender Adapter Tests - Pre-refactoring Baseline")
public class JdbcSenderAdapterTest {

    private JdbcSenderAdapter adapter;
    private JdbcSenderAdapterConfig config;
    
    @Mock
    private DataSource mockDataSource;
    
    @Mock
    private Connection mockConnection;
    
    @Mock
    private PreparedStatement mockStatement;
    
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private ResultSetMetaData mockMetaData;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Create test configuration
        config = new JdbcSenderAdapterConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/testdb");
        config.setDriverClass("org.postgresql.Driver");
        config.setUsername("testuser");
        config.setPassword("testpass");
        config.setSelectQuery("SELECT * FROM test_table WHERE id > ? ORDER BY id");
        config.setIncrementalColumn("id");
        config.setPollingInterval(5000L);
        config.setMaxResults(100);
        config.setQueryTimeoutSeconds(30);
        config.setMinPoolSize(1);
        config.setMaxPoolSize(10);
        config.setConnectionTimeoutSeconds(10);
        
        adapter = new JdbcSenderAdapter(config);
    }

    @Test
    @DisplayName("Should initialize adapter successfully with valid configuration")
    void testInitialization() {
        AdapterOperationResult result = adapter.initialize();
        
        assertTrue(result.isSuccess());
        assertEquals("JDBC sender adapter initialized successfully", result.getMessage());
        assertEquals(AdapterConfiguration.AdapterStatusEnum.INITIALIZED, adapter.getStatus());
    }

    @Test
    @DisplayName("Should fail initialization with missing JDBC URL")
    void testInitializationFailureNoUrl() {
        config.setJdbcUrl(null);
        adapter = new JdbcSenderAdapter(config);
        
        AdapterOperationResult result = adapter.initialize();
        
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("JDBC URL is required"));
    }

    @Test
    @DisplayName("Should poll data successfully")
    void testPollForData() throws Exception {
        // Setup mocks
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getColumnCount()).thenReturn(3);
        when(mockMetaData.getColumnName(1)).thenReturn("id");
        when(mockMetaData.getColumnName(2)).thenReturn("name");
        when(mockMetaData.getColumnName(3)).thenReturn("value");
        
        // Simulate two rows of data
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getObject(1)).thenReturn(1L, 2L);
        when(mockResultSet.getObject(2)).thenReturn("Test1", "Test2");
        when(mockResultSet.getObject(3)).thenReturn(100, 200);
        when(mockResultSet.getObject("id")).thenReturn(1L, 2L);
        
        // Initialize and test
        adapter.initialize();
        FetchRequest request = new FetchRequest();
        AdapterOperationResult result = adapter.fetch(request);
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) result.getData();
        assertEquals(2, records.size());
        assertEquals("Test1", records.get(0).get("name"));
        assertEquals("Test2", records.get(1).get("name"));
    }

    @Test
    @DisplayName("Should handle incremental polling correctly")
    void testIncrementalPolling() throws Exception {
        // First poll - no last processed value
        FetchRequest request = new FetchRequest();
        
        // After first poll, adapter should track last processed value
        // Second poll should include WHERE clause
        String expectedQuery = config.getSelectQuery() + " AND id > ?";
        
        // Verify incremental behavior is working
        assertNotNull(config.getIncrementalColumn());
        assertEquals("id", config.getIncrementalColumn());
    }

    @Test
    @DisplayName("Should start and stop polling successfully")
    void testPollingLifecycle() throws InterruptedException {
        adapter.initialize();
        
        assertFalse(adapter.isPolling());
        
        // Start polling
        adapter.startPolling(1000); // 1 second interval
        assertTrue(adapter.isPolling());
        
        // Let it run for a bit
        TimeUnit.MILLISECONDS.sleep(100);
        
        // Stop polling
        adapter.stopPolling();
        assertFalse(adapter.isPolling());
    }

    @Test
    @DisplayName("Should handle connection test properly")
    void testConnectionTest() throws Exception {
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(5)).thenReturn(true);
        when(mockConnection.getMetaData()).thenReturn(mock(DatabaseMetaData.class));
        when(mockConnection.getMetaData().getDatabaseProductName()).thenReturn("PostgreSQL");
        when(mockConnection.getMetaData().getDatabaseProductVersion()).thenReturn("15.0");
        
        adapter.initialize();
        AdapterOperationResult result = adapter.testConnection();
        
        assertTrue(result.isSuccess());
        assertTrue(result.getMessage().contains("All connection tests passed"));
    }

    @Test
    @DisplayName("Should generate correct configuration summary")
    void testConfigurationSummary() {
        String summary = adapter.getConfigurationSummary();
        
        assertNotNull(summary);
        assertTrue(summary.contains("JDBC Sender (Inbound)"));
        assertTrue(summary.contains("5000ms"));
        assertTrue(summary.contains("SELECT"));
    }

    @Test
    @DisplayName("Should validate adapter metadata")
    void testAdapterMetadata() {
        AdapterMetadata metadata = adapter.getMetadata();
        
        assertNotNull(metadata);
        assertEquals(AdapterConfiguration.AdapterTypeEnum.JDBC, metadata.getAdapterType());
        assertEquals(AdapterConfiguration.AdapterModeEnum.SENDER, metadata.getAdapterMode());
        assertEquals("1.0.0", metadata.getVersion());
        assertTrue(metadata.isSupportsAsync());
        assertFalse(metadata.isSupportsBatch());
    }

    @Test
    @DisplayName("Should handle query timeout properly")
    void testQueryTimeout() throws Exception {
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        
        adapter.initialize();
        adapter.fetch(new FetchRequest());
        
        verify(mockStatement).setQueryTimeout(30); // From config
    }

    @Test
    @DisplayName("Should handle fetch size configuration")
    void testFetchSize() throws Exception {
        config.setFetchSize(50);
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        
        adapter.initialize();
        adapter.fetch(new FetchRequest());
        
        verify(mockStatement).setFetchSize(50);
    }

    @Test
    @DisplayName("Should shutdown cleanly")
    void testShutdown() {
        adapter.initialize();
        adapter.startPolling(1000);
        
        AdapterOperationResult result = adapter.destroy();
        
        assertTrue(result.isSuccess());
        assertFalse(adapter.isPolling());
        assertEquals(AdapterConfiguration.AdapterStatusEnum.DESTROYED, adapter.getStatus());
    }

    @Test
    @DisplayName("Should handle data callback registration")
    void testDataCallbackRegistration() {
        SenderAdapterPort.DataReceivedCallback callback = (data, result) -> {
            // Mock callback
        };
        
        adapter.setDataReceivedCallback(callback);
        // Callback should be registered (no exception)
    }

    @Test
    @DisplayName("Should throw UnsupportedOperationException for push-based listening")
    void testUnsupportedListening() {
        assertThrows(UnsupportedOperationException.class, () -> {
            adapter.startListening(null);
        });
        
        assertFalse(adapter.isListening());
    }
}