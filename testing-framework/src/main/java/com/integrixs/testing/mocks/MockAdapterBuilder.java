package com.integrixs.testing.mocks;

import com.integrixs.testing.adapters.MockAdapter;
import com.integrixs.testing.adapters.MockHttpAdapter;
import com.integrixs.testing.adapters.MockFileAdapter;
import com.integrixs.testing.adapters.MockDatabaseAdapter;
import com.integrixs.testing.adapters.MockMessageQueueAdapter;
import com.integrixs.testing.adapters.MockSoapAdapter;
import com.integrixs.testing.adapters.MockFtpAdapter;

import java.util.function.Consumer;

/**
 * Fluent builder for creating mock adapters
 */
public class MockAdapterBuilder {
    
    /**
     * Create a new HTTP mock adapter
     */
    public HttpMockBuilder http() {
        return new HttpMockBuilder();
    }
    
    /**
     * Create a new File mock adapter
     */
    public FileMockBuilder file() {
        return new FileMockBuilder();
    }
    
    /**
     * Create a new Database mock adapter
     */
    public DatabaseMockBuilder database() {
        return new DatabaseMockBuilder();
    }
    
    /**
     * Create a new Message Queue mock adapter
     */
    public MessageQueueMockBuilder messageQueue() {
        return new MessageQueueMockBuilder();
    }
    
    /**
     * Create a new SOAP mock adapter
     */
    public SoapMockBuilder soap() {
        return new SoapMockBuilder();
    }
    
    /**
     * Create a new FTP mock adapter
     */
    public FtpMockBuilder ftp() {
        return new FtpMockBuilder();
    }
    
    /**
     * HTTP mock builder
     */
    public static class HttpMockBuilder {
        private final MockHttpAdapter adapter = new MockHttpAdapter();
        
        public HttpMockBuilder withResponse(int status, String body) {
            adapter.addResponse(status, body);
            return this;
        }
        
        public HttpMockBuilder withJsonResponse(int status, Object object) {
            adapter.addJsonResponse(status, object);
            return this;
        }
        
        public HttpMockBuilder withHeader(String key, String value) {
            adapter.addResponseHeader(key, value);
            return this;
        }
        
        public HttpMockBuilder withDelay(long milliseconds) {
            adapter.setDelay(milliseconds);
            return this;
        }
        
        public HttpMockBuilder withError(String error) {
            adapter.setError(error);
            return this;
        }
        
        public HttpMockBuilder captureRequests() {
            adapter.setCaptureRequests(true);
            return this;
        }
        
        public HttpMockBuilder onRequest(Consumer<MockHttpAdapter.HttpRequest> handler) {
            adapter.setRequestHandler(handler);
            return this;
        }
        
        public MockHttpAdapter build() {
            return adapter;
        }
    }
    
    /**
     * File mock builder
     */
    public static class FileMockBuilder {
        private final MockFileAdapter adapter = new MockFileAdapter();
        
        public FileMockBuilder withFile(String path, String content) {
            adapter.addFile(path, content);
            return this;
        }
        
        public FileMockBuilder withFile(String path, byte[] content) {
            adapter.addFile(path, content);
            return this;
        }
        
        public FileMockBuilder withDirectory(String path) {
            adapter.addDirectory(path);
            return this;
        }
        
        public FileMockBuilder captureWrites() {
            adapter.setCaptureWrites(true);
            return this;
        }
        
        public FileMockBuilder onRead(String path, Consumer<String> handler) {
            adapter.setReadHandler(path, handler);
            return this;
        }
        
        public FileMockBuilder onWrite(String path, Consumer<byte[]> handler) {
            adapter.setWriteHandler(path, handler);
            return this;
        }
        
        public FileMockBuilder withError(String path, String error) {
            adapter.setError(path, error);
            return this;
        }
        
        public MockFileAdapter build() {
            return adapter;
        }
    }
    
    /**
     * Database mock builder
     */
    public static class DatabaseMockBuilder {
        private final MockDatabaseAdapter adapter = new MockDatabaseAdapter();
        
        public DatabaseMockBuilder withQueryResult(String query, Object[][] rows) {
            adapter.addQueryResult(query, rows);
            return this;
        }
        
        public DatabaseMockBuilder withUpdateResult(String query, int rowsAffected) {
            adapter.addUpdateResult(query, rowsAffected);
            return this;
        }
        
        public DatabaseMockBuilder withStoredProcedureResult(String name, Object result) {
            adapter.addStoredProcedureResult(name, result);
            return this;
        }
        
