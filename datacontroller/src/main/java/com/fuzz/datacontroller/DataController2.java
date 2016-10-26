package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceStorage;
import com.fuzz.datacontroller.source.TreeMapSingleTypeDataSourceContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */

public class DataController2<T> {

    private DataSourceStorage<T> dataSourceStorage;

    private DataSourceChainer<T> dataSourceChainer;

    DataController2(Builder<T> builder) {
        if (builder.dataSourceStorage != null) {
            dataSourceStorage = builder.dataSourceStorage;
        } else {
            dataSourceStorage = new TreeMapSingleTypeDataSourceContainer<>();
        }

        if (builder.dataSources.isEmpty()) {
            throw new IllegalStateException("You need to register at least one DataSource for this DataController");
        }

        for (DataSource<T> dataSource : builder.dataSources) {
            dataSourceStorage.registerDataSource(dataSource);
        }

        if (builder.dataSourceChainer != null) {
            dataSourceChainer = builder.dataSourceChainer;
        } else {
            dataSourceChainer = new DataSourceChainer<T>() {
                @Override
                public boolean shouldQueryNext(DataSource<T> lastSource,
                                               DataSource<T> sourceToChain) {
                    return true;
                }
            };
        }
    }

    public void cancel() {
        Collection<DataSource<T>> sourceCollection = dataSourceStorage.sources();
        for (DataSource<T> source : sourceCollection) {
            source.cancel();
        }
    }

    public void cancel(DataSourceStorage.DataSourceParams dataSourceParams) {
        DataSource<T> dataSource = dataSourceStorage.getDataSource(dataSourceParams);
        dataSource.cancel();
    }

    public DataControllerRequest.Builder<T> request() {
        return new DataControllerRequest.Builder<>(new ArrayList<>(dataSourceStorage.sources()),
                dataSourceChainer);
    }

    public DataControllerRequest.Builder<T> request(DataSourceStorage.DataSourceParams params) {
        DataSource<T> dataSource = dataSourceStorage.getDataSource(params);
        List<DataSource<T>> list = new ArrayList<>();
        list.add(dataSource);
        return new DataControllerRequest.Builder<>(list, dataSourceChainer);
    }

    public static final class Builder<T> {

        private List<DataSource<T>> dataSources = new ArrayList<>();

        private DataSourceStorage<T> dataSourceStorage;

        private DataSourceChainer<T> dataSourceChainer;

        public Builder<T> dataSource(DataSource<T> dataSource) {
            dataSources.add(dataSource);
            return this;
        }

        public Builder<T> dataSourceStorage(DataSourceStorage<T> dataSourceStorage) {
            this.dataSourceStorage = dataSourceStorage;
            return this;
        }

        public Builder<T> dataSourceChainer(DataSourceChainer<T> dataSourceChainer) {
            this.dataSourceChainer = dataSourceChainer;
            return this;
        }

        public DataController2<T> build() {
            return new DataController2<>(this);
        }
    }
}
