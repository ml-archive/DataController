package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.source.DataSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Defines a class for global configuration options for certain annotations. These options
 * get reused unless otherwise specified by individual annotations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface DataControllerConfig {

    /**
     * @return An instance to get invoked via default constructor to handle responses.
     */
    Class<?> responseHandler() default Object.class;

    /**
     * @return An instance to get invoked via default constructor to handle error conversions.
     */
    Class<?> errorConverter() default Object.class;

    /**
     * @return {@link DataSource.RefreshStrategy} to use. Must have a default constructor.
     */
    Class<? extends DataSource.RefreshStrategy> refreshStrategy() default DataSource.DefaultRefreshStrategy.class;
}
