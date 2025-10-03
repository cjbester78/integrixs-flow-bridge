package com.integrixs.backend.camunda;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Error handling delegate for Camunda processes
 */
@Component("integrixErrorDelegate")
public class IntegrixErrorDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(IntegrixErrorDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        logger.info("Error handling for process instance: {}", execution.getProcessInstanceId());
        
        // Get error information
        String errorMessage = (String) execution.getVariable("errorMessage");
        String errorCode = (String) execution.getVariable("errorCode");
        String failedActivity = (String) execution.getVariable("failedActivity");
        
        // Log the error
        logger.error("Process error - Instance: {}, Activity: {}, Code: {}, Message: {}", 
            execution.getProcessInstanceId(), failedActivity, errorCode, errorMessage);
        
        // Set error handling variables
        execution.setVariable("errorHandled", true);
        execution.setVariable("errorTimestamp", System.currentTimeMillis());
        
        // Additional error handling logic can be added here
        // For example: send notifications, create incidents, etc.
    }
}