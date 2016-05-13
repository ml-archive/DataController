package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.source.DataSource.SourceType;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Description: Stores {@link DataSource} in a {@link TreeMap} based on its {@link SourceType} ordering.
 */
public class TreeMapSingleTypeDataSourceContainer<TResponse> implements DataSourceStorage<TResponse> {

    private final Map<SourceType, DataSource<TResponse>>
            dataSourceMap = new TreeMap<>();

    @Override
    public void registerDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSourceMap) {
            dataSourceMap.put(dataSource.getSourceType(), dataSource);
        }
    }

    @Override
    public DataSource<TResponse> getDataSource(DataSourceParams sourceParams) {
        DataSource<TResponse> dataSource = null;
        if (sourceParams.sourceType != null) {
            dataSource = dataSourceMap.get(sourceParams.sourceType);
        }
        if (dataSource == null) {
            throw new RuntimeException("No data source found for type: " + sourceParams.sourceType);
        }
        return dataSource;
    }

    @Override
    public void deregisterDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSourceMap) {
            dataSourceMap.remove(dataSource.getSourceType());
        }
    }

    @Override
    public Collection<DataSource<TResponse>> sources() {
        synchronized (dataSourceMap) {
            return dataSourceMap.values();
        }
    }
}
