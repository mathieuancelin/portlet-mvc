package com.sample.portlet.fwk.annotation;

import com.sample.portlet.fwk.AbstractPortletController;
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
public @interface OnAction {

    /**
     * @return action name
     */
    String value() default AbstractPortletController.WILDCARD;
}
