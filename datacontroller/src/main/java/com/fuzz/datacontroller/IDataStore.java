package com.fuzz.datacontroller;

/**
 * Description: A simple data store that lets you store a set of data and retrieve it.
 */
public interface IDataStore<T> {

    void store(T object);

    T get();

    void clear();
}
