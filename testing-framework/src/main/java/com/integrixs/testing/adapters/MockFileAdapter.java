package com.integrixs.testing.adapters;

import java.util.Map;

/**
 * Mock file adapter for testing
 */
public class MockFileAdapter extends MockAdapter {
    
    private String directory;
    private String pattern;
    
    public MockFileAdapter(String adapterId) {
        super(adapterId, "FILE");
    }
    
    @Override
    public void sendMessage(String message, Map<String, Object> headers) throws Exception {
        recordMessage(message, headers, "OUTBOUND");
    }
    
    @Override
    public String receiveMessage() throws Exception {
        return getNextMessage();
    }
    
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
    
    public String getDirectory() {
        return directory;
    }
    
    public String getPattern() {
        return pattern;
    }
}