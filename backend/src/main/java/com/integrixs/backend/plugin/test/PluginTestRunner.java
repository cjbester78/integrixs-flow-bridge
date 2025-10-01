package com.integrixs.backend.plugin.test;

import com.integrixs.backend.plugin.api.*;
import com.integrixs.backend.plugin.api.AdapterPlugin.Direction;
import java.util.*;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Automated test runner for plugins
 */
public class PluginTestRunner {

    private static final Logger log = LoggerFactory.getLogger(PluginTestRunner.class);


    private final PluginTestHarness harness;
    private final List<TestResult> results = new ArrayList<>();

    public PluginTestRunner(Class<? extends AdapterPlugin> pluginClass) {
        this.harness = new PluginTestHarness(pluginClass);
    }

    /**
     * Run all tests
     */
    public TestReport runAllTests(Map<String, Object> configuration) {
        log.info("Starting plugin test suite");

        long startTime = System.currentTimeMillis();

        // Metadata tests
        runTest("Metadata Validation", this::testMetadata);
        runTest("Configuration Schema", this::testConfigurationSchema);

        // Configuration tests
        harness.configure(configuration);
        runTest("Plugin Initialization", () -> testInitialization(configuration));
        runTest("Connection Test - Outbound", () -> testConnection(Direction.OUTBOUND));
        runTest("Connection Test - Inbound", () -> testConnection(Direction.INBOUND));
        runTest("Health Check", this::testHealthCheck);

        // Message handling tests
        runTest("Send Single Message", this::testSendMessage);
        runTest("Send Batch Messages", this::testSendBatch);
        runTest("Receive Messages", this::testReceiveMessages);

        // Cleanup
        harness.destroy();

        long duration = System.currentTimeMillis() - startTime;

        return TestReport.builder()
                .results(new ArrayList<>(results))
                .totalTests(results.size())
                .passedTests((int) results.stream().filter(TestResult::isPassed).count())
                .failedTests((int) results.stream().filter(r -> !r.isPassed()).count())
                .duration(duration)
                .build();
    }

    /**
     * Run a specific test
     */
    public TestResult runTest(String testName, TestCase testCase) {
        log.info("Running test: {}", testName);

        long startTime = System.currentTimeMillis();
        TestResult result;

        try {
            testCase.execute();
            result = TestResult.builder()
                    .testName(testName)
                    .passed(true)
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
            log.info("Test passed: {}", testName);
        } catch(Exception e) {
            result = TestResult.builder()
                    .testName(testName)
                    .passed(false)
                    .error(e.getMessage())
                    .stackTrace(getStackTrace(e))
                    .duration(System.currentTimeMillis() - startTime)
                    .build();
            log.error("Test failed: {}", testName, e);
        }

        results.add(result);
        return result;
    }

    private void testMetadata() {
        AdapterMetadata metadata = harness.getMetadata();

        assertNotNull(metadata, "Metadata is null");
        assertNotNull(metadata.getId(), "Plugin ID is null");
        assertNotNull(metadata.getName(), "Plugin name is null");
        assertNotNull(metadata.getVersion(), "Plugin version is null");
        assertNotNull(metadata.getVendor(), "Plugin vendor is null");
        assertNotNull(metadata.getCategory(), "Plugin category is null");

        // Validate version format
        assertTrue(metadata.getVersion().matches("\\d+\\.\\d+\\.\\d+.*"),
            "Invalid version format: " + metadata.getVersion());
    }

    private void testConfigurationSchema() {
        ConfigurationSchema schema = harness.getConfigurationSchema();

        assertNotNull(schema, "Configuration schema is null");
        assertNotNull(schema.getSections(), "Schema sections is null");
        assertFalse(schema.getSections().isEmpty(), "Schema has no sections");

        // Validate sections
        for(ConfigurationSchema.Section section : schema.getSections()) {
            assertNotNull(section.getId(), "Section ID is null");
            assertNotNull(section.getTitle(), "Section title is null");
            assertNotNull(section.getFields(), "Section fields is null");

            // Validate fields
            for(ConfigurationSchema.Field field : section.getFields()) {
                assertNotNull(field.getName(), "Field name is null");
                assertNotNull(field.getType(), "Field type is null");
                assertNotNull(field.getLabel(), "Field label is null");
            }
        }
    }

    private void testInitialization(Map<String, Object> configuration) {
        // Test is successful if no exception is thrown during configure
        assertNotNull(configuration, "Configuration is null");
    }

    private void testConnection(Direction direction) {
        ConnectionTestResult result = harness.testConnection(direction);

        assertNotNull(result, "Connection test result is null");
        assertTrue(result.isSuccessful(),
            "Connection test failed: " + result.getErrorDetails());

        if(result.getResponseTime() != null) {
            assertTrue(result.getResponseTime().toMillis() < 30000,
                "Connection took too long: " + result.getResponseTime().toMillis() + "ms");
        }
    }

    private void testHealthCheck() {
        HealthStatus health = harness.checkHealth();

        assertNotNull(health, "Health status is null");
        assertNotNull(health.getState(), "Health state is null");
        assertEquals(HealthStatus.HealthState.HEALTHY, health.getState(),
            "Plugin is not healthy: " + health.getMessage());
    }

    private void testSendMessage() {
        PluginMessage message = PluginTestHarness.createTestMessage(
            "test-1",
            Map.of("test", "data", "value", 123)
       );

        SendResult result = harness.send(message);

        assertNotNull(result, "Send result is null");
        assertTrue(result.isSuccessful(),
            "Failed to send message: " + result.getError());
        assertEquals(message.getId(), result.getMessageId(),
            "Message ID mismatch");
    }

