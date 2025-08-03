package com.beaver.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPermission {
    /**
     * The permissions required to access the annotated method
     */
    Permission[] value();

    /**
     * Whether all specified permissions are required (true) or just one (false)
     * Default is false (OR logic - user needs at least one of the specified permissions)
     */
    boolean requireAll() default false;
}
