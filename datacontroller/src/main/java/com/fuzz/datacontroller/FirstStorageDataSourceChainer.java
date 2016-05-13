package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: If the previous source has {@link DataSource#hasStoredData()}, this will not execute.
 */
public class FirstStorageDataSourceChainer<TResponse> implements DataSourceChainer<TResponse> {
    @Override
    public boolean shouldQueryNext(DataSource<TResponse> lastSource, DataSource<TResponse> sourceToChain) {
        return !lastSource.hasStoredData();
    }
}
