package com.fuzz.datacontroller.internal;

/**
 * Description: Used internally to construct _Def classes.
 */
public interface Creator<T, Input> {

    T newInstance(Input input);
}
