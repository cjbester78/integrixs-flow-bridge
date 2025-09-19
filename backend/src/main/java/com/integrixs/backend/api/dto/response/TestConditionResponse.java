package com.integrixs.backend.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class TestConditionResponse {

    private String id;
    private Instant timestamp;
    private String condition;
    private String conditionType;
    private boolean result;
    private Long executionTimeMs;
    private String error;
    private TestDetails details;

    public static class TestDetails {
        private String evaluatedExpression;
        private Map<String, Object> variables;
        private List<ExecutionStep> steps;

        // Default constructor
        public TestDetails() {
        }

        // Builder
        public static TestDetailsBuilder builder() {
            return new TestDetailsBuilder();
        }

        public static class TestDetailsBuilder {
            private TestDetails details = new TestDetails();

            public TestDetailsBuilder evaluatedExpression(String evaluatedExpression) {
                details.evaluatedExpression = evaluatedExpression;
                return this;
            }

            public TestDetailsBuilder variables(Map<String, Object> variables) {
                details.variables = variables;
                return this;
            }

            public TestDetailsBuilder steps(List<ExecutionStep> steps) {
                details.steps = steps;
                return this;
            }

            public TestDetails build() {
                return details;
            }
        }

        public String getEvaluatedExpression() {
            return evaluatedExpression;
        }

        public void setEvaluatedExpression(String evaluatedExpression) {
            this.evaluatedExpression = evaluatedExpression;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

        public List<ExecutionStep> getSteps() {
            return steps;
        }

        public void setSteps(List<ExecutionStep> steps) {
            this.steps = steps;
        }
    }

    public static class ExecutionStep {
        private String description;
        private Object result;
        private Long durationMs;

        // Default constructor
        public ExecutionStep() {
        }

        // Builder
        public static ExecutionStepBuilder builder() {
            return new ExecutionStepBuilder();
        }

        public static class ExecutionStepBuilder {
            private ExecutionStep step = new ExecutionStep();

            public ExecutionStepBuilder description(String description) {
                step.description = description;
                return this;
            }

            public ExecutionStepBuilder result(Object result) {
                step.result = result;
                return this;
            }

            public ExecutionStepBuilder durationMs(Long durationMs) {
                step.durationMs = durationMs;
                return this;
            }

            public ExecutionStep build() {
                return step;
            }
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Object getResult() {
            return result;
        }

        public void setResult(Object result) {
            this.result = result;
        }

        public Long getDurationMs() {
            return durationMs;
        }

        public void setDurationMs(Long durationMs) {
            this.durationMs = durationMs;
        }
    }

    // Default constructor
    public TestConditionResponse() {
    }

    // Builder
    public static TestConditionResponseBuilder builder() {
        return new TestConditionResponseBuilder();
    }

    public static class TestConditionResponseBuilder {
        private TestConditionResponse response = new TestConditionResponse();

        public TestConditionResponseBuilder id(String id) {
            response.id = id;
            return this;
        }

        public TestConditionResponseBuilder timestamp(Instant timestamp) {
            response.timestamp = timestamp;
            return this;
        }

        public TestConditionResponseBuilder condition(String condition) {
            response.condition = condition;
            return this;
        }

        public TestConditionResponseBuilder conditionType(String conditionType) {
            response.conditionType = conditionType;
            return this;
        }

        public TestConditionResponseBuilder result(boolean result) {
            response.result = result;
            return this;
        }

        public TestConditionResponseBuilder executionTimeMs(Long executionTimeMs) {
            response.executionTimeMs = executionTimeMs;
            return this;
        }

        public TestConditionResponseBuilder error(String error) {
            response.error = error;
            return this;
        }

        public TestConditionResponseBuilder details(TestDetails details) {
            response.details = details;
            return this;
        }

        public TestConditionResponse build() {
            return response;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public TestDetails getDetails() {
        return details;
    }

    public void setDetails(TestDetails details) {
        this.details = details;
    }
}
