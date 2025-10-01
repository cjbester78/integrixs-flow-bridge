package com.integrixs.backend.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect for logging business operations with enhanced context and structure.
 */
@Aspect
@Component
public class BusinessOperationLogger {

    private static final Logger log = LoggerFactory.getLogger(BusinessOperationLogger.class);


    private static final String OPERATION_ID = "operationId";
    private static final String OPERATION_TYPE = "operationType";
    private static final String MODULE = "module";
    private static final String USER_ID = "userId";

    @Around("@annotation(operation)")
    public Object logBusinessOperation(ProceedingJoinPoint joinPoint, BusinessOperation operation) throws Throwable {
        String operationId = UUID.randomUUID().toString();
        String operationType = operation.value();
        String module = operation.module().isEmpty() ? joinPoint.getTarget().getClass().getSimpleName() : operation.module();

        // Set MDC context
        MDC.put(OPERATION_ID, operationId);
        MDC.put(OPERATION_TYPE, operationType);
        MDC.put(MODULE, module);

        Instant startTime = Instant.now();

        try {
            // Log operation start
            logOperationStart(operationType, module, joinPoint, operation);

            // Execute the operation
            Object result = joinPoint.proceed();

            // Log operation success
            long duration = ChronoUnit.MILLIS.between(startTime, Instant.now());
            logOperationSuccess(operationType, module, result, duration, operation);

            return result;

        } catch(Exception e) {
            // Log operation failure
            long duration = ChronoUnit.MILLIS.between(startTime, Instant.now());
            logOperationFailure(operationType, module, e, duration);
            throw e;

        } finally {
            // Clear MDC context
            MDC.remove(OPERATION_ID);
            MDC.remove(OPERATION_TYPE);
            MDC.remove(MODULE);
        }
    }

    private void logOperationStart(String operationType, String module, ProceedingJoinPoint joinPoint, BusinessOperation operation) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("OPERATION.START\n");
        logMessage.append("Type: ").append(operationType).append("\n");
        logMessage.append("Module: ").append(module).append("\n");
        logMessage.append("Method: ").append(joinPoint.getSignature().getName()).append("\n");

        if(operation.logInput() && joinPoint.getArgs().length > 0) {
            logMessage.append("Parameters: ").append(Arrays.toString(joinPoint.getArgs())).append("\n");
        }

        logMessage.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
        logMessage.append("Timestamp: ").append(Instant.now());

        log.info(logMessage.toString());
    }

    private void logOperationSuccess(String operationType, String module, Object result, long duration, BusinessOperation operation) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("OPERATION.SUCCESS\n");
        logMessage.append("Type: ").append(operationType).append("\n");
        logMessage.append("Module: ").append(module).append("\n");

        if(operation.includeMetrics()) {
            logMessage.append("Duration: ").append(duration).append("ms\n");
        }

        if(operation.logOutput() && result != null) {
            String resultString = result.toString();
            if(resultString.length() > 200) {
                resultString = resultString.substring(0, 200) + "...";
            }
            logMessage.append("Result: ").append(resultString).append("\n");
        }

        logMessage.append("Status: COMPLETED");

        log.info(logMessage.toString());
    }

    private void logOperationFailure(String operationType, String module, Exception e, long duration) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("OPERATION.FAILED\n");
        logMessage.append("Type: ").append(operationType).append("\n");
        logMessage.append("Module: ").append(module).append("\n");
        logMessage.append("Duration: ").append(duration).append("ms\n");
        logMessage.append("Error Type: ").append(e.getClass().getSimpleName()).append("\n");
        logMessage.append("Error Message: ").append(e.getMessage()).append("\n");
        logMessage.append("Status: FAILED");

        log.error(logMessage.toString(), e);
    }
}
