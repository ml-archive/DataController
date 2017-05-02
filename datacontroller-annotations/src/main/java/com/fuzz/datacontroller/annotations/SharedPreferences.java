package com.fuzz.datacontroller.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Marks a method or datacontroller reference as having shared preferences data source.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface SharedPreferences {

    /**
     * @return The preference delegate matching type of data source to use in the data source.
     */
    Class<?> preferenceDelegate();
}
