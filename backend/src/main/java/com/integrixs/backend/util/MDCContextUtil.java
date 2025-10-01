package com.integrixs.backend.util;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class for MDC context propagation in various scenarios.
 * Helps maintain logging context across thread boundaries.
 */
public final class MDCContextUtil {

    // Private constructor to prevent instantiation
    private MDCContextUtil() {
        // Utility class - not meant to be instantiated
    }

    /**
     * Wraps a Runnable with MDC context preservation
     */
    public static Runnable wrapWithMDC(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return() -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
                if(contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                runnable.run();
            } finally {
                if(previousContext != null) {
                    MDC.setContextMap(previousContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * Wraps a Callable with MDC context preservation
     */
    public static <T> Callable<T> wrapWithMDC(Callable<T> callable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return() -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
                if(contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                return callable.call();
            } finally {
                if(previousContext != null) {
                    MDC.setContextMap(previousContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * Wraps a Supplier with MDC context preservation
     */
    public static <T> Supplier<T> wrapWithMDC(Supplier<T> supplier) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return() -> {
            Map<String, String> previousContext = MDC.getCopyOfContextMap();
            try {
                if(contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                return supplier.get();
            } finally {
                if(previousContext != null) {
                    MDC.setContextMap(previousContext);
                } else {
                    MDC.clear();
                }
            }
        };
    }

    /**
     * Creates a CompletableFuture that preserves MDC context
     */
    public static <T> CompletableFuture<T> supplyAsyncWithMDC(Supplier<T> supplier) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return CompletableFuture.supplyAsync(() -> {
            if(contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                return supplier.get();
            } finally {
                MDC.clear();
            }
        });
    }

    /**
     * Runs async operation with MDC context
     */
    public static CompletableFuture<Void> runAsyncWithMDC(Runnable runnable) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        return CompletableFuture.runAsync(() -> {
            if(contextMap != null) {
                MDC.setContextMap(contextMap);
            }
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        });
    }

    /**
     * Sets flow context in MDC
     */
    public static void setFlowContext(String flowId, String flowName, String correlationId) {
        MDC.put("flowId", flowId);
        MDC.put("flowName", flowName);
        MDC.put("correlationId", correlationId);
    }

    /**
     * Sets adapter context in MDC
     */
    public static void setAdapterContext(String adapterId, String adapterName, String adapterType) {
        MDC.put("adapterId", adapterId);
        MDC.put("adapterName", adapterName);
        MDC.put("adapterType", adapterType);
    }

    /**
     * Sets operation context in MDC
     */
    public static void setOperationContext(String operationId, String operationType, String module) {
        MDC.put("operationId", operationId);
        MDC.put("operationType", operationType);
        MDC.put("module", module);
    }

    /**
     * Clears specific context keys from MDC
     */
    public static void clearContext(String... keys) {
        for(String key : keys) {
            MDC.remove(key);
        }
    }

    /**
     * Executes a block of code with temporary MDC context
     */
    public static <T> T executeWithContext(Map<String, String> context, Supplier<T> supplier) {
        Map<String, String> previousContext = MDC.getCopyOfContextMap();
        try {
            context.forEach(MDC::put);
            return supplier.get();
        } finally {
            MDC.clear();
            if(previousContext != null) {
                MDC.setContextMap(previousContext);
            }
        }
    }
}
