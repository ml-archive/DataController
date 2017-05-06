package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.DataControllerRequest;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSource.RefreshStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Used to define {@link DataControllerRef} with network component.
 * If specified in a type that contains {@link DataDefinition}, the {@link #responseHandler()} and {@link #errorConverter()}
 * are the global defaults when network is requested.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Network {

    /**
     * @return An instance to get invoked via default constructor to handle responses.
     */
    Class<?> responseHandler() default Object.class;

    /**
     * @return An instance to get invoked via default constructor to handle error conversions.
     */
    Class<?> errorConverter() default Object.class;

    /**
     * @return {@link RefreshStrategy} to use. Must have a default constructor.
     */
    Class<? extends RefreshStrategy> refreshStrategy() default DataSource.DefaultRefreshStrategy.class;

    /**
     * @return By default the call return type is the return type of the parameter from {@link DataControllerRequest}.
     * This allows you to determine what the return type is.
     */
    Class<?> callReturnType() default Object.class;
}
