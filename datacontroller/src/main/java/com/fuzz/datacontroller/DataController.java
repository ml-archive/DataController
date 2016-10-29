package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceContainer;
import com.fuzz.datacontroller.source.TreeMapSingleTypeDataSourceContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */

public class DataController<T> {

    /**
     * Description: Represents a failed callback response.
     */
    public interface Error {

        /**
         * Called when a {@link DataSource} fails to load data. This can happen at any point within
         * the source chain.
         *
         * @param dataResponseError The error that occurred. Whether its from the network or database.
         *                          In memory sources should never call this method.
         */
        void onFailure(DataResponseError dataResponseError);
    }

    /**
     * Description: Represents a successful execution.
     */
    public interface Success<TResponse> {

        /**
         * Called when a response succeeds. This is called once for EVERY {@link DataSource}
         * registered in this {@link DataController}.
         *
         * @param response The response from a successful callback.
         */
        void onSuccess(DataControllerResponse<TResponse> response);
    }

    /**
     * The main callback interface for getting callbacks on the the {@link DataController} class.
     *
     * @param <TResponse>
     */
    public interface DataControllerCallback<TResponse> extends Error, Success<TResponse> {
    }

    private final DataControllerCallbackGroup<T> callbackGroup
            = new DataControllerCallbackGroup<>();

    private final DataSourceContainer<T> dataSourceContainer;

    final DataSourceChainer<T> dataSourceChainer;

    DataController(Builder<T> builder) {
        if (builder.dataSourceContainer != null) {
            dataSourceContainer = builder.dataSourceContainer;
        } else {
            dataSourceContainer = new TreeMapSingleTypeDataSourceContainer<>();
        }

        if (builder.dataSources.isEmpty()) {
            throw new IllegalStateException("You need to register at least one DataSource for this DataController");
        }

        for (DataSource<T> dataSource : builder.dataSources) {
            dataSourceContainer.registerDataSource(dataSource);
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
        Collection<DataSource<T>> sourceCollection = dataSourceContainer.sources();
        for (DataSource<T> source : sourceCollection) {
            source.cancel();
        }
    }

    public void cancel(DataSourceContainer.DataSourceParams dataSourceParams) {
        DataSource<T> dataSource = dataSourceContainer.getDataSource(dataSourceParams);
        dataSource.cancel();
    }

    public void registerForCallbacks(DataControllerCallback<T> callback) {
        callbackGroup.registerForCallbacks(callback);
    }

    public void deregisterForCallbacks(DataControllerCallback<T> callback) {
        callbackGroup.deregisterForCallbacks(callback);
    }

    public void clearCallbacks() {
        callbackGroup.clearCallbacks();
    }

    public boolean hasCallbacks() {
        return callbackGroup.hasCallbacks();
    }

    public DataControllerRequest.Builder<T> request() {
        return new DataControllerRequest.Builder<>(this);
    }

    public DataControllerRequest.Builder<T> request(DataSourceContainer.DataSourceParams params) {
        DataSource<T> dataSource = dataSourceContainer.getDataSource(params);
        List<DataSource<T>> list = new ArrayList<>();
        list.add(dataSource);
        return new DataControllerRequest.Builder<>(this)
                .addSourceTargets(list);
    }

    public Collection<DataSource<T>> dataSources() {
        return dataSourceContainer.sources();
    }

    public DataSource<T> getDataSource(DataSourceContainer.DataSourceParams dataSourceParams) {
        return dataSourceContainer.getDataSource(dataSourceParams);
    }

    void onSuccess(DataControllerResponse<T> response) {
        callbackGroup.onSuccess(response);
    }

    void onFailure(DataResponseError dataResponseError) {
        callbackGroup.onFailure(dataResponseError);
    }

    public static final class Builder<T> {

        private List<DataSource<T>> dataSources = new ArrayList<>();

        private DataSourceContainer<T> dataSourceContainer;

        private DataSourceChainer<T> dataSourceChainer;

        public Builder<T> dataSource(DataSource<T> dataSource) {
            dataSources.add(dataSource);
            return this;
        }

        public Builder<T> dataSourceStorage(DataSourceContainer<T> dataSourceContainer) {
            this.dataSourceContainer = dataSourceContainer;
            return this;
        }

        public Builder<T> dataSourceChainer(DataSourceChainer<T> dataSourceChainer) {
            this.dataSourceChainer = dataSourceChainer;
            return this;
        }

        public DataController<T> build() {
            return new DataController<>(this);
        }
    }
}
