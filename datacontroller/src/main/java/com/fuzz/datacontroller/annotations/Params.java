package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.source.DataSource.SourceParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Describes a method that returns associated {@link SourceParams} for a particular method.
 * The return type must be a valid set of source params.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Params {

    /**
     * @return The name of the method referenced that generates source params.
     */
    String value();
}
