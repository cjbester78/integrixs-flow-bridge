package com.integrixs.backend.service;

import com.integrixs.data.model.IntegrationFlow;
import com.integrixs.data.model.SagaTransaction;
import com.integrixs.data.model.SagaStep;
import com.integrixs.data.sql.repository.SagaTransactionSqlRepository;
import com.integrixs.data.sql.repository.SagaStepSqlRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Enhanced Saga Transaction Service with support for parallel execution
 * Extends the basic saga pattern to support parallel steps and complex orchestration
 */
@Service
@Primary
public class EnhancedSagaTransactionService extends SagaTransactionService {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedSagaTransactionService.class);

    @Autowired
    private SagaTransactionSqlRepository transactionRepository;

    @Autowired
    private SagaStepSqlRepository stepRepository;

    // Executor for parallel step execution
    private final ExecutorService parallelExecutor = Executors.newCachedThreadPool();

    /**
     * Execute a saga with support for parallel steps
     */
    public CompletableFuture<SagaResult> executeSagaWithParallel(String transactionId,
                                                                 List<SagaStepGroup> stepGroups) {
        return CompletableFuture.supplyAsync(() -> {
            SagaTransaction transaction = transactionRepository.findById(UUID.fromString(transactionId))
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

            List<SagaStep> allExecutedSteps = new ArrayList<>();
            Map<String, Object> sagaContext = new ConcurrentHashMap<>();
            sagaContext.put("transactionId", transactionId);
            sagaContext.put("correlationId", transaction.getCorrelationId());

            try {
                // Execute each step group
                for(int groupIndex = 0; groupIndex < stepGroups.size(); groupIndex++) {
                    SagaStepGroup group = stepGroups.get(groupIndex);

                    logger.info("Executing step group {} with {} steps in {} mode",
                        groupIndex, group.getSteps().size(), group.getExecutionMode());

                    List<SagaStep> groupResults;

                    if(group.getExecutionMode() == ExecutionMode.PARALLEL) {
                        groupResults = executeParallelSteps(transaction, group.getSteps(), sagaContext);
                    } else {
                        groupResults = executeSequentialSteps(transaction, group.getSteps(), sagaContext);
                    }

                    allExecutedSteps.addAll(groupResults);

                    // Check if any step in the group failed
                    boolean groupFailed = groupResults.stream()
                        .anyMatch(step -> step.getStatus() == SagaStep.StepStatus.FAILED);

                    if(groupFailed) {
                        logger.error("Step group {} failed, starting compensation", groupIndex);
                        compensate(transaction, allExecutedSteps, sagaContext);

                        String failedStep = groupResults.stream()
                            .filter(step -> step.getStatus() == SagaStep.StepStatus.FAILED)
                            .map(SagaStep::getStepName)
                            .findFirst()
                            .orElse("Unknown");

                        return SagaResult.failed(
                            transactionId,
                            "Saga failed at step: " + failedStep,
                            "Group " + groupIndex + " execution failed"
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
                    compensate(transaction, allExecutedSteps, sagaContext);
                } catch(Exception ce) {
                    logger.error("Compensation failed for transaction {}", transactionId, ce);
                }

                failTransaction(transactionId, e.getMessage());

                return SagaResult.failed(transactionId, "Saga execution failed", e.getMessage());
            }
        });
    }

    /**
     * Execute steps in parallel
     */
    private List<SagaStep> executeParallelSteps(SagaTransaction transaction,
                                               List<SagaStepDefinition> steps,
                                               Map<String, Object> context) {

        List<CompletableFuture<SagaStep>> futures = steps.stream()
            .map(stepDef -> CompletableFuture.supplyAsync(
                () -> executeStep(transaction, stepDef, context),
                parallelExecutor
           ))
            .collect(Collectors.toList());

        // Wait for all parallel steps to complete
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
       );

        try {
            allOf.get(5, TimeUnit.MINUTES); // Timeout after 5 minutes
        } catch(TimeoutException e) {
            logger.error("Parallel step execution timed out", e);
            futures.forEach(f -> f.cancel(true));
        } catch(Exception e) {
            logger.error("Error waiting for parallel steps", e);
        }

        // Collect results
        return futures.stream()
            .map(future -> {
                try {
                    return future.get();
                } catch(Exception e) {
                    logger.error("Failed to get step result", e);
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Execute steps sequentially
     */
    private List<SagaStep> executeSequentialSteps(SagaTransaction transaction,
                                                 List<SagaStepDefinition> steps,
                                                 Map<String, Object> context) {
        List<SagaStep> executedSteps = new ArrayList<>();

        for(SagaStepDefinition stepDef : steps) {
            SagaStep step = executeStep(transaction, stepDef, context);
            executedSteps.add(step);

            if(step.getStatus() == SagaStep.StepStatus.FAILED) {
                break; // Stop on first failure
            }
        }

        return executedSteps;
    }

    /**
     * Create a split - join orchestration
     */
    public OrchestrationBuilder createSplitJoin() {
        return new OrchestrationBuilder();
    }

    /**
     * Builder for complex orchestrations
     */
    public static class OrchestrationBuilder {
        private final List<SagaStepGroup> groups = new ArrayList<>();

        public OrchestrationBuilder sequential(SagaStepDefinition... steps) {
            groups.add(new SagaStepGroup(
                Arrays.asList(steps),
                ExecutionMode.SEQUENTIAL
           ));
            return this;
        }

        public OrchestrationBuilder parallel(SagaStepDefinition... steps) {
            groups.add(new SagaStepGroup(
                Arrays.asList(steps),
                ExecutionMode.PARALLEL
           ));
            return this;
        }

        public OrchestrationBuilder split(String splitFunction, SagaStepDefinition... steps) {
            // Create a split step followed by parallel execution
            SagaStepDefinition splitStep = new SagaStepDefinition(
                "Split_" + UUID.randomUUID().toString(),
                "SPLIT",
                0,
                Map.of("function", splitFunction)
           );

            groups.add(new SagaStepGroup(
                Collections.singletonList(splitStep),
                ExecutionMode.SEQUENTIAL
           ));

            groups.add(new SagaStepGroup(
                Arrays.asList(steps),
                ExecutionMode.PARALLEL
           ));

            return this;
        }

        public OrchestrationBuilder join(String joinFunction) {
            SagaStepDefinition joinStep = new SagaStepDefinition(
                "Join_" + UUID.randomUUID().toString(),
                "JOIN",
                0,
                Map.of("function", joinFunction)
           );

            groups.add(new SagaStepGroup(
                Collections.singletonList(joinStep),
                ExecutionMode.SEQUENTIAL
           ));

            return this;
        }

        public List<SagaStepGroup> build() {
            return new ArrayList<>(groups);
        }
    }

    /**
     * Execute a fork - join pattern
     */
    public CompletableFuture<SagaResult> executeForkJoin(String transactionId,
                                                        List<SagaStepDefinition> forkSteps,
                                                        SagaStepDefinition joinStep) {

        OrchestrationBuilder builder = new OrchestrationBuilder();
        builder.parallel(forkSteps.toArray(new SagaStepDefinition[0]))
               .sequential(joinStep);

        return executeSagaWithParallel(transactionId, builder.build());
    }

    /**
     * Execute a scatter - gather pattern
     */
    public CompletableFuture<SagaResult> executeScatterGather(String transactionId,
                                                             String scatterFunction,
                                                             List<SagaStepDefinition> gatherSteps,
                                                             String aggregateFunction) {

        // Create scatter step
        SagaStepDefinition scatterStep = new SagaStepDefinition(
            "Scatter",
            "SCATTER",
            0,
            Map.of("function", scatterFunction)
       );

        // Create aggregate step
        SagaStepDefinition aggregateStep = new SagaStepDefinition(
            "Aggregate",
            "AGGREGATE",
            0,
            Map.of("function", aggregateFunction)
       );

        OrchestrationBuilder builder = new OrchestrationBuilder();
        builder.sequential(scatterStep)
               .parallel(gatherSteps.toArray(new SagaStepDefinition[0]))
               .sequential(aggregateStep);

        return executeSagaWithParallel(transactionId, builder.build());
    }

    /**
     * Step group definition
     */
    public static class SagaStepGroup {
        private final List<SagaStepDefinition> steps;
        private final ExecutionMode executionMode;

        public SagaStepGroup(List<SagaStepDefinition> steps, ExecutionMode executionMode) {
            this.steps = steps;
            this.executionMode = executionMode;
        }

        public List<SagaStepDefinition> getSteps() { return steps; }
        public ExecutionMode getExecutionMode() { return executionMode; }
    }

    /**
     * Execution modes
     */
    public enum ExecutionMode {
        SEQUENTIAL,
        PARALLEL
    }

    @Override
    protected void finalize() throws Throwable {
        parallelExecutor.shutdown();
        super.finalize();
    }
}
