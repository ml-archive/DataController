package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.source.DataSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Memory {

    /**
     * @return {@link DataSource.RefreshStrategy} to use. Must have a default constructor.
     */
    Class<? extends DataSource.RefreshStrategy> refreshStrategy() default DataSource.DefaultRefreshStrategy.class;

}
