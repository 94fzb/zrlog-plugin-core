package com.zrlog.plugin.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
    String value() default "";

    boolean enable() default true;
}
