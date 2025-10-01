package com.integrixs.backend.service;

// import com.integrixs.backend.service.deprecated.NotificationService;
import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SagaTransaction;
import com.integrixs.data.model.SagaStep;
import com.integrixs.data.sql.repository.SagaTransactionSqlRepository;
import com.integrixs.data.sql.repository.SagaStepSqlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Saga Transaction Service for managing distributed transactions
 * Implements the Saga pattern for handling complex multi - step flows with compensation
 */
public abstract class SagaTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(SagaTransactionService.class);

    @Autowired
    private SagaTransactionSqlRepository transactionRepository;

    @Autowired
    private SagaStepSqlRepository stepRepository;

    // @Autowired
    // private NotificationService notificationService;

    // Registry for saga step handlers
    private final Map<String, SagaStepHandler> stepHandlers = new ConcurrentHashMap<>();

    // Registry for compensation handlers
    private final Map<String, CompensationHandler> compensationHandlers = new ConcurrentHashMap<>();

    /**
     * Register a saga step handler
     */
    public void registerStepHandler(String stepType, SagaStepHandler handler) {
        stepHandlers.put(stepType, handler);
        logger.info("Registered saga step handler for type: {}", stepType);
    }

    /**
     * Register a compensation handler
     */
    public void registerCompensationHandler(String stepType, CompensationHandler handler) {
        compensationHandlers.put(stepType, handler);
        logger.info("Registered compensation handler for type: {}", stepType);
    }

    /**
     * Start a new saga transaction
     */
    public SagaTransaction startTransaction(IntegrationFlow flow, String correlationId) {
        try {
            SagaTransaction transaction = new SagaTransaction();
            transaction.setFlow(flow);
            transaction.setCorrelationId(correlationId);
            transaction.setStatus(SagaTransaction.SagaStatus.STARTED);
            transaction.setStartedAt(LocalDateTime.now());
            transaction.setCurrentStep(0);

            transaction = transactionRepository.save(transaction);
            logger.info("Started saga transaction {} for flow {}", transaction.getId(), flow.getName());

            return transaction;

        } catch(Exception e) {
            logger.error("Failed to start saga transaction for flow {}", flow.getId(), e);
            throw new RuntimeException("Failed to start saga transaction", e);
        }
    }

    /**
     * Execute a saga with defined steps
     */
    public CompletableFuture<SagaResult> executeSaga(String transactionId, List<SagaStepDefinition> steps) {
        return CompletableFuture.supplyAsync(() -> {
            SagaTransaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

            List<SagaStep> executedSteps = new ArrayList<>();
            Map<String, Object> sagaContext = new HashMap<>();
            sagaContext.put("transactionId", transactionId);
            sagaContext.put("correlationId", transaction.getCorrelationId());

            try {
                // Execute each step in sequence
                for(int i = 0; i < steps.size(); i++) {
                    SagaStepDefinition stepDef = steps.get(i);

                    // Update transaction current step
                    updateTransactionStep(transactionId, i);

                    // Execute step
                    SagaStep step = executeStep(transaction, stepDef, sagaContext);
                    executedSteps.add(step);

                    if(step.getStatus() == SagaStep.StepStatus.FAILED) {
                        // Step failed, start compensation
                        logger.error("Saga step {} failed, starting compensation", stepDef.getName());
                        compensate(transaction, executedSteps, sagaContext);

                        return SagaResult.failed(
                            transactionId,
                            "Saga failed at step: " + stepDef.getName(),
                            step.getErrorMessage()
                       );
                    }
                }

                // All steps completed successfully
                completeTransaction(transactionId);

                return SagaResult.success(transactionId, sagaContext);

            } catch(Exception e) {
                logger.error("Saga execution failed for transaction {}", transactionId, e);

                // Compensate executed steps
                try {
                    compensate(transaction, executedSteps, sagaContext);
                } catch(Exception ce) {
                    logger.error("Compensation failed for transaction {}", transactionId, ce);
                }

                failTransaction(transactionId, e.getMessage());

                return SagaResult.failed(transactionId, "Saga execution failed", e.getMessage());
            }
        });
    }

    /**
     * Execute a single saga step
     */
    protected SagaStep executeStep(SagaTransaction transaction, SagaStepDefinition stepDef,
                                 Map<String, Object> context) {

        logger.info("Executing saga step: {}", stepDef.getName());

        // Create step record
        SagaStep step = new SagaStep();
        step.setSagaTransaction(transaction);
        step.setStepName(stepDef.getName());
        step.setActionType(stepDef.getType());
        step.setStepOrder(stepDef.getOrder());
        step.setStatus(SagaStep.StepStatus.STARTED);
        step.setStartedAt(LocalDateTime.now());

        step = stepRepository.save(step);

        try {
            // Get step handler
            SagaStepHandler handler = stepHandlers.get(stepDef.getType());
            if(handler == null) {
                throw new IllegalArgumentException("No handler registered for step type: " + stepDef.getType());
            }

            // Execute step
            StepResult result = handler.execute(stepDef, context);

            // Update step with result
            step.setStatus(result.isSuccess() ? SagaStep.StepStatus.COMPLETED : SagaStep.StepStatus.FAILED);
            step.setCompletedAt(LocalDateTime.now());
            step.setResultData(result.getData());

            if(!result.isSuccess()) {
                step.setErrorMessage(result.getErrorMessage());
            }

            // Update context with step results
            if(result.getData() != null) {
                context.put(stepDef.getName() + "_result", result.getData());
            }

            step = stepRepository.save(step);

            logger.info("Saga step {} completed with status: {}", stepDef.getName(), step.getStatus());

        } catch(Exception e) {
            logger.error("Saga step {} failed with error", stepDef.getName(), e);

            step.setStatus(SagaStep.StepStatus.FAILED);
            step.setCompletedAt(LocalDateTime.now());
            step.setErrorMessage(e.getMessage());
            step = stepRepository.save(step);
        }

        return step;
    }

    /**
     * Compensate executed steps in reverse order
     */
    protected void compensate(SagaTransaction transaction, List<SagaStep> executedSteps,
                           Map<String, Object> context) {

        logger.info("Starting compensation for transaction {}", transaction.getId());

        updateTransactionStatus(transaction.getId().toString(), SagaTransaction.SagaStatus.COMPENSATING);

        // Compensate in reverse order
        for(int i = executedSteps.size() - 1; i >= 0; i--) {
            SagaStep step = executedSteps.get(i);

            if(step.getStatus() == SagaStep.StepStatus.COMPLETED) {
                try {
                    compensateStep(step, context);
                } catch(Exception e) {
                    logger.error("Failed to compensate step {}", step.getStepName(), e);
                    // Continue with other compensations
                }
            }
        }

        updateTransactionStatus(transaction.getId().toString(), SagaTransaction.SagaStatus.COMPENSATING);
    }

    /**
     * Compensate a single step
     */
    private void compensateStep(SagaStep step, Map<String, Object> context) {
        logger.info("Compensating saga step: {}", step.getStepName());

        CompensationHandler handler = compensationHandlers.get(step.getActionType());
        if(handler == null) {
            logger.warn("No compensation handler for step type: {}", step.getActionType());
            return;
        }

        try {
            handler.compensate(step, context);

            step.setCompensated(true);
            step.setCompletedAt(LocalDateTime.now());
            stepRepository.save(step);

            logger.info("Successfully compensated step: {}", step.getStepName());

        } catch(Exception e) {
            logger.error("Failed to compensate step: {}", step.getStepName(), e);
            throw new RuntimeException("Compensation failed for step: " + step.getStepName(), e);
        }
    }

    /**
     * Update transaction status
     */
    private void updateTransactionStatus(String transactionId, SagaTransaction.SagaStatus status) {
        SagaTransaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        transaction.setStatus(status);
        if(status == SagaTransaction.SagaStatus.COMPLETED ||
            status == SagaTransaction.SagaStatus.FAILED ||
            status == SagaTransaction.SagaStatus.COMPENSATING) {
            transaction.setCompletedAt(LocalDateTime.now());
        }

        transactionRepository.save(transaction);
    }

    /**
     * Update transaction current step
     */
    private void updateTransactionStep(String transactionId, int stepIndex) {
        SagaTransaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        transaction.setCurrentStep(stepIndex);
        transactionRepository.save(transaction);
    }

    /**
     * Complete a transaction
     */
    protected void completeTransaction(String transactionId) {
        updateTransactionStatus(transactionId, SagaTransaction.SagaStatus.COMPLETED);
        logger.info("Saga transaction {} completed successfully", transactionId);
    }

    /**
     * Fail a transaction
     */
    protected void failTransaction(String transactionId, String error) {
        SagaTransaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

        transaction.setStatus(SagaTransaction.SagaStatus.FAILED);
        transaction.setErrorMessage(error);
        transaction.setCompletedAt(LocalDateTime.now());

        transactionRepository.save(transaction);
        logger.error("Saga transaction {} failed: {}", transactionId, error);
    }

    /**
     * Get transaction status
     */
    public Optional<SagaTransaction> getTransaction(String transactionId) {
        return transactionRepository.findById(UUID.fromString(transactionId));
    }

    /**
     * Get transaction steps
     */
    public List<SagaStep> getTransactionSteps(String transactionId) {
        SagaTransaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        return stepRepository.findByTransaction(transaction);
    }

    // Supporting classes

    /**
     * Saga step definition
     */
    public static class SagaStepDefinition {
        private final String name;
        private final String type;
        private final int order;
        private final Map<String, Object> parameters;

        public SagaStepDefinition(String name, String type, int order, Map<String, Object> parameters) {
            this.name = name;
            this.type = type;
            this.order = order;
            this.parameters = parameters != null ? parameters : new HashMap<>();
        }

        public String getName() { return name; }
        public String getType() { return type; }
        public int getOrder() { return order; }
        public Map<String, Object> getParameters() { return parameters; }
    }

    /**
     * Step execution result
     */
    public static class StepResult {
        private final boolean success;
        private final String data;
        private final String errorMessage;

        public static StepResult success(String data) {
            return new StepResult(true, data, null);
        }

        public static StepResult failure(String errorMessage) {
            return new StepResult(false, null, errorMessage);
        }

        private StepResult(boolean success, String data, String errorMessage) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public String getData() { return data; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Saga execution result
     */
    public static class SagaResult {
        private final String transactionId;
        private final boolean success;
        private final Map<String, Object> context;
        private final String errorMessage;
        private final String errorDetails;

        public static SagaResult success(String transactionId, Map<String, Object> context) {
            return new SagaResult(transactionId, true, context, null, null);
        }

        public static SagaResult failed(String transactionId, String errorMessage, String errorDetails) {
            return new SagaResult(transactionId, false, null, errorMessage, errorDetails);
        }

        private SagaResult(String transactionId, boolean success, Map<String, Object> context,
                         String errorMessage, String errorDetails) {
            this.transactionId = transactionId;
            this.success = success;
            this.context = context;
            this.errorMessage = errorMessage;
            this.errorDetails = errorDetails;
        }

        public String getTransactionId() { return transactionId; }
        public boolean isSuccess() { return success; }
        public Map<String, Object> getContext() { return context; }
        public String getErrorMessage() { return errorMessage; }
        public String getErrorDetails() { return errorDetails; }
    }

    /**
     * Interface for saga step handlers
     */
    public interface SagaStepHandler {
        StepResult execute(SagaStepDefinition step, Map<String, Object> context);
    }

    /**
     * Interface for compensation handlers
     */
    public interface CompensationHandler {
        void compensate(SagaStep step, Map<String, Object> context);
    }
}
