package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceContainer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Description: Runs whenever we request data from a {@link DataController}. This class executes
 * every time to {@link #execute()}, meaning we can cache this for subsequent requests if we'd like.
 *
 * @author Andrew Grosner (Fuzz)
 */
public class DataControllerRequest<T> {


    public interface ErrorFilter {

        DataResponseError filter(DataResponseError dataResponseError);
    }

    public interface SuccessFilter<T> {

        DataControllerResponse<T> filter(DataControllerResponse<T> response);
    }

    private final DataController<T> dataController;
    private final DataSource.SourceParams sourceParams;

    private final Set<DataSourceContainer.DataSourceParams> targetedRequestSources;
    private final Set<DataSourceContainer.DataSourceParams> targetedStorageSources;

    private final Map<DataSourceContainer.DataSourceParams, DataSource.SourceParams>
            targetParamsMap;


    private final DataControllerCallbackGroup<T> callbackGroup
            = new DataControllerCallbackGroup<>();

    private final ErrorFilter errorFilter;
    private final SuccessFilter<T> successFilter;

    DataControllerRequest(Builder<T> builder) {
        this.targetedRequestSources = builder.targetedRequestSources;
        this.targetedStorageSources = builder.targetedStorageSources;
        this.dataController = builder.dataController;
        this.sourceParams = builder.sourceParams;
        this.targetParamsMap = builder.targetParamsMap;
        this.successFilter = builder.successFilter;
        for (DataController.DataControllerCallback<T> callback : builder.callbackGroup) {
            callbackGroup.registerForCallbacks(callback);
        }
        this.errorFilter = builder.errorFilter;
    }

    /**
     * Executes the request. Subsequent calls will also execute unless the {@link DataSource}
     * is busy, its {@link DataSource.RefreshStrategy} prevents it, or the {@link DataSourceChainer}.
     */
    public void execute() {
        if (targetedRequestSources.isEmpty()) {
            List<DataSource<T>> dataSources = new ArrayList<>(requestSources());
            for (int i = 0; i < dataSources.size(); i++) {
                DataSource<T> source = dataSources.get(i);
                if (i == 0 || dataController.dataSourceChainer.shouldQueryNext(dataSources.get(i - 1), source)) {
                    source.get(sourceParams, internalSuccessCallback, internalErrorCallback);
                }
            }
        } else {
            int index = 0;
            List<DataSourceContainer.DataSourceParams> dataSourceParams = new ArrayList<>(targetedRequestSources);
            for (DataSourceContainer.DataSourceParams params : dataSourceParams) {
                DataSource<T> source = dataController.getDataSource(params);
                if (index == 0 || dataController.dataSourceChainer.shouldQueryNext(
                        dataController.getDataSource(dataSourceParams.get(index - 1)), source)) {
                    source.get(targetParamsMap.get(params), internalSuccessCallback, internalErrorCallback);
                }
                index++;
            }
        }
    }

    /**
     * Deregister a callback from this {@link DataControllerRequest}.
     */
    public void deregister(DataController.DataControllerCallback<T> dataControllerCallback) {
        callbackGroup.deregisterForCallbacks(dataControllerCallback);
    }

    public void clearCallbacks() {
        callbackGroup.clearCallbacks();
    }

    /**
     * @return true if we have attached any specific request callbacks here.
     */
    public boolean hasRequestCallbacks() {
        return callbackGroup.hasCallbacks();
    }

    /**
     * @return true if we have {@link #hasRequestCallbacks()} or the corresponding {@link DataController}
     * has callbacks.
     */
    public boolean hasCallbacks() {
        return callbackGroup.hasCallbacks() || dataController.hasCallbacks();
    }

    private Collection<DataSource<T>> requestSources() {
        if (!targetedRequestSources.isEmpty()) {
            List<DataSource<T>> sources = new ArrayList<>();
            for (DataSourceContainer.DataSourceParams params : targetedRequestSources) {
                sources.add(dataController.getDataSource(params));
            }
            return sources;
        } else {
            return dataController.dataSources();
        }
    }

    private Collection<DataSource<T>> responseSources() {
        if (!targetedStorageSources.isEmpty()) {
            List<DataSource<T>> sources = new ArrayList<>();
            for (DataSourceContainer.DataSourceParams params : targetedStorageSources) {
                sources.add(dataController.getDataSource(params));
            }
            return sources;
        } else {
            return dataController.dataSources();
        }
    }


    private final DataController.Success<T> internalSuccessCallback = new DataController.Success<T>() {
        @Override
        public void onSuccess(DataControllerResponse<T> response) {
            DataControllerResponse<T> dataControllerResponse = successFilter != null ?
                    successFilter.filter(response) : response;

            Collection<DataSource<T>> sources = responseSources();
            for (DataSource<T> dataSource : sources) {
                dataSource.store(dataControllerResponse);
            }

            callbackGroup.onSuccess(dataControllerResponse);
            dataController.onSuccess(dataControllerResponse);
        }
    };

    private final DataController.Error internalErrorCallback = new DataController.Error() {
        @Override
        public void onFailure(DataResponseError dataResponseError) {
            DataResponseError error = errorFilter != null ?
                    errorFilter.filter(dataResponseError) : dataResponseError;
            callbackGroup.onFailure(error);
            dataController.onFailure(error);
        }
    };

    public static final class Builder<T> {

        private final Set<DataController.DataControllerCallback<T>> callbackGroup
                = new LinkedHashSet<>();

        private final Set<DataSourceContainer.DataSourceParams> targetedRequestSources
                = new LinkedHashSet<>();

        private final Set<DataSourceContainer.DataSourceParams> targetedStorageSources
                = new LinkedHashSet<>();

        private final DataController<T> dataController;

        private DataSource.SourceParams sourceParams = DataSource.SourceParams.defaultParams;

        private final Map<DataSourceContainer.DataSourceParams, DataSource.SourceParams> targetParamsMap
                = new HashMap<>();

        private ErrorFilter errorFilter;
        private SuccessFilter<T> successFilter;

        Builder(DataController<T> dataController) {
            this.dataController = dataController;
        }

        /**
         * Appends a specific source param for a {@link DataSource} to call when executing request.
         */
        public Builder<T> sourceParamsForTarget(DataSourceContainer.DataSourceParams dataSourceParams,
                                                DataSource.SourceParams sourceParams) {
            targetParamsMap.put(dataSourceParams, sourceParams);

            if (!targetedRequestSources.contains(dataSourceParams)) {
                addRequestSourceTarget(dataSourceParams);
            }
            return this;
        }

        public Builder<T> sourceParams(DataSource.SourceParams sourceParams) {
            this.sourceParams = sourceParams;
            return this;
        }

        /**
         * Specifies a way to filter and mutate {@link DataResponseError} when passed
         * through a {@link DataController.Error} callback.
         */
        public Builder<T> errorFilter(ErrorFilter errorFilter) {
            this.errorFilter = errorFilter;
            return this;
        }

        /**
         * Specifies a way to filter and mutate a {@link DataControllerResponse} when passed
         * through a {@link DataController.Success} callback.
         */
        public Builder<T> successFilter(SuccessFilter<T> successFilter) {
            this.successFilter = successFilter;
            return this;
        }


        /**
         * Adds a {@link DataSource} that we wish to query from directly. Specifying at least one direct
         * target means that we won't target any of the {@link DataController} sources.
         */
        public Builder<T> addRequestSourceTarget(DataSourceContainer.DataSourceParams dataSource) {
            this.targetedRequestSources.add(dataSource);
            return this;
        }

        /**
         * Adds a group of {@link DataSource} we wish to query from.
         */
        public Builder<T> addRequestSourceTargets(Collection<DataSourceContainer.DataSourceParams> dataSource) {
            this.targetedRequestSources.addAll(dataSource);
            return this;
        }


        /**
         * Adds a {@link DataSource} that we wish receives {@link DataSource#store(DataControllerResponse)}.
         * Specifying at least one direct target means that we won't target any of the {@link DataController} sources
         * when we receive a response from another {@link DataSource}.
         */
        public Builder<T> addStorageSourceTarget(DataSourceContainer.DataSourceParams dataSource) {
            this.targetedRequestSources.add(dataSource);
            return this;
        }

        /**
         * Adds a group of {@link DataSource} we wish to query from.
         */
        public Builder<T> addStorageSourceTargets(Collection<DataSourceContainer.DataSourceParams> dataSource) {
            this.targetedRequestSources.addAll(dataSource);
            return this;
        }

        /**
         * Registers a callback only used in this request.
         *
         * @param callback The callback used.
         */
        public Builder<T> register(DataController.DataControllerCallback<T> callback) {
            this.callbackGroup.add(callback);
            return this;
        }

        public DataControllerRequest<T> build() {
            return new DataControllerRequest<>(this);
        }
    }
}
