package com.fuzz.datacontroller.datacontroller2;

import com.fuzz.datacontroller.datacontroller2.fetcher.DataFetcher;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Provides basic implementation of a data controller.
 */
public class DataController<TResponse, TStorage> {

    private DataSource<TResponse, TStorage> dataSource;

    private DataFetcher<TResponse> dataFetcher;

    private DataCaller<TResponse, TStorage> dataCaller;
}
