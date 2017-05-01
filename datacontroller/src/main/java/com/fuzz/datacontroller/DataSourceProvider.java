package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceContainer;

/**
 * Description: Interface for providing {@link DataSource}
 */
public interface DataSourceProvider {

    <T> DataSource<T> provideDataSource(Class<T> tClass, DataSourceContainer.DataSourceParams params);
}
