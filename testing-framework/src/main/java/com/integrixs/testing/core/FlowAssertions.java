package com.integrixs.testing.core;

import com.integrixs.testing.runners.FlowExecution;
import com.jayway.jsonpath.JsonPath;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xmlunit.assertj3.XmlAssert;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * Custom assertions for flow testing
 */
public class FlowAssertions {
    
    /**
     * Start asserting on a flow execution
     */
    public static FlowExecutionAssert assertThat(FlowExecution execution) {
        return new FlowExecutionAssert(execution);
    }
    
    /**
     * Assertions for flow execution
     */
    public static class FlowExecutionAssert extends AbstractAssert<FlowExecutionAssert, FlowExecution> {
        
        public FlowExecutionAssert(FlowExecution actual) {
            super(actual, FlowExecutionAssert.class);
        }
        
        /**
         * Assert execution was successful
         */
        public FlowExecutionAssert isSuccessful() {
            isNotNull();
            if (!actual.isSuccessful()) {
                failWithMessage("Expected successful execution but failed with: %s", actual.getError());
            }
            return this;
        }
        
        /**
         * Assert execution failed
         */
        public FlowExecutionAssert hasFailed() {
            isNotNull();
            if (actual.isSuccessful()) {
                failWithMessage("Expected failed execution but was successful");
            }
            return this;
        }
        
        /**
         * Assert execution failed with specific error
         */
        public FlowExecutionAssert hasFailedWith(String expectedError) {
            hasFailed();
            Assertions.assertThat(actual.getError())
                .contains(expectedError);
            return this;
        }
        
        /**
         * Assert execution failed with exception type
         */
        public FlowExecutionAssert hasFailedWithException(Class<? extends Exception> exceptionType) {
            hasFailed();
            Assertions.assertThat(actual.getException())
                .isInstanceOf(exceptionType);
            return this;
        }
        
        /**
         * Assert execution time
         */
        public FlowExecutionAssert hasExecutionTimeLessThan(Duration duration) {
            isNotNull();
            Assertions.assertThat(actual.getExecutionTime())
                .isLessThan(duration.toMillis());
            return this;
        }
        
        /**
         * Assert output exists
         */
        public FlowExecutionAssert hasOutput() {
            isNotNull();
            isSuccessful();
            if (actual.getOutput() == null) {
                failWithMessage("Expected output but was null");
            }
            return this;
        }
        
        /**
         * Assert output value
         */
        public FlowExecutionAssert hasOutput(Object expectedOutput) {
            hasOutput();
            Assertions.assertThat(actual.getOutput())
                .isEqualTo(expectedOutput);
            return this;
        }
        
        /**
         * Assert output matches predicate
         */
        public FlowExecutionAssert hasOutputMatching(Consumer<Object> assertion) {
            hasOutput();
            assertion.accept(actual.getOutput());
            return this;
        }
        
        /**
         * Assert JSON output
         */
        public JsonOutputAssert hasJsonOutput() {
            hasOutput();
            return new JsonOutputAssert(actual.getOutputAsString());
        }
        
        /**
         * Assert XML output
         */
        public XmlOutputAssert hasXmlOutput() {
            hasOutput();
            return new XmlOutputAssert(actual.getOutputAsString());
        }
        
        /**
         * Assert header exists
         */
        public FlowExecutionAssert hasHeader(String key) {
            isNotNull();
            Map<String, String> headers = actual.getHeaders();
            if (headers == null || !headers.containsKey(key)) {
                failWithMessage("Expected header '%s' but was not found", key);
            }
            return this;
        }
        
        /**
         * Assert header value
         */
        public FlowExecutionAssert hasHeader(String key, String value) {
            hasHeader(key);
            Assertions.assertThat(actual.getHeaders().get(key))
                .isEqualTo(value);
            return this;
        }
        
        /**
         * Assert execution state
         */
        public FlowExecutionAssert hasState(String expectedState) {
            isNotNull();
            Assertions.assertThat(actual.getState())
                .isEqualTo(expectedState);
            return this;
        }
        
        /**
         * Assert step was executed
         */
        public FlowExecutionAssert hasExecutedStep(String stepName) {
            isNotNull();
            if (!actual.getExecutedSteps().contains(stepName)) {
                failWithMessage("Expected step '%s' to be executed but was not found in: %s",
                    stepName, actual.getExecutedSteps());
            }
            return this;
        }
        
