package com.fuzz.datacontroller.annotations;

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
}
