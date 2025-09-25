package com.integrixs.testing.adapters;

import java.util.Map;

/**
 * Mock message queue adapter for testing
 */
public class MockMessageQueueAdapter extends MockAdapter {
    
    private String queueName;
    private String exchangeName;
    
    public MockMessageQueueAdapter(String adapterId) {
        super(adapterId, "MESSAGE_QUEUE");
    }
    
    @Override
    public void sendMessage(String message, Map<String, Object> headers) throws Exception {
        recordMessage(message, headers, "OUTBOUND");
    }
    
    @Override
    public String receiveMessage() throws Exception {
        return getNextMessage();
    }
    
    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
    
    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }
    
    public String getQueueName() {
        return queueName;
    }
    
    public String getExchangeName() {
        return exchangeName;
    }
}