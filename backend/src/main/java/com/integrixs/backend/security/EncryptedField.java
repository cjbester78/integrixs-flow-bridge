package com.integrixs.backend.security;

import java.lang.annotation.*;

/**
 * Annotation to mark fields that should be encrypted in the database.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EncryptedField {

    /**
     * Optional encryption algorithm. Default uses service configuration.
     */
    String algorithm() default "";

    /**
     * Whether to enable searching on this encrypted field.
     * If true, a deterministic encryption mode will be used(less secure).
     */
    boolean searchable() default false;
}
