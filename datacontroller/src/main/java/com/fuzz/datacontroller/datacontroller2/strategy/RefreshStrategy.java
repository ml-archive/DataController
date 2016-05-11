package com.fuzz.datacontroller.datacontroller2.strategy;

import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Provides chaining between different {@link DataSource}. This allows you to
 * intercept and determine whether a {@link DataSource} should get called.
 */
public interface RefreshStrategy<TResponse> {

    boolean shouldRefresh(DataSource<TResponse> dataSource);
}