    private void testSendBatch() {
        List<PluginMessage> messages = Arrays.asList(
            PluginTestHarness.createTestMessage("batch-1", Map.of("index", 1)),
            PluginTestHarness.createTestMessage("batch-2", Map.of("index", 2)),
            PluginTestHarness.createTestMessage("batch-3", Map.of("index", 3))
       );

        BatchSendResult result = harness.sendBatch(messages);

        assertNotNull(result, "Batch send result is null");
        assertEquals(messages.size(), result.getTotalMessages(),
            "Total message count mismatch");
        assertTrue(result.getSuccessCount() > 0,
            "No messages were sent successfully");
    }

    private void testReceiveMessages() {
        harness.startListening();

        try {
            // Wait a bit for messages
            List<PluginMessage> messages = harness.waitForMessages(1, 10, TimeUnit.SECONDS);

            assertFalse(messages.isEmpty(),
                "No messages received within timeout");

            PluginMessage message = messages.get(0);
            assertNotNull(message.getBody(), "Message body is null");
            assertNotNull(message.getContentType(), "Content type is null");

        } finally {
            harness.stopListening();
        }
    }

    private void assertNotNull(Object obj, String message) {
        if(obj == null) {
            throw new AssertionError(message);
        }
    }

    private void assertTrue(boolean condition, String message) {
        if(!condition) {
            throw new AssertionError(message);
        }
    }

    private void assertFalse(boolean condition, String message) {
        if(condition) {
            throw new AssertionError(message);
        }
    }

    private void assertEquals(Object expected, Object actual, String message) {
        if(!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " - Expected: " + expected + ", Actual: " + actual);
        }
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element).append("\n");
        }
        return sb.toString();
    }

    /**
     * Test case interface
     */
    @FunctionalInterface
    public interface TestCase {
        void execute() throws Exception;
    }

    /**
     * Test result
     */
    public static class TestResult {
        private String testName;
        private boolean passed;
        private String error;
        private String stackTrace;
        private long duration;

        // Default constructor
        public TestResult() {
        }

        // Getters and setters
        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getStackTrace() {
            return stackTrace;
        }

        public void setStackTrace(String stackTrace) {
            this.stackTrace = stackTrace;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        // Builder
        public static TestResultBuilder builder() {
            return new TestResultBuilder();
        }

        public static class TestResultBuilder {
            private String testName;
            private boolean passed;
            private String error;
            private String stackTrace;
            private long duration;

            public TestResultBuilder testName(String testName) {
                this.testName = testName;
                return this;
            }

            public TestResultBuilder passed(boolean passed) {
                this.passed = passed;
                return this;
            }

            public TestResultBuilder error(String error) {
                this.error = error;
                return this;
            }

            public TestResultBuilder stackTrace(String stackTrace) {
                this.stackTrace = stackTrace;
                return this;
            }

            public TestResultBuilder duration(long duration) {
                this.duration = duration;
                return this;
            }

            public TestResult build() {
                TestResult result = new TestResult();
                result.testName = this.testName;
                result.passed = this.passed;
                result.error = this.error;
                result.stackTrace = this.stackTrace;
                result.duration = this.duration;
                return result;
            }
        }
    }

    /**
     * Test report
     */
    public static class TestReport {
        private List<TestResult> results;
        private int totalTests;
        private int passedTests;
        private int failedTests;
        private long duration;

        // Default constructor
        public TestReport() {
        }

        // Getters and setters
        public List<TestResult> getResults() {
            return results;
        }

        public void setResults(List<TestResult> results) {
            this.results = results;
        }

        public int getTotalTests() {
            return totalTests;
        }

        public void setTotalTests(int totalTests) {
            this.totalTests = totalTests;
        }

        public int getPassedTests() {
            return passedTests;
        }

        public void setPassedTests(int passedTests) {
            this.passedTests = passedTests;
        }

        public int getFailedTests() {
            return failedTests;
        }

        public void setFailedTests(int failedTests) {
            this.failedTests = failedTests;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public double getSuccessRate() {
            return totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        }

        public void printSummary() {
            log.info("=== Plugin Test Report ===");
            log.info("Total Tests: {}", totalTests);
            log.info("Passed: {}", passedTests);
            log.info("Failed: {}", failedTests);
            log.info("Success Rate: {}%", String.format("%.1f", getSuccessRate()));
            log.info("Duration: {}ms", duration);

            if(failedTests > 0) {
                log.warn("Failed Tests:");
                results.stream()
                        .filter(r -> !r.isPassed())
                        .forEach(r -> {
                            log.warn(" - {}: {}", r.getTestName(), r.getError());
                        });
            }
        }

        // Builder
        public static TestReportBuilder builder() {
            return new TestReportBuilder();
        }

        public static class TestReportBuilder {
            private List<TestResult> results;
            private int totalTests;
            private int passedTests;
            private int failedTests;
            private long duration;

            public TestReportBuilder results(List<TestResult> results) {
                this.results = results;
                return this;
            }

            public TestReportBuilder totalTests(int totalTests) {
                this.totalTests = totalTests;
                return this;
            }

            public TestReportBuilder passedTests(int passedTests) {
                this.passedTests = passedTests;
                return this;
            }

            public TestReportBuilder failedTests(int failedTests) {
                this.failedTests = failedTests;
                return this;
            }

            public TestReportBuilder duration(long duration) {
                this.duration = duration;
                return this;
            }

            public TestReport build() {
                TestReport report = new TestReport();
                report.results = this.results;
                report.totalTests = this.totalTests;
                report.passedTests = this.passedTests;
                report.failedTests = this.failedTests;
                report.duration = this.duration;
                return report;
            }
        }
    }
}
