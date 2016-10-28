package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: Will chain together two {@link DataSource} and determine if the next source in order
 * of priority should be queried.
 */
public interface DataSourceChainer<TResponse> {

    /**
     * @param lastSource    The previous data source called.
     * @param sourceToChain The next source that we query to determine if we should run this one along the chain.
     * @return True if the sourceToChain should call
     * {@link DataSource#get(DataSource.SourceParams, DataController2.Success, DataController2.Error)}
     * otherwise we break the chain.
     */
    boolean shouldQueryNext(DataSource<TResponse> lastSource, DataSource<TResponse> sourceToChain);
}