        public DatabaseMockBuilder captureQueries() {
            adapter.setCaptureQueries(true);
            return this;
        }
        
        public DatabaseMockBuilder onQuery(Consumer<String> handler) {
            adapter.setQueryHandler(handler);
            return this;
        }
        
        public DatabaseMockBuilder withTransaction(Consumer<MockDatabaseAdapter.Transaction> handler) {
            adapter.setTransactionHandler(handler);
            return this;
        }
        
        public DatabaseMockBuilder withError(String error) {
            adapter.setError(error);
            return this;
        }
        
        public MockDatabaseAdapter build() {
            return adapter;
        }
    }
    
    /**
     * Message Queue mock builder
     */
    public static class MessageQueueMockBuilder {
        private final MockMessageQueueAdapter adapter = new MockMessageQueueAdapter();
        
        public MessageQueueMockBuilder withQueue(String name) {
            adapter.createQueue(name);
            return this;
        }
        
        public MessageQueueMockBuilder withTopic(String name) {
            adapter.createTopic(name);
            return this;
        }
        
        public MessageQueueMockBuilder withMessage(String destination, Object message) {
            adapter.addMessage(destination, message);
            return this;
        }
        
        public MessageQueueMockBuilder withMessages(String destination, Object... messages) {
            for (Object message : messages) {
                adapter.addMessage(destination, message);
            }
            return this;
        }
        
        public MessageQueueMockBuilder captureMessages() {
            adapter.setCaptureMessages(true);
            return this;
        }
        
        public MessageQueueMockBuilder onMessage(String destination, Consumer<Object> handler) {
            adapter.setMessageHandler(destination, handler);
            return this;
        }
        
        public MessageQueueMockBuilder withDelay(long milliseconds) {
            adapter.setDelay(milliseconds);
            return this;
        }
        
        public MessageQueueMockBuilder simulateBackpressure(int threshold) {
            adapter.setBackpressureThreshold(threshold);
            return this;
        }
        
        public MockMessageQueueAdapter build() {
            return adapter;
        }
    }
    
    /**
     * SOAP mock builder
     */
    public static class SoapMockBuilder {
        private final MockSoapAdapter adapter = new MockSoapAdapter();
        
        public SoapMockBuilder withWsdl(String wsdlPath) {
            adapter.setWsdl(wsdlPath);
            return this;
        }
        
        public SoapMockBuilder withOperation(String operation, String response) {
            adapter.addOperationResponse(operation, response);
            return this;
        }
        
        public SoapMockBuilder withFault(String operation, String faultCode, String faultString) {
            adapter.addOperationFault(operation, faultCode, faultString);
            return this;
        }
        
        public SoapMockBuilder validateRequests() {
            adapter.setValidateRequests(true);
            return this;
        }
        
        public SoapMockBuilder captureRequests() {
            adapter.setCaptureRequests(true);
            return this;
        }
        
        public SoapMockBuilder onRequest(String operation, Consumer<String> handler) {
            adapter.setRequestHandler(operation, handler);
            return this;
        }
        
        public MockSoapAdapter build() {
            return adapter;
        }
    }
    
    /**
     * FTP mock builder
     */
    public static class FtpMockBuilder {
        private final MockFtpAdapter adapter = new MockFtpAdapter();
        
        public FtpMockBuilder withFile(String path, String content) {
            adapter.addFile(path, content);
            return this;
        }
        
        public FtpMockBuilder withFile(String path, byte[] content) {
            adapter.addFile(path, content);
            return this;
        }
        
        public FtpMockBuilder withDirectory(String path) {
            adapter.addDirectory(path);
            return this;
        }
        
        public FtpMockBuilder withUser(String username, String password) {
            adapter.addUser(username, password);
            return this;
        }
        
        public FtpMockBuilder withPermissions(String path, String permissions) {
            adapter.setPermissions(path, permissions);
            return this;
        }
        
        public FtpMockBuilder captureTransfers() {
            adapter.setCaptureTransfers(true);
            return this;
        }
        
        public FtpMockBuilder onUpload(String path, Consumer<byte[]> handler) {
            adapter.setUploadHandler(path, handler);
            return this;
        }
        
        public FtpMockBuilder onDownload(String path, Consumer<String> handler) {
            adapter.setDownloadHandler(path, handler);
            return this;
        }
        
        public FtpMockBuilder simulateNetworkLatency(long milliseconds) {
            adapter.setNetworkLatency(milliseconds);
            return this;
        }
        
        public MockFtpAdapter build() {
            return adapter;
        }
    }
}