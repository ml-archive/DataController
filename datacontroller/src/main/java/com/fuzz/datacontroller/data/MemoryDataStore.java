package com.fuzz.datacontroller.data;

/**
 * Description: Holds onto data received in memory.
 */
public class MemoryDataStore<T> implements IDataStore<T> {

    private T storage;

    public MemoryDataStore() {
    }

    public MemoryDataStore(T storage) {
        this.storage = storage;
    }

    @Override
    public void store(T object) {
        storage = object;
    }

    @Override
    public T get() {
        return storage;
    }

    @Override
    public void clear() {
        storage = null;
    }
}
