package com.integrixs.testing.core;

import java.lang.annotation.*;

/**
 * Annotation to inject mock adapters into test class fields
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MockAdapter {
    /**
     * Name of the mock adapter to inject
     */
    String value();
}