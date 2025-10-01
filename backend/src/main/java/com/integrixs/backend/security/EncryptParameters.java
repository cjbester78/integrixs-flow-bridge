package com.integrixs.backend.security;

import java.lang.annotation.*;

/**
 * Annotation to encrypt specific method parameters.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptParameters {
    /**
     * Names of parameters to encrypt.
     */
    String[] value();
}
