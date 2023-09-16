package com.mdc.mspring.mvc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServletVariable {
    boolean required() default true;

    String defaultValue() default "";

    // 接收url的参数名
    String value() default "";
}
