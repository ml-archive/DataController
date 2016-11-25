package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.source.DataSource.SourceType;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Description: Stores {@link DataSource} in a {@link TreeMap} based on its {@link SourceType} ordering.
 */
public class TreeMapSingleTypeDataSourceContainer<TResponse> implements DataSourceContainer<TResponse> {

    private final Map<DataSourceParams, DataSource<TResponse>>
            dataSourceMap = new TreeMap<>();

    @Override
    public void registerDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSourceMap) {
            dataSourceMap.put(new DataSourceParams(dataSource.sourceType()), dataSource);
        }
    }

    @Override
    public DataSource<TResponse> getDataSource(DataSourceParams sourceParams) {
        DataSource<TResponse> dataSource = null;
        if (sourceParams.getSourceType() != null) {
            for (Map.Entry<DataSourceParams, DataSource<TResponse>> entry : dataSourceMap.entrySet()) {
                if (entry.getKey().getSourceType().equals(sourceParams.getSourceType())) {
                    dataSource = entry.getValue();
                    break;
                }
            }
        }
        if (dataSource == null) {
            throw new RuntimeException("No data source found for type: " + sourceParams.getSourceType());
        }
        return dataSource;
    }

    @Override
    public DataSourceParams paramsForDataSource(DataSource<TResponse> dataSource) {
        DataSourceParams params = null;
        for (Map.Entry<DataSourceParams, DataSource<TResponse>> entry : dataSourceMap.entrySet()) {
            if (entry.getValue().equals(dataSource)) {
                params = entry.getKey();
                break;
            }
        }
        return params;
    }

    @Override
    public void deregisterDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSourceMap) {
            DataSourceParams paramsToRemove = paramsForDataSource(dataSource);
            if (paramsToRemove != null) {
                dataSourceMap.remove(paramsToRemove);
            }
        }
    }

    @Override
    public Collection<DataSource<TResponse>> sources() {
        synchronized (dataSourceMap) {
            return dataSourceMap.values();
        }
    }
}
