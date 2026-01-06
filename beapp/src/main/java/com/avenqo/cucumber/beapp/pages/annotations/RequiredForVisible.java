package com.avenqo.cucumber.beapp.pages.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredForVisible {
    // optional: Timeout für dieses Element
    int timeoutSeconds() default 10;
}