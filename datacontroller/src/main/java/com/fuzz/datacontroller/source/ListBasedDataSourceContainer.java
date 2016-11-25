package com.fuzz.datacontroller.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description: Allows adding any amount of {@link DataSource} into a {@link java.util.List} that
 * we use together.
 */
public class ListBasedDataSourceContainer<TResponse> implements DataSourceContainer<TResponse> {

    private final Map<DataSourceParams, DataSource<TResponse>> dataSources;

    public ListBasedDataSourceContainer(Map<DataSourceParams, DataSource<TResponse>> dataSources) {
        this.dataSources = dataSources;
    }

    public ListBasedDataSourceContainer() {
        this(new LinkedHashMap<DataSourceParams, DataSource<TResponse>>());
    }

    @Override
    public void registerDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSources) {
            dataSources.put(new DataSourceParams(dataSources.size()), dataSource);
        }
    }

    @Override
    public DataSource<TResponse> getDataSource(DataSourceParams sourceParams) {
        DataSource<TResponse> dataSource = null;
        if (sourceParams.getPosition() > -1 && sourceParams.getPosition() < dataSources.size()) {
            dataSource = new ArrayList<>(dataSources.values()).get(sourceParams.getPosition());
        }
        if (dataSource == null) {
            throw new ArrayIndexOutOfBoundsException("Invalid index or item not found for source params " +
                    "at position: " + sourceParams.getPosition());
        }
        return dataSource;
    }

    @Override
    public DataSourceParams paramsForDataSource(DataSource<TResponse> dataSource) {
        DataSourceParams params = null;
        for (Map.Entry<DataSourceParams, DataSource<TResponse>> source : dataSources.entrySet()) {
            if (source.getValue().equals(dataSource)) {
                params = source.getKey();
                break;
            }
        }
        return params;
    }

    @Override
    public void deregisterDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSources) {
            DataSourceParams paramsToRemove = paramsForDataSource(dataSource);
            if (paramsToRemove != null) {
                dataSources.remove(paramsToRemove);
            }
        }
    }

    @Override
    public Collection<DataSource<TResponse>> sources() {
        return dataSources.values();
    }
}
