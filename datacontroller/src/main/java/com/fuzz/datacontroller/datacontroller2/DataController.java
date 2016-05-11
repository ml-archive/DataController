package com.fuzz.datacontroller.datacontroller2;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;
import com.fuzz.datacontroller.datacontroller2.source.DataSource.SourceType;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

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

    public interface DataControllerCallback<TResponse> extends Error, Success<TResponse> {
    }

    private final Map<SourceType, DataSource<TResponse>>
            dataSourceMap = new TreeMap<>();

    private final DataControllerCallbackGroup<TResponse> callbackGroup = new DataControllerCallbackGroup<>();

    public void registerDataSource(SourceType sourceType,
                                   DataSource<TResponse> dataSource) {
        synchronized (dataSourceMap) {
            dataSourceMap.put(sourceType, dataSource);
        }
    }

    public void deregisterDataSource(SourceType sourceType) {
        synchronized (dataSourceMap) {
            dataSourceMap.remove(sourceType);
        }
    }

    public void registerForCallbacks(DataControllerCallback<TResponse> dataControllerCallback) {
        callbackGroup.registerForCallbacks(dataControllerCallback);
    }

    public void deregisterForCallbacks(DataControllerCallback<TResponse> dataControllerCallback) {
        callbackGroup.deregisterForCallbacks(dataControllerCallback);
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
        synchronized (dataSourceMap) {
            Collection<DataSource<TResponse>> sourceCollection = dataSourceMap.values();
            for (DataSource<TResponse> source : sourceCollection) {
                source.get(sourceParams, internalSuccessCallback, internalErrorCallback);
            }
        }
    }

    /**
     * Requests a specific source with specified params.
     *
     * @param sourceType   The type of source to request via {@link SourceType}
     * @param sourceParams The params used in the request.
     */
    public void requestSpecific(SourceType sourceType, DataSource.SourceParams sourceParams) {
        DataSource<TResponse> dataSource = dataSourceMap.get(sourceType);
        if (dataSource == null) {
            throw new RuntimeException("No data source found for type: " + sourceType);
        }

        dataSource.get(sourceParams, internalSuccessCallback, internalErrorCallback);
    }


    private final Success<TResponse> internalSuccessCallback = new Success<TResponse>() {
        @Override
        public void onSuccess(DataControllerResponse<TResponse> response) {
            dataSourceMap.get(response.getSourceType()).store(response.getResponse());

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
