package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */

public class DataControllerRequest<T> {

    private final DataController2<T> dataController;
    private DataSource.SourceParams sourceParams;

    private Set<DataSource<T>> targetedSources = new LinkedHashSet<>();

    private final DataControllerCallbackGroup<T> callbackGroup
            = new DataControllerCallbackGroup<>();

    private final DataResponseError.ErrorFilter errorFilter;

    DataControllerRequest(Builder<T> builder) {
        this.targetedSources = builder.targetedSources;
        this.dataController = builder.dataController;
        this.sourceParams = builder.sourceParams;
        for (DataController2.DataControllerCallback<T> callback : builder.callbackGroup) {
            callbackGroup.registerForCallbacks(callback);
        }
        this.errorFilter = builder.errorFilter;
    }

    /**
     * Executes the request. Subsequent calls will also execute unless the {@link DataSource}
     * is busy, its {@link DataSource.RefreshStrategy} prevents it, or the {@link DataSourceChainer}.
     */
    public void execute() {
        List<DataSource<T>> dataSources = new ArrayList<>(sources());
        for (int i = 0; i < dataSources.size(); i++) {
            DataSource<T> source = dataSources.get(i);
            if (i == 0 || dataController.dataSourceChainer.shouldQueryNext(dataSources.get(i - 1), source)) {
                source.get(sourceParams, internalSuccessCallback, internalErrorCallback);
            }
        }
    }

    /**
     * Deregister a callback from this {@link DataControllerRequest}.
     */
    public void deregister(DataController2.DataControllerCallback<T> dataControllerCallback) {
        callbackGroup.deregisterForCallbacks(dataControllerCallback);
    }

    public void clearCallbacks() {
        callbackGroup.clearCallbacks();
    }

    private Collection<DataSource<T>> sources() {
        if (!targetedSources.isEmpty()) {
            return targetedSources;
        } else {
            return dataController.dataSources();
        }
    }

    private final DataController2.Success<T> internalSuccessCallback = new DataController2.Success<T>() {
        @Override
        public void onSuccess(DataControllerResponse<T> response) {
            Collection<DataSource<T>> sources = sources();
            for (DataSource<T> dataSource : sources) {
                dataSource.store(response);
            }

            callbackGroup.onSuccess(response);
            dataController.onSuccess(response);
        }
    };

    private final DataController2.Error internalErrorCallback = new DataController2.Error() {
        @Override
        public void onFailure(DataResponseError dataResponseError) {
            DataResponseError error = errorFilter != null ?
                    errorFilter.filter(dataResponseError) : dataResponseError;
            callbackGroup.onFailure(error);
            dataController.onFailure(error);
        }
    };

    public static final class Builder<T> {

        private final Set<DataController2.DataControllerCallback<T>> callbackGroup
                = new LinkedHashSet<>();

        private final Set<DataSource<T>> targetedSources = new LinkedHashSet<>();

        private final DataController2<T> dataController;

        private DataSource.SourceParams sourceParams = new DataSource.SourceParams();

        private DataResponseError.ErrorFilter errorFilter;

        Builder(DataController2<T> dataController) {
            this.dataController = dataController;
        }

        public Builder<T> sourceParams(DataSource.SourceParams sourceParams) {
            this.sourceParams = sourceParams;
            return this;
        }

        public Builder<T> errorFilter(DataResponseError.ErrorFilter errorFilter) {
            this.errorFilter = errorFilter;
            return this;
        }

        /**
         * Adds a {@link DataSource} that we wish to query from directly. Specifying at least one direct
         * target means that we won't target any of the {@link DataController2} sources.
         */
        public Builder<T> addSourceTarget(DataSource<T> dataSource) {
            this.targetedSources.add(dataSource);
            return this;
        }

        /**
         * Adds a group of {@link DataSource} we wish to query from.
         */
        public Builder<T> addSourceTargets(Collection<DataSource<T>> dataSource) {
            this.targetedSources.addAll(dataSource);
            return this;
        }

        /**
         * Registers a callback only used in this request.
         *
         * @param callback The callback used.
         */
        public Builder<T> register(DataController2.DataControllerCallback<T> callback) {
            this.callbackGroup.add(callback);
            return this;
        }

        public DataControllerRequest<T> build() {
            return new DataControllerRequest<>(this);
        }
    }
}
