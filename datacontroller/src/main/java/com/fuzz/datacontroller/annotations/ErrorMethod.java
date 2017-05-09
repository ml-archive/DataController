package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.DataController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Annotats a method to get called when a {@link DataController.Error} occurs.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface ErrorMethod {

    /**
     * @return Represents the name of the callback to generate.
     */
    String value();
}
