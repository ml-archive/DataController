package com.fuzz.datacontroller.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Specifes a method to return a generated data controller.
 * This is useful for customization. The name of method must match the name of data controller.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface DataControllerRef {

    /**
     * @return if true, we construct the reference in constructor. We will not build sources
     * and only allow targeting via {@link Targets}. Otherwise, we throw a compiler error.
     */
    boolean inConstructor() default false;

    /**
     * @return if true, we will leave it out of a constructor that we generate as a parameter
     * and instead generate null.
     */
    boolean optional() default false;
}
