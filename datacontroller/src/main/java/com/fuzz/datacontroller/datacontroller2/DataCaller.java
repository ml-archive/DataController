package com.fuzz.datacontroller.datacontroller2;

import com.fuzz.datacontroller.datacontroller2.fetcher.DataFetcher;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Maps a {@link DataFetcher} to a {@link DataSource}
 */
public interface DataCaller<TResponse, TStorage> {

    void call(DataFetcher<TResponse> dataFetcher, DataSource<TResponse, TStorage> dataSource);

    void cancel(DataFetcher<TResponse> dataFetcher);
}
