package com.integrixs.testing.reports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.integrixs.testing.runners.FlowExecution;
import com.integrixs.testing.core.FlowTestContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generate test reports for flow executions
 */
public class TestReportGenerator {
    
    private final ExtentReports extent;
    private final ExtentSparkReporter sparkReporter;
    private final ObjectMapper objectMapper;
    private final Path reportDir;
    private final Map<String, TestSummary> testSummaries;
    
    public TestReportGenerator(String reportPath) {
        this.reportDir = Paths.get(reportPath);
        try {
            Files.createDirectories(reportDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create report directory", e);
        }
        
        this.sparkReporter = new ExtentSparkReporter(reportDir.resolve("index.html").toString());
        configureReporter();
        
        this.extent = new ExtentReports();
        this.extent.attachReporter(sparkReporter);
        this.extent.setSystemInfo("Framework", "Integrixs Testing Framework");
        this.extent.setSystemInfo("Version", "1.0.0");
        this.extent.setSystemInfo("Environment", System.getProperty("test.environment", "test"));
        
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        this.testSummaries = new HashMap<>();
    }
    
    /**
     * Configure the report settings
     */
    private void configureReporter() {
        sparkReporter.config().setTheme(Theme.STANDARD);
        sparkReporter.config().setDocumentTitle("Integrixs Flow Test Report");
        sparkReporter.config().setReportName("Flow Test Execution Results");
        sparkReporter.config().setTimeStampFormat("yyyy-MM-dd HH:mm:ss");
        sparkReporter.config().setEncoding("UTF-8");
        sparkReporter.config().setJs("""
            $(document).ready(function() {
                $('.test-detail').on('click', function() {
                    $(this).find('.flow-details').slideToggle();
                });
            });
            """);
        sparkReporter.config().setCss("""
            .flow-details {
                background-color: #f5f5f5;
                padding: 10px;
                border-radius: 5px;
                margin-top: 10px;
                font-family: monospace;
                font-size: 12px;
            }
            .metric-card {
                background: white;
                border: 1px solid #ddd;
                border-radius: 5px;
                padding: 15px;
                margin: 10px;
                display: inline-block;
            }
            .metric-value {
                font-size: 24px;
                font-weight: bold;
                color: #333;
            }
            .metric-label {
                font-size: 14px;
                color: #666;
            }
            """);
    }
    
    /**
     * Add test result
     */
    public void addTestResult(String testName, FlowExecution execution, FlowTestContext context) {
        ExtentTest test = extent.createTest(testName);
        
        // Add test metadata
        test.assignCategory(execution.getFlow().getName());
        test.info("Flow: " + execution.getFlow().getName());
        test.info("Execution ID: " + execution.getExecutionId());
        
        // Add execution details
        if (execution.isSuccessful()) {
            test.pass("Flow execution completed successfully");
            test.log(Status.PASS, "Execution time: " + execution.getExecutionTime() + "ms");
        } else {
            test.fail("Flow execution failed: " + execution.getError());
            if (execution.getException() != null) {
                test.fail(execution.getException());
            }
        }
        
        // Add step details
        test.info("Steps executed: " + execution.getExecutedSteps().size());
        for (String step : execution.getExecutedSteps()) {
            test.info(" - " + step);
        }
        
        // Add input/output
        if (execution.getInput() != null) {
            test.info("Input: " + formatJson(execution.getInput()));
        }
        if (execution.getOutput() != null) {
            test.info("Output: " + formatJson(execution.getOutput()));
        }
        
        // Add flow visualization
        String flowDiagram = generateFlowDiagram(execution);
        test.info("<div class='flow-details'>" + flowDiagram + "</div>");
        
        // Update summary
        updateTestSummary(testName, execution);
    }
    
    /**
     * Generate flow diagram
     */
    private String generateFlowDiagram(FlowExecution execution) {
        StringBuilder diagram = new StringBuilder();
        diagram.append("<pre>\n");
        diagram.append("Flow Execution Diagram:\n");
        diagram.append("======================\n\n");
        
        List<String> steps = execution.getExecutedSteps();
        for (int i = 0; i < steps.size(); i++) {
            String step = steps.get(i);
            diagram.append(String.format("[%s] %s\n", 
                execution.isSuccessful() ? "✓" : "✗", step));
            if (i < steps.size() - 1) {
                diagram.append("   |\n");
                diagram.append("   v\n");
            }
        }
        
        diagram.append("\n");
        diagram.append("Status: ").append(execution.getState()).append("\n");
        diagram.append("Duration: ").append(execution.getExecutionTime()).append("ms\n");
        diagram.append("</pre>");
        
        return diagram.toString();
    }
    
    /**
     * Format JSON for display
     */
    private String formatJson(Object obj) {
        try {
            return "<pre>" + objectMapper.writeValueAsString(obj) + "</pre>";
        } catch (Exception e) {
            return obj.toString();
        }
    }
    
    /**
     * Update test summary
     */
    private void updateTestSummary(String testName, FlowExecution execution) {
        TestSummary summary = testSummaries.computeIfAbsent(testName, k -> new TestSummary());
        summary.totalExecutions++;
        if (execution.isSuccessful()) {
            summary.successfulExecutions++;
        } else {
            summary.failedExecutions++;
        }
        summary.totalExecutionTime += execution.getExecutionTime();
        summary.executionTimes.add(execution.getExecutionTime());
    }
    
    /**
     * Generate report
     */
    public void generateReport() {
        // Add summary dashboard
        addSummaryDashboard();
        
        // Generate additional reports
        generatePerformanceReport();
        generateMetricsReport();
        generateExecutionTimeline();
        
        // Flush report
        extent.flush();
        
        // Generate JSON summary
        generateJsonSummary();
    }
    
    /**
     * Add summary dashboard
     */
    private void addSummaryDashboard() {
        ExtentTest dashboard = extent.createTest("Test Execution Dashboard");
        
        int totalTests = testSummaries.size();
        int totalExecutions = testSummaries.values().stream()
            .mapToInt(s -> s.totalExecutions).sum();
        int successfulExecutions = testSummaries.values().stream()
            .mapToInt(s -> s.successfulExecutions).sum();
        int failedExecutions = testSummaries.values().stream()
            .mapToInt(s -> s.failedExecutions).sum();
        
        double successRate = totalExecutions > 0 ? 
            (double) successfulExecutions / totalExecutions * 100 : 0;
        
        String dashboardHtml = String.format("""
            <div class="dashboard">
                <div class="metric-card">
                    <div class="metric-value">%d</div>
                    <div class="metric-label">Total Tests</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value">%d</div>
                    <div class="metric-label">Total Executions</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value" style="color: green;">%d</div>
                    <div class="metric-label">Successful</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value" style="color: red;">%d</div>
                    <div class="metric-label">Failed</div>
                </div>
                <div class="metric-card">
                    <div class="metric-value">%.1f%%</div>
                    <div class="metric-label">Success Rate</div>
                </div>
            </div>
            """, totalTests, totalExecutions, successfulExecutions, failedExecutions, successRate);
        
        dashboard.info(dashboardHtml);
    }
    
    /**
     * Generate performance report
     */
    private void generatePerformanceReport() {
        ExtentTest perfReport = extent.createTest("Performance Report");
        
        testSummaries.forEach((testName, summary) -> {
            if (!summary.executionTimes.isEmpty()) {
                double avgTime = summary.totalExecutionTime / summary.totalExecutions;
                double minTime = summary.executionTimes.stream()
                    .min(Long::compare).orElse(0L);
                double maxTime = summary.executionTimes.stream()
                    .max(Long::compare).orElse(0L);
                
                perfReport.info(String.format(
                    "Test: %s - Avg: %.2fms, Min: %.0fms, Max: %.0fms",
                    testName, avgTime, minTime, maxTime
               ));
            }
        });
    }
    
    /**
     * Generate metrics report
     */
    private void generateMetricsReport() {
        Path metricsFile = reportDir.resolve("metrics.json");
        
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("testSummaries", testSummaries);
        
        try {
            objectMapper.writeValue(metricsFile.toFile(), metrics);
        } catch (IOException e) {
            // Log error
        }
    }
    
    /**
     * Generate execution timeline
     */
    private void generateExecutionTimeline() {
        // Implementation for timeline visualization
        // This would generate a timeline chart showing test executions over time
    }
    
    /**
     * Generate JSON summary
     */
    private void generateJsonSummary() {
        Path summaryFile = reportDir.resolve("summary.json");
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("reportGeneratedAt", LocalDateTime.now());
        summary.put("totalTests", testSummaries.size());
        summary.put("testResults", testSummaries);
        
        try {
            objectMapper.writeValue(summaryFile.toFile(), summary);
        } catch (IOException e) {
            // Log error
        }
    }
    
    /**
     * Test summary data
     */
    private static class TestSummary {
        int totalExecutions = 0;
        int successfulExecutions = 0;
        int failedExecutions = 0;
        long totalExecutionTime = 0;
        List<Long> executionTimes = new ArrayList<>();
    }
}