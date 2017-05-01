package com.grosner.datacontroller.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Specifies a parameter is to be used in a {@link DB} query.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.PARAMETER)
public @interface DQuery {
    /**
     * @return The special
     */
    String value() default "";
}
