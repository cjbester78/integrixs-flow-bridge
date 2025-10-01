package com.integrixs.backend.interceptor;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Task decorator to propagate MDC context to async tasks.
 */
@Component
public class MDCTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        // Capture the current MDC context
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return() -> {
            if(contextMap != null) {
                // Set the captured context in the new thread
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                // Clear MDC after task execution
                MDC.clear();
            }
        };
    }
}
