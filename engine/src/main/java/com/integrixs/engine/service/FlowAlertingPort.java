package com.integrixs.engine.service;

import com.integrixs.data.model.FlowExecution;

/**
 * Port interface for flow alerting functionality
 * This interface allows the engine module to use alerting functionality
 * without creating a circular dependency with the backend module
 */
public interface FlowAlertingPort {

    /**
     * Evaluate alert rules for a flow execution
     * @param flowExecution The flow execution to evaluate
     */
    void evaluateFlowAlerts(FlowExecution flowExecution);
}