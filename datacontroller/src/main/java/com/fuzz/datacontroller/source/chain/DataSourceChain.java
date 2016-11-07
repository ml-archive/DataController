package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: Simple class that provides convenience for {@link ChainingSource} chaining operations.
 *
 * @author Andrew Grosner (Fuzz)
 */
public class DataSourceChain<T> {

    private DataSourceChain(Builder<T> builder) {

    }

    public static class Builder<T> {

        Builder(DataSource<T> dataSource) {

        }
    }
}
