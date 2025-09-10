package com.integrixs.testing.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.testing.runners.FlowExecution;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Test context that holds the state for flow testing
 */
public class FlowTestContext {
    
    private String flowDefinition;
    private String environment;
    private boolean useMockAdapters;
    private String testDataDir;
    private int timeout;
    
    private IntegrationFlow flow;
    private Map<String, Object> testData;
    private List<FlowExecution> executions;
    private Map<String, Object> metrics;
    private Map<String, Object> contextVariables;
    
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    
    public FlowTestContext() {
        this.testData = new HashMap<>();
        this.executions = new ArrayList<>();
        this.metrics = new ConcurrentHashMap<>();
        this.contextVariables = new ConcurrentHashMap<>();
    }
    
    /**
     * Load flow definition from file
     */
    public void loadFlow() throws IOException {
        Path flowPath = Paths.get(flowDefinition);
        if (!Files.exists(flowPath)) {
            // Try as resource
            flowPath = Paths.get(getClass().getClassLoader()
                .getResource(flowDefinition).getPath());
        }
        
        String content = Files.readString(flowPath);
        
        if (flowDefinition.endsWith(".yaml") || flowDefinition.endsWith(".yml")) {
            flow = yamlMapper.readValue(content, IntegrationFlow.class);
        } else if (flowDefinition.endsWith(".json")) {
            flow = jsonMapper.readValue(content, IntegrationFlow.class);
        } else {
            throw new IllegalArgumentException("Unsupported flow file format: " + flowDefinition);
        }
    }
    
    /**
     * Load test data from file
     */
    public void loadTestData(String testDataFile) throws IOException {
        Path dataPath = Paths.get(testDataDir, testDataFile);
        if (!Files.exists(dataPath)) {
            // Try as resource
            dataPath = Paths.get(getClass().getClassLoader()
                .getResource(testDataDir + "/" + testDataFile).getPath());
        }
        
        String content = Files.readString(dataPath);
        
        if (testDataFile.endsWith(".yaml") || testDataFile.endsWith(".yml")) {
            testData = yamlMapper.readValue(content, Map.class);
        } else if (testDataFile.endsWith(".json")) {
            testData = jsonMapper.readValue(content, Map.class);
        } else if (testDataFile.endsWith(".xml")) {
            // Handle XML test data
            testData = Map.of("xml", content);
        } else if (testDataFile.endsWith(".csv")) {
            // Handle CSV test data
            testData = Map.of("csv", content);
        } else {
            // Plain text
            testData = Map.of("content", content);
        }
    }
    
    /**
     * Reset context for next test
     */
    public void reset() {
        testData.clear();
        executions.clear();
        contextVariables.clear();
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        // Clean up any temporary files or resources
        executions.forEach(execution -> {
            if (execution != null) {
                execution.cleanup();
            }
        });
    }
    
    /**
     * Save metrics for the test
     */
    public void saveMetrics(String testName) {
        if (!executions.isEmpty()) {
            Map<String, Object> testMetrics = new HashMap<>();
            
            // Calculate average execution time
            double avgTime = executions.stream()
                .mapToLong(FlowExecution::getExecutionTime)
                .average()
                .orElse(0.0);
            
            // Calculate success rate
            long successCount = executions.stream()
                .filter(FlowExecution::isSuccessful)
                .count();
            double successRate = (double) successCount / executions.size();
            
            testMetrics.put("averageExecutionTime", avgTime);
            testMetrics.put("successRate", successRate);
            testMetrics.put("totalExecutions", executions.size());
            testMetrics.put("timestamp", new Date());
            
            metrics.put(testName, testMetrics);
        }
    }
    
    /**
     * Add execution result
     */
    public void addExecution(FlowExecution execution) {
        executions.add(execution);
    }
    
    /**
     * Get variable from context
     */
    public Object getVariable(String key) {
        return contextVariables.get(key);
    }
    
    /**
     * Set variable in context
     */
    public void setVariable(String key, Object value) {
        contextVariables.put(key, value);
    }
    
    /**
     * Get test data value
     */
    public Object getTestDataValue(String path) {
        if (path.contains(".")) {
            // Navigate nested structure
            String[] parts = path.split("\\.");
            Object current = testData;
            
            for (String part : parts) {
                if (current instanceof Map) {
                    current = ((Map<?, ?>) current).get(part);
                } else if (current instanceof List && part.matches("\\d+")) {
                    int index = Integer.parseInt(part);
                    current = ((List<?>) current).get(index);
                } else {
                    return null;
                }
            }
            
            return current;
        }
        
        return testData.get(path);
    }
    
    // Getters and setters
    public String getFlowDefinition() {
        return flowDefinition;
    }
    
    public void setFlowDefinition(String flowDefinition) {
        this.flowDefinition = flowDefinition;
    }
    
    public String getEnvironment() {
        return environment;
    }
    
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    
    public boolean isUseMockAdapters() {
        return useMockAdapters;
    }
    
    public void setUseMockAdapters(boolean useMockAdapters) {
        this.useMockAdapters = useMockAdapters;
    }
    
    public String getTestDataDir() {
        return testDataDir;
    }
    
    public void setTestDataDir(String testDataDir) {
        this.testDataDir = testDataDir;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public IntegrationFlow getFlow() {
        return flow;
    }
    
    public Map<String, Object> getTestData() {
        return testData;
    }
    
    public List<FlowExecution> getExecutions() {
        return executions;
    }
    
    public Map<String, Object> getMetrics() {
        return metrics;
    }
}