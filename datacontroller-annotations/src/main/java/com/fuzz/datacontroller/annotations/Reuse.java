package com.fuzz.datacontroller.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Reuse {

    /**
     * @return The name of the method to reuse for data controller. Method names must be unique.
     */
    String value();
}
