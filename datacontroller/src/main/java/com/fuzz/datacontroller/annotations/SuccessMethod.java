package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.DataController.Success;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotates a method to get called when a {@link Success} occurs.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface SuccessMethod {

    /**
     * @return Represents the name of the callback to generate.
     */
    String value();

    /**
     * @return If true, we don't call success if response is somehow null.
     */
    boolean nonNullWrapping() default false;

}