        /**
         * Assert number of steps executed
         */
        public FlowExecutionAssert hasExecutedStepsCount(int count) {
            isNotNull();
            Assertions.assertThat(actual.getExecutedSteps())
                .hasSize(count);
            return this;
        }
    }
    
    /**
     * JSON output assertions
     */
    public static class JsonOutputAssert {
        private final String json;
        
        public JsonOutputAssert(String json) {
            this.json = json;
        }
        
        /**
         * Assert JSON path exists
         */
        public JsonOutputAssert hasPath(String jsonPath) {
            Object value = JsonPath.read(json, jsonPath);
            Assertions.assertThat(value).isNotNull();
            return this;
        }
        
        /**
         * Assert JSON path value
         */
        public JsonOutputAssert hasPath(String jsonPath, Object expectedValue) {
            Object value = JsonPath.read(json, jsonPath);
            Assertions.assertThat(value).isEqualTo(expectedValue);
            return this;
        }
        
        /**
         * Assert JSON path matches pattern
         */
        public JsonOutputAssert hasPathMatching(String jsonPath, String pattern) {
            String value = JsonPath.read(json, jsonPath);
            Assertions.assertThat(value).matches(pattern);
            return this;
        }
        
        /**
         * Get JSON path for further assertions
         */
        public <T> T extractPath(String jsonPath, Class<T> type) {
            return JsonPath.read(json, jsonPath);
        }
    }
    
    /**
     * XML output assertions
     */
    public static class XmlOutputAssert {
        private final String xml;
        
        public XmlOutputAssert(String xml) {
            this.xml = xml;
        }
        
        /**
         * Assert XML is valid
         */
        public XmlOutputAssert isValid() {
            XmlAssert.assertThat(xml).isValid();
            return this;
        }
        
        /**
         * Assert XPath exists
         */
        public XmlOutputAssert hasXPath(String xpath) {
            XmlAssert.assertThat(xml).hasXPath(xpath);
            return this;
        }
        
        /**
         * Assert XPath value
         */
        public XmlOutputAssert hasXPath(String xpath, String value) {
            XmlAssert.assertThat(xml)
                .hasXPath(xpath)
                .withValue(value);
            return this;
        }
        
        /**
         * Assert XML is equivalent to
         */
        public XmlOutputAssert isEquivalentTo(String expectedXml) {
            XmlAssert.assertThat(xml)
                .and(expectedXml)
                .areSimilar();
            return this;
        }
    }
    
    /**
     * Performance assertions
     */
    public static class PerformanceAssert {
        
        public static void assertPerformance(Runnable test, Duration maxDuration) {
            long start = System.currentTimeMillis();
            test.run();
            long elapsed = System.currentTimeMillis() - start;
            
            Assertions.assertThat(elapsed)
                .describedAs("Execution time")
                .isLessThan(maxDuration.toMillis());
        }
        
        public static void assertThroughput(Runnable test, int iterations, int minThroughput) {
            long start = System.currentTimeMillis();
            for (int i = 0; i < iterations; i++) {
                test.run();
            }
            long elapsed = System.currentTimeMillis() - start;
            
            double throughput = (iterations * 1000.0) / elapsed;
            Assertions.assertThat(throughput)
                .describedAs("Throughput (ops/sec)")
                .isGreaterThan(minThroughput);
        }
    }
    
    /**
     * Concurrency assertions
     */
    public static class ConcurrencyAssert {
        
        public static void assertThreadSafe(Runnable test, int threads) throws InterruptedException {
            Thread[] threadArray = new Thread[threads];
            final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
            final java.util.concurrent.atomic.AtomicBoolean failed = new java.util.concurrent.atomic.AtomicBoolean(false);
            
            for (int i = 0; i < threads; i++) {
                threadArray[i] = new Thread(() -> {
                    try {
                        latch.await();
                        test.run();
                    } catch (Exception e) {
                        failed.set(true);
                        throw new RuntimeException(e);
                    }
                });
                threadArray[i].start();
            }
            
            latch.countDown();
            
            for (Thread thread : threadArray) {
                thread.join();
            }
            
            Assertions.assertThat(failed.get())
                .describedAs("Thread safety")
                .isFalse();
        }
    }
}