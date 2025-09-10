package com.integrixs.testing.core;

import java.lang.annotation.*;

/**
 * Annotation to inject test utilities into test class fields
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestUtility {
}