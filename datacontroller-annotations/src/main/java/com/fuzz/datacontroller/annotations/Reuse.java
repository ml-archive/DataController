package com.fuzz.datacontroller.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Reuses a data controller from another method. The datasources will be the same, but
 * the source targets may not always be.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Reuse {

    /**
     * @return The name of the method to reuse for data controller. Method names must be unique.
     */
    String value();
}
