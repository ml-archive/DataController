package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.source.DataSource;

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

    /**
     * @return {@link DataSource.RefreshStrategy} to use. Must have a default constructor.
     */
    Class<? extends DataSource.RefreshStrategy> refreshStrategy() default DataSource.DefaultRefreshStrategy.class;

}
