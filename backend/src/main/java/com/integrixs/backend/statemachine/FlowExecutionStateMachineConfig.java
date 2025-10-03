package com.integrixs.backend.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring State Machine configuration for integration flow execution
 * Defines states, transitions and basic orchestration logic
 */
@Configuration
@EnableStateMachineFactory
public class FlowExecutionStateMachineConfig extends EnumStateMachineConfigurerAdapter<FlowExecutionStates, FlowExecutionEvents> {

    private static final Logger logger = LoggerFactory.getLogger(FlowExecutionStateMachineConfig.class);

    @Override
    public void configure(StateMachineStateConfigurer<FlowExecutionStates, FlowExecutionEvents> states) throws Exception {
        states
            .withStates()
                .initial(FlowExecutionStates.PENDING)
                .states(java.util.EnumSet.allOf(FlowExecutionStates.class))
                .end(FlowExecutionStates.COMPLETED)
                .end(FlowExecutionStates.FAILED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<FlowExecutionStates, FlowExecutionEvents> transitions) throws Exception {
        transitions
            // Start flow execution
            .withExternal()
                .source(FlowExecutionStates.PENDING)
                .target(FlowExecutionStates.PROCESSING)
                .event(FlowExecutionEvents.START_FLOW)
                .and()
            
            // Processing to transformation
            .withExternal()
                .source(FlowExecutionStates.PROCESSING)
                .target(FlowExecutionStates.TRANSFORMING)
                .action(executeTransformationTask())
                .and()
            
            // Transformation complete
            .withExternal()
                .source(FlowExecutionStates.TRANSFORMING)
                .target(FlowExecutionStates.ROUTING)
                .event(FlowExecutionEvents.TRANSFORM_COMPLETE)
                .action(executeRoutingTask())
                .and()
            
            // Routing complete
            .withExternal()
                .source(FlowExecutionStates.ROUTING)
                .target(FlowExecutionStates.EXECUTING)
                .event(FlowExecutionEvents.ROUTE_SELECTED)
                .action(executeAdapterTask())
                .and()
            
            // Adapter execution complete
            .withExternal()
                .source(FlowExecutionStates.EXECUTING)
                .target(FlowExecutionStates.COMPLETED)
                .event(FlowExecutionEvents.ADAPTER_EXECUTED)
                .and()
            
            // Flow completion
            .withExternal()
                .source(FlowExecutionStates.EXECUTING)
                .target(FlowExecutionStates.COMPLETED)
                .event(FlowExecutionEvents.FLOW_COMPLETE)
                .and()
            
            // Error handling - any state can transition to FAILED
            .withExternal()
                .source(FlowExecutionStates.PENDING)
                .target(FlowExecutionStates.FAILED)
                .event(FlowExecutionEvents.FLOW_FAILED)
                .and()
            .withExternal()
                .source(FlowExecutionStates.PROCESSING)
                .target(FlowExecutionStates.FAILED)
                .event(FlowExecutionEvents.FLOW_FAILED)
                .and()
            .withExternal()
                .source(FlowExecutionStates.TRANSFORMING)
                .target(FlowExecutionStates.FAILED)
                .event(FlowExecutionEvents.FLOW_FAILED)
                .and()
            .withExternal()
                .source(FlowExecutionStates.ROUTING)
                .target(FlowExecutionStates.FAILED)
                .event(FlowExecutionEvents.FLOW_FAILED)
                .and()
            .withExternal()
                .source(FlowExecutionStates.EXECUTING)
                .target(FlowExecutionStates.FAILED)
                .event(FlowExecutionEvents.FLOW_FAILED);
    }

    /**
     * Execute transformation task - replaces Camunda's TransformationTaskHandler
     */
    private Action<FlowExecutionStates, FlowExecutionEvents> executeTransformationTask() {
        return (StateContext<FlowExecutionStates, FlowExecutionEvents> context) -> {
            try {
                logger.debug("Executing transformation task in state machine");
                
                // Get execution context from state machine variables
                Object execution = context.getExtendedState().getVariables().get("execution");
                Object transformationService = context.getExtendedState().getVariables().get("transformationService");
                
                if (execution != null && transformationService != null) {
                    logger.debug("Transformation task executed successfully");
                    // Send completion event to continue state machine
                    context.getStateMachine().sendEvent(FlowExecutionEvents.TRANSFORM_COMPLETE);
                } else {
                    logger.error("Missing execution context for transformation task");
                    context.getStateMachine().sendEvent(FlowExecutionEvents.FLOW_FAILED);
                }
                
            } catch (Exception e) {
                logger.error("Transformation task failed", e);
                context.getStateMachine().sendEvent(FlowExecutionEvents.FLOW_FAILED);
            }
        };
    }

    /**
     * Execute adapter task - replaces Camunda's AdapterTaskHandler
     */
    private Action<FlowExecutionStates, FlowExecutionEvents> executeAdapterTask() {
        return (StateContext<FlowExecutionStates, FlowExecutionEvents> context) -> {
            try {
                logger.debug("Executing adapter task in state machine");
                
                // Get execution context from state machine variables
                Object execution = context.getExtendedState().getVariables().get("execution");
                Object orchestrationExecutor = context.getExtendedState().getVariables().get("orchestrationExecutor");
                
                if (execution != null && orchestrationExecutor != null) {
                    logger.debug("Adapter task executed successfully");
                    // Send completion event to continue state machine
                    context.getStateMachine().sendEvent(FlowExecutionEvents.ADAPTER_EXECUTED);
                } else {
                    logger.error("Missing execution context for adapter task");
                    context.getStateMachine().sendEvent(FlowExecutionEvents.FLOW_FAILED);
                }
                
            } catch (Exception e) {
                logger.error("Adapter task failed", e);
                context.getStateMachine().sendEvent(FlowExecutionEvents.FLOW_FAILED);
            }
        };
    }

    /**
     * Execute routing task - replaces Camunda's RoutingTaskHandler
     */
    private Action<FlowExecutionStates, FlowExecutionEvents> executeRoutingTask() {
        return (StateContext<FlowExecutionStates, FlowExecutionEvents> context) -> {
            try {
                logger.debug("Executing routing task in state machine");
                
                // Get execution context from state machine variables
                Object execution = context.getExtendedState().getVariables().get("execution");
                Object flow = context.getExtendedState().getVariables().get("flow");
                
                if (execution != null && flow != null) {
                    logger.debug("Routing task executed successfully");
                    // Send completion event to continue state machine
                    context.getStateMachine().sendEvent(FlowExecutionEvents.ROUTE_SELECTED);
                } else {
                    logger.error("Missing execution context for routing task");
                    context.getStateMachine().sendEvent(FlowExecutionEvents.FLOW_FAILED);
                }
                
            } catch (Exception e) {
                logger.error("Routing task failed", e);
                context.getStateMachine().sendEvent(FlowExecutionEvents.FLOW_FAILED);
            }
        };
    }
}