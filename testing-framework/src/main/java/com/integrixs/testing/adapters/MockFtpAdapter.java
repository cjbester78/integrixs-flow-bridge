package com.integrixs.testing.adapters;

import java.util.Map;

/**
 * Mock FTP adapter for testing
 */
public class MockFtpAdapter extends MockAdapter {
    
    private String host;
    private String directory;
    
    public MockFtpAdapter(String adapterId) {
        super(adapterId, "FTP");
    }
    
    @Override
    public void sendMessage(String message, Map<String, Object> headers) throws Exception {
        recordMessage(message, headers, "OUTBOUND");
    }
    
    @Override
    public String receiveMessage() throws Exception {
        return getNextMessage();
    }
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    public String getHost() {
        return host;
    }
    
    public String getDirectory() {
        return directory;
    }
}