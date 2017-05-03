package com.fuzz.datacontroller.annotations;

import com.fuzz.datacontroller.DataController;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description: If present, we will cancel the {@link DataController} this method references.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface CancelDataController {
}
