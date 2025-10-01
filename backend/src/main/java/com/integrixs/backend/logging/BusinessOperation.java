package com.integrixs.backend.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that represent business operations requiring enhanced logging.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessOperation {

    /**
     * The type of business operation being performed
     */
    String value();

    /**
     * Module or component performing the operation
     */
    String module() default "";

    /**
     * Whether to log input parameters
     */
    boolean logInput() default true;

    /**
     * Whether to log output/result
     */
    boolean logOutput() default true;

    /**
     * Whether to include performance metrics
     */
    boolean includeMetrics() default true;
}
