package com.integrixs.testing.adapters;

import java.util.Map;

/**
 * Mock SOAP adapter for testing
 */
public class MockSoapAdapter extends MockAdapter {
    
    private String wsdlUrl;
    private String operation;
    
    public MockSoapAdapter(String adapterId) {
        super(adapterId, "SOAP");
    }
    
    @Override
    public void sendMessage(String message, Map<String, Object> headers) throws Exception {
        recordMessage(message, headers, "OUTBOUND");
    }
    
    @Override
    public String receiveMessage() throws Exception {
        return getNextMessage();
    }
    
    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public String getWsdlUrl() {
        return wsdlUrl;
    }
    
    public String getOperation() {
        return operation;
    }
}