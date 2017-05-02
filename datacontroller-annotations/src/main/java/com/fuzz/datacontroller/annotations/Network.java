package com.fuzz.datacontroller.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Used to define {@link DataControllerRef} with network component.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Network {

    /**
     * @return An instance to get invoked via default constructor to handle responses.
     */
    Class<?> responseHandler() default Object.class;

    /**
     * @return An instance to get invoked via default constructor to handle error conversions.
     */
    Class<?> errorConverter() default Object.class;
}
