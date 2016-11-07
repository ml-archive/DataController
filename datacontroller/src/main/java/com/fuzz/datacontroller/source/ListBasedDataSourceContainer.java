package com.fuzz.datacontroller.source;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description: Allows adding any amount of {@link DataSource} into a {@link java.util.List} that
 * we use together.
 */
public class ListBasedDataSourceContainer<TResponse> implements DataSourceContainer<TResponse> {

    private final List<DataSource<TResponse>> dataSources;

    public ListBasedDataSourceContainer(List<DataSource<TResponse>> dataSources) {
        this.dataSources = dataSources;
    }

    public ListBasedDataSourceContainer() {
        this(new ArrayList<DataSource<TResponse>>());
    }

    @Override
    public void registerDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSources) {
            dataSources.add(dataSource);
        }
    }

    @Override
    public DataSource<TResponse> getDataSource(DataSourceParams sourceParams) {
        DataSource<TResponse> dataSource = null;
        if (sourceParams.position > -1 && sourceParams.position < dataSources.size()) {
            dataSource = dataSources.get(sourceParams.position);
        }
        if (dataSource == null) {
            throw new ArrayIndexOutOfBoundsException("Invalid index or item not found for source params " +
                    "at position: " + sourceParams.position);
        }
        return dataSource;
    }

    @Override
    public void deregisterDataSource(DataSource<TResponse> dataSource) {
        synchronized (dataSources) {
            dataSources.remove(dataSource);
        }
    }

    @Override
    public Collection<DataSource<TResponse>> sources() {
        return dataSources;
    }
}
