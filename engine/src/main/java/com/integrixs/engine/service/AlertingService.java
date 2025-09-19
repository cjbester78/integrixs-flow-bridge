package com.integrixs.engine.service;

import com.integrixs.data.model.FlowExecution;

/**
 * Interface for alerting service to evaluate flow alerts
 * This interface allows the engine module to use alerting functionality
 * without creating a circular dependency with the backend module
 */
public interface AlertingService {
    
    /**
     * Evaluate alert rules for a flow execution
     * @param flowExecution The flow execution to evaluate
     */
    void evaluateFlowAlerts(FlowExecution flowExecution);
}