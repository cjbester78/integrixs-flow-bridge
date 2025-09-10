package com.integrixs.testing.core;

import java.lang.annotation.*;

/**
 * Annotation to inject FlowTestContext into test class fields
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InjectFlowContext {
}