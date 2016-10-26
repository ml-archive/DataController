package com.fuzz.datacontroller;

import com.fuzz.datacontroller.source.DataSource;
import com.sun.istack.internal.NotNull;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */

public class DataControllerRequest<T> {

    private final List<DataSource<T>> dataSources;

    private final DataSourceChainer<T> dataSourceChainer;

    private DataSource.SourceParams sourceParams;

    private final DataControllerCallbackGroup<T> callbackGroup
            = new DataControllerCallbackGroup<>();

    private final DataResponseError.ErrorFilter errorFilter;

    DataControllerRequest(Builder<T> builder) {
        this.dataSources = builder.dataSources;
        this.dataSourceChainer = builder.dataSourceChainer;
        this.sourceParams = builder.sourceParams;
        for (DataController.DataControllerCallback<T> callback : builder.callbackGroup) {
            callbackGroup.registerForCallbacks(callback);
        }
        this.errorFilter = builder.errorFilter;
    }

    public void execute() {
        for (int i = 0; i < dataSources.size(); i++) {
            DataSource<T> source = dataSources.get(i);
            if (i == 0 || dataSourceChainer.shouldQueryNext(dataSources.get(i - 1), source)) {
                source.get(sourceParams, internalSuccessCallback, internalErrorCallback);
            }
        }
    }

    public void deregister(DataController.DataControllerCallback<T> dataControllerCallback) {
        callbackGroup.deregisterForCallbacks(dataControllerCallback);
    }

    public void clearCallbacks() {
        callbackGroup.clearCallbacks();
    }

    private final DataController.Success<T> internalSuccessCallback = new DataController.Success<T>() {
        @Override
        public void onSuccess(DataControllerResponse<T> response) {
            for (DataSource<T> dataSource : dataSources) {
                dataSource.store(response);
            }

            callbackGroup.onSuccess(response);
        }
    };

    private final DataController.Error internalErrorCallback = new DataController.Error() {
        @Override
        public void onFailure(DataResponseError dataResponseError) {
            callbackGroup.onFailure(errorFilter != null ?
                    errorFilter.filter(dataResponseError) : dataResponseError);
        }
    };

    public static final class Builder<T> {

        private final Set<DataController.DataControllerCallback<T>> callbackGroup
                = new LinkedHashSet<>();

        @NotNull
        private final List<DataSource<T>> dataSources;

        private final DataSourceChainer<T> dataSourceChainer;

        private DataSource.SourceParams sourceParams;

        private DataResponseError.ErrorFilter errorFilter;

        public Builder(List<DataSource<T>> dataSources,
                       DataSourceChainer<T> dataSourceChainer) {
            this.dataSources = dataSources;
            this.dataSourceChainer = dataSourceChainer;
        }

        public Builder<T> sourceParams(DataSource.SourceParams sourceParams) {
            this.sourceParams = sourceParams;
            return this;
        }

        public Builder<T> errorFilter(DataResponseError.ErrorFilter errorFilter) {
            this.errorFilter = errorFilter;
            return this;
        }

        public Builder<T> register(DataController.DataControllerCallback<T> callback) {
            this.callbackGroup.add(callback);
            return this;
        }

        public DataControllerRequest<T> build() {
            return new DataControllerRequest<>(this);
        }
    }
}
