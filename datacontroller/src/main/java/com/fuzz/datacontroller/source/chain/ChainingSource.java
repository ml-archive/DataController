package com.fuzz.datacontroller.source.chain;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerRequest;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: Provides building blocks to chain together different {@link DataSource}. The collection
 * of {@link DataSource} operate as one unit. This class sort of operate like a {@link DataControllerRequest}
 * in that we can specify {@link DataSource} to call and store, but the results are chained in order
 * of declaration. This source can have 1...N "request" data sources and 1...N "storage" data sources.
 *
 * @author Andrew Grosner (Fuzz)
 */
public class ChainingSource<T> implements DataSource.Source<T> {

    private final List<DataSourceChain<?, T>> dataSourceChains
            = new ArrayList<>();

    private ChainingSource(Builder<T> builder) {
        for (DataSourceChain<?, T> dataSource : builder.dataSourceChains) {
            dataSourceChains.add(dataSource);
        }
    }

    public DataSource.Builder<T> builderInstance(DataSource.SourceType registeredSourceType) {
        return new DataSource.Builder<>(this, registeredSourceType);
    }

    @Override
    public void get(DataSource.SourceParams sourceParams,
                    DataController.Error error,
                    DataController.Success<T> success) {
        recursiveGet(0, dataSourceChains, sourceParams, error, success);
    }

    @Override
    public void cancel() {

    }

    @Override
    public void store(DataControllerResponse<T> response) {

    }

    @Override
    public T getStoredData(DataSource.SourceParams params) {
        return null;
    }

    @Override
    public void clearStoredData(DataSource.SourceParams params) {

    }

    @Override
    public boolean hasStoredData(DataSource.SourceParams params) {
        return false;
    }

    void recursiveGet(final int position,
                      final List<DataSourceChain<?, T>> dataSources,
                      DataSource.SourceParams sourceParams,
                      DataController.Error error, final DataController.Success<T> success) {
        final DataSourceChain dataSource = dataSources.get(position);
        dataSource.get(sourceParams, new DataController.Success<T>() {
            @Override
            public void onSuccess(DataControllerResponse<T> response) {
                if (position == dataSources.size() - 1) {
                    success.onSuccess(response);
                }
            }
        }, error);
    }

    @SuppressWarnings("unchecked")
    public static class Builder<T> {

        private final List<DataSourceChain<?, T>> dataSourceChains = new ArrayList<>();

        public Builder(DataSourceChain<T, T> dataSourceChain) {
            dataSourceChains.add(dataSourceChain);
        }

        public <V> DataSourceChain.Builder<V, T> chain(DataSource<V> dataSource,
                                                       DataSourceChain.ResponseConverter<V, T>
                                                               responseConverter) {
            return new DataSourceChain.Builder<>(dataSource, responseConverter);
        }

        public ChainingSource<T> build() {
            return new ChainingSource<>(this);
        }

        public DataSource.Builder<T> builderInstance(DataSource.SourceType registeredSourceType) {
            return build().builderInstance(registeredSourceType);
        }
    }
}
