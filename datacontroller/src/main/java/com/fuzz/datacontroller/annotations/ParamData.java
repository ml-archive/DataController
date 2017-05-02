package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.source.DataSource.SourceType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Marks a parameter as belonging to Data Source Params "data" property. It will get
 * assigned to the params used in the generated code.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface ParamData {

    /**
     * @return The source for which it belongs. Compiler error will occur if targeted {@link DataController}
     * does not match this targeted source.
     */
    SourceType targetedSource() default SourceType.NETWORK;

    /**
     * @return will be used later to describe instead of {@link SourceType}, the position of source
     * when we have multiple of same source type.
     */
    int position() default -1;
}
