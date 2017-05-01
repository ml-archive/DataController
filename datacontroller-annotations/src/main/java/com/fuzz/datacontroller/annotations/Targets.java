package com.fuzz.datacontroller.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: Describes a request that targets the specified source type annotations in the method.
 * It will only request data from sources specified. May reuse data controller, or create a new one.
 * Creating a new DC will mean this annotation has no practical effect.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Targets {
}
