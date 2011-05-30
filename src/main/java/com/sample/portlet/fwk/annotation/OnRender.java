package com.sample.portlet.fwk.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(METHOD)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface OnRender {

    enum Phase {
        VIEW, EDIT, HELP, ALL
    }

    Phase value() default Phase.ALL;
}
