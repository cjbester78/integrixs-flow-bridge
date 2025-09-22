package com.integrixs.backend.saga;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result of saga execution.
 *
 * @param <T> the type of result data
 * @author Integration Team
 * @since 1.0.0
 */
public class SagaResult<T> {

    private String sagaId;
    private boolean success;
    private T data;
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<StepResult<T>> stepResults = new ArrayList<>();

    /**
     * Creates a successful saga result.
     *
     * @param sagaId the saga ID
     * @param data the result data
     * @param <T> the data type
     * @return successful result
     */
    public static <T> SagaResult<T> success(String sagaId, T data) {
        return SagaResult.<T>builder()
                .sagaId(sagaId)
                .success(true)
                .data(data)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a failed saga result.
     *
     * @param sagaId the saga ID
     * @param errorMessage the error message
     * @param <T> the data type
     * @return failed result
     */
    public static <T> SagaResult<T> failure(String sagaId, String errorMessage) {
        return SagaResult.<T>builder()
                .sagaId(sagaId)
                .success(false)
                .errorMessage(errorMessage)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now())
                .build();
    }

    /**
     * Adds a step result.
     *
     * @param stepResult the step result
     */
    public void addStepResult(StepResult<T> stepResult) {
        this.stepResults.add(stepResult);
    }

    // Getters and Setters
    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public List<StepResult<T>> getStepResults() {
        return stepResults;
    }

    public void setStepResults(List<StepResult<T>> stepResults) {
        this.stepResults = stepResults;
    }

    // Builder pattern
    public static <T> SagaResultBuilder<T> builder() {
        return new SagaResultBuilder<T>();
    }

    public static class SagaResultBuilder<T> {
        private String sagaId;
        private boolean success;
        private T data;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private List<StepResult<T>> stepResults = new ArrayList<>();

        public SagaResultBuilder<T> sagaId(String sagaId) {
            this.sagaId = sagaId;
            return this;
        }

        public SagaResultBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public SagaResultBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public SagaResultBuilder<T> errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public SagaResultBuilder<T> startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public SagaResultBuilder<T> endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public SagaResultBuilder<T> stepResults(List<StepResult<T>> stepResults) {
            this.stepResults = stepResults;
            return this;
        }

        public SagaResult<T> build() {
            SagaResult<T> result = new SagaResult<>();
            result.sagaId = this.sagaId;
            result.success = this.success;
            result.data = this.data;
            result.errorMessage = this.errorMessage;
            result.startTime = this.startTime;
            result.endTime = this.endTime;
            result.stepResults = this.stepResults;
            return result;
        }
    }
}
