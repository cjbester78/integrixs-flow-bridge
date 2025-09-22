package com.integrixs.backend.saga;

/**
 * Result of a saga step execution.
 *
 * @param <T> the type of result data
 * @author Integration Team
 * @since 1.0.0
 */
public class StepResult<T> {

    private String stepName;
    private boolean success;
    private T data;
    private String errorMessage;
    private long executionTimeMs;

    /**
     * Creates a successful step result.
     *
     * @param stepName the step name
     * @param data the result data
     * @param <T> the data type
     * @return successful result
     */
    public static <T> StepResult<T> success(String stepName, T data) {
        return StepResult.<T>builder()
                .stepName(stepName)
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates a failed step result.
     *
     * @param stepName the step name
     * @param errorMessage the error message
     * @param <T> the data type
     * @return failed result
     */
    public static <T> StepResult<T> failure(String stepName, String errorMessage) {
        return StepResult.<T>builder()
                .stepName(stepName)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    // Getters and Setters
    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    // Builder pattern
    public static <T> StepResultBuilder<T> builder() {
        return new StepResultBuilder<T>();
    }

    public static class StepResultBuilder<T> {
        private String stepName;
        private boolean success;
        private T data;
        private String errorMessage;
        private long executionTimeMs;

        public StepResultBuilder<T> stepName(String stepName) {
            this.stepName = stepName;
            return this;
        }

        public StepResultBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public StepResultBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public StepResultBuilder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public StepResultBuilder<T> executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public StepResult<T> build() {
            StepResult<T> result = new StepResult<>();
            result.stepName = this.stepName;
            result.success = this.success;
            result.data = this.data;
            result.errorMessage = this.errorMessage;
            result.executionTimeMs = this.executionTimeMs;
            return result;
        }
    }
}
