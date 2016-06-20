package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSource.SourceType;
import com.fuzz.datacontroller.source.DataSourceStorage;
import com.fuzz.datacontroller.source.TreeMapSingleTypeDataSourceContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Description: Provides basic implementation of a data controller.
 */
public class DataController<TResponse> {

    /**
     * Description: Represents a failed callback response.
     */
    public interface Error {

        void onFailure(DataResponseError dataResponseError);
    }

    /**
     * Description: Represents a successful execution.
     */
    public interface Success<TResponse> {

        void onSuccess(DataControllerResponse<TResponse> response);
    }

    /**
     * The main callback interface for getting callbacks on the the {@link DataController} class.
     *
     * @param <TResponse>
     */
    public interface DataControllerCallback<TResponse> extends Error, Success<TResponse> {
    }

    private final DataSourceStorage<TResponse> dataSourceStorage;

    private final DataControllerCallbackGroup<TResponse> callbackGroup
            = new DataControllerCallbackGroup<>();

    private final DataSourceChainer<TResponse> dataSourceChainer;

    public DataController(DataSourceStorage<TResponse> dataSourceStorage, DataSourceChainer<TResponse> dataSourceChainer) {
        this.dataSourceStorage = dataSourceStorage;
        this.dataSourceChainer = dataSourceChainer;
    }

    public DataController() {
        this(new TreeMapSingleTypeDataSourceContainer<TResponse>(),
                new DataSourceChainer<TResponse>() {
                    @Override
                    public boolean shouldQueryNext(DataSource<TResponse> lastSource, DataSource<TResponse> sourceToChain) {
                        return true;
                    }
                });
    }

    public void registerDataSource(DataSource<TResponse> dataSource) {
        dataSourceStorage.registerDataSource(dataSource);
    }

    public void deregisterDataSource(DataSource<TResponse> dataSource) {
        dataSourceStorage.deregisterDataSource(dataSource);
    }

    public void registerForCallbacks(DataControllerCallback<TResponse> dataControllerCallback) {
        callbackGroup.registerForCallbacks(dataControllerCallback);
    }

    public void deregisterForCallbacks(DataControllerCallback<TResponse> dataControllerCallback) {
        callbackGroup.deregisterForCallbacks(dataControllerCallback);
    }

    public void clearCallbacks() {
        callbackGroup.clearCallbacks();
    }

    /**
     * Requests data with default parameters.
     */
    public void requestData() {
        requestData(new DataSource.SourceParams());
    }

    /**
     * Requests data from each of the {@link DataSource} here, passing in a sourceParams object.
     * It will iterate through all sources and call each one.
     *
     * @param sourceParams The params to use for a query.
     */
    public void requestData(DataSource.SourceParams sourceParams) {
        List<DataSource<TResponse>> sourceCollection = getSources();
        for (int i = 0; i < sourceCollection.size(); i++) {
            DataSource<TResponse> source = sourceCollection.get(i);
            if (i == 0 || dataSourceChainer.shouldQueryNext(sourceCollection.get(i - 1), source)) {
                source.get(sourceParams, internalSuccessCallback, internalErrorCallback);
            }
        }
    }

    /**
     * Requests a specific source with specified params.
     *
     * @param dataSourceParams The type of source to request via {@link SourceType}
     * @param sourceParams     The params used in the request.
     */
    public void requestSpecific(DataSourceStorage.DataSourceParams dataSourceParams,
                                DataSource.SourceParams sourceParams) {
        DataSource<TResponse> dataSource = dataSourceStorage.getDataSource(dataSourceParams);
        dataSource.get(sourceParams, internalSuccessCallback, internalErrorCallback);
    }

    /**
     * Cancels all attached {@link DataSource}.
     */
    public void cancel() {
        Collection<DataSource<TResponse>> sourceCollection = dataSourceStorage.sources();
        for (DataSource<TResponse> source : sourceCollection) {
            source.cancel();
        }
    }

    /**
     * @return True if we have associated callbacks on this DC.
     */
    public boolean hasCallbacks() {
        return callbackGroup.hasCallbacks();
    }

    public List<DataSource<TResponse>> getSources() {
        return new ArrayList<>(dataSourceStorage.sources());
    }

    public DataSource<TResponse> getSource(DataSourceStorage.DataSourceParams sourceParams) {
        return dataSourceStorage.getDataSource(sourceParams);
    }


    private final Success<TResponse> internalSuccessCallback = new Success<TResponse>() {
        @Override
        public void onSuccess(DataControllerResponse<TResponse> response) {
            Collection<DataSource<TResponse>> dataSources = dataSourceStorage.sources();
            for (DataSource<TResponse> dataSource : dataSources) {
                dataSource.store(response);
            }

            callbackGroup.onSuccess(response);
        }
    };

    private final Error internalErrorCallback = new Error() {
        @Override
        public void onFailure(DataResponseError dataResponseError) {
            callbackGroup.onFailure(dataResponseError);
        }
    };


}
