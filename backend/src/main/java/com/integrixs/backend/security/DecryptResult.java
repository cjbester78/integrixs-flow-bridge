package com.integrixs.backend.security;

import java.lang.annotation.*;

/**
 * Annotation to decrypt method return values.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DecryptResult {
    /**
     * Field name for decryption context. If empty, uses method name.
     */
    String fieldName() default "";
}
