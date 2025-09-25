package com.integrixs.testing.adapters;

import java.util.Map;

/**
 * Mock database adapter for testing
 */
public class MockDatabaseAdapter extends MockAdapter {
    
    private String jdbcUrl;
    private String query;
    
    public MockDatabaseAdapter(String adapterId) {
        super(adapterId, "DATABASE");
    }
    
    @Override
    public void sendMessage(String message, Map<String, Object> headers) throws Exception {
        recordMessage(message, headers, "OUTBOUND");
    }
    
    @Override
    public String receiveMessage() throws Exception {
        return getNextMessage();
    }
    
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getJdbcUrl() {
        return jdbcUrl;
    }
    
    public String getQuery() {
        return query;
    }
}