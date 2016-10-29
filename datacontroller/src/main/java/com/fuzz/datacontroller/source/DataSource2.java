package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;

/**
 * Description:
 *
 * @author Andrew Grosner (fuzz)
 */

public class DataSource2<T> {

    public interface DataSourceStorage<T> {

        void store(DataControllerResponse<T> response);

        T getStoredData(DataSource.SourceParams params);

        void clearStoredData(DataSource.SourceParams params);

        boolean hasStoredData(DataSource.SourceParams params);
    }


    public interface DataSourceCaller<T> {
        void get(DataSource.SourceParams sourceParams,
                 DataController.Success<T> success,
                 DataController.Error error);

        void cancel();
    }

    /**
     * Description: A simple interface for determining when data should get refreshed. If the call
     * returns a false, a call to {@link #get(DataSource.SourceParams, DataController.Success, DataController.Error)}
     * does not do anything.
     */
    public interface RefreshStrategy<TResponse> {

        /**
         * @param dataSource The data source that we're calling.
         * @return True if we should refresh by calling {@link #get(DataSource.SourceParams,
         * DataController.Success, DataController.Error)}.
         * If false, we do not refresh data.
         */
        boolean shouldRefresh(DataSource2<TResponse> dataSource);
    }

    private final Object syncLock = new Object();

    private boolean isBusy = false;

    private final DataSourceCaller<T> caller;
    private final DataSource.SourceType sourceType;

    private final DataSourceStorage<T> storage;
    private final RefreshStrategy<T> refreshStrategy;
    private final DataSource.SourceParams defaultParams;


    DataSource2(Builder<T> builder) {
        this.caller = builder.caller;
        this.sourceType = builder.sourceType;
        this.storage = builder.storage;
        this.refreshStrategy = builder.refreshStrategy;
        this.defaultParams = builder.defaultParams;
    }

    public RefreshStrategy<T> refreshStrategy() {
        return refreshStrategy;
    }


    public T getStoredData() {
        return storage.getStoredData(defaultParams);
    }

    public T getStoredData(DataSource.SourceParams sourceParams) {
        return storage.getStoredData(sourceParams);
    }

    public boolean hasStoredData() {
        return storage.hasStoredData(defaultParams);
    }

    public boolean hasStoredData(DataSource.SourceParams params) {
        return storage.hasStoredData(params);
    }

    public void clearStoredData() {
        storage.clearStoredData(defaultParams);
    }

    public void clearStoredData(DataSource.SourceParams sourceParams) {
        storage.clearStoredData(sourceParams);
    }

    /**
     * Calls {@link DataSourceStorage#store(DataControllerResponse)}
     * only if the {@link DataControllerResponse#getSourceType()} is different.
     * i.e comes from a different source.
     *
     * @param tResponse The response returned here.
     */
    public final void store(DataControllerResponse<T> tResponse) {
        if (!tResponse.getSourceType().equals(sourceType)) {
            storage.store(tResponse);
        }
    }

    /**
     * Requests a call on the underlying data to return on the specified success and error callbacks. This
     * respects the {@link RefreshStrategy} set in the constructor of this source.
     * It is explicitly up to the source on what kinds of parameters it can handle or expect. The expectation
     * is that if no params returned or it is a default instance, all data should be returned.
     *
     * @param sourceParams The params used to retrieve information from the {@link DataSource}.
     * @param success      Called when a successful request returns.
     * @param error        Called when a request fails.
     */
    public final void get(DataSource.SourceParams sourceParams, DataController.Success<T> success,
                          DataController.Error error) {
        if (refreshStrategy.shouldRefresh(this) && !isBusy()) {
            setBusy(true);
            caller.get(sourceParams, wrapBusySuccess(success), wrapBusyError(error));
        }
    }

    private void setBusy(boolean isBusy) {
        synchronized (syncLock) {
            this.isBusy = isBusy;
        }
    }

    public boolean isBusy() {
        synchronized (syncLock) {
            return isBusy;
        }
    }

    /**
     * @return convenience method designed to communicate busy state completion.
     */
    private DataController.Error wrapBusyError(final DataController.Error error) {
        return new DataController.Error() {
            @Override
            public void onFailure(DataResponseError dataResponseError) {
                setBusy(false);
                error.onFailure(dataResponseError);
            }
        };
    }

    /**
     * @return convenience method designed to communicate busy state completion.
     */
    private DataController.Success<T> wrapBusySuccess(
            final DataController.Success<T> success) {
        return new DataController.Success<T>() {
            @Override
            public void onSuccess(DataControllerResponse<T> response) {
                setBusy(false);
                success.onSuccess(response);
            }
        };
    }

    public static final class Builder<T> {

        private final DataSourceCaller<T> caller;
        private final DataSource.SourceType sourceType;

        private DataSourceStorage<T> storage;
        private RefreshStrategy<T> refreshStrategy;

        private DataSource.SourceParams defaultParams = DataSource.SourceParams.defaultParams;

        public Builder(DataSourceCaller<T> caller,
                       DataSource.SourceType sourceType) {
            this.caller = caller;
            this.sourceType = sourceType;
        }

        public Builder<T> storage(DataSourceStorage<T> storage) {
            this.storage = storage;
            return this;
        }

        public Builder<T> refreshStrategy(RefreshStrategy<T> strategy) {
            this.refreshStrategy = strategy;
            return this;
        }

        public Builder<T> defaultParams(DataSource.SourceParams defaultParams) {
            this.defaultParams = defaultParams;
            return this;
        }

        public DataSource2<T> build() {
            return new DataSource2<>(this);
        }
    }
}
