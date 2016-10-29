package com.fuzz.datacontroller.source;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerRequest;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;

/**
 * Description: Provides a source of where information comes from.
 */
public abstract class DataSource<TResponse> {

    /**
     * Describes where this {@link DataSource} information came from. This is especially useful
     * when distinguishing whether information was loaded from network or storage.
     */
    public enum SourceType {

        /**
         * Retrieved from memory.
         */
        MEMORY,

        /**
         * Retrieved from disk.
         */
        DISK,

        /**
         * Retrieved from network.
         */
        NETWORK
    }

    /**
     * SourceParams provide the base class for all information passing between
     * caller {@link DataControllerRequest} and receiver {@link DataSource}.
     * <p></p>
     * Some {@link DataSource} require more information that this base class cannot represent. It
     * is up to them to provide the kind of params they expect.
     */
    public static class SourceParams {

        public static final SourceParams defaultParams = new SourceParams();

        /**
         * an optional index to use. -1 is default, meaning we should retrieve all information.
         */
        public int index = -1;

        /**
         * Data in this class.
         */
        public Object data;
    }

    private final Object syncLock = new Object();

    private boolean isBusy = false;

    private final DataSource2.RefreshStrategy<TResponse> refreshStrategy;

    public DataSource(DataSource2.RefreshStrategy<TResponse> refreshStrategy) {
        this.refreshStrategy = refreshStrategy;
    }

    public DataSource() {
        this(new DataSource2.RefreshStrategy<TResponse>() {
            @Override
            public boolean shouldRefresh(DataSource<TResponse> dataSource) {
                return true;
            }
        });
    }

    public DataSource2.RefreshStrategy<TResponse> getRefreshStrategy() {
        return refreshStrategy;
    }

    /**
     * Queries this {@link DataSource} for data stored. This potentially can be expensive since
     * if this is a {@link SourceType#DISK}, it will perform an IO operation on the calling thread.
     * This method is useful for retrieving data in same thread, but should be avoided unless absolutely
     * necessary. Prefer using the {@link #get(SourceParams, DataController.Success, DataController.Error)} method.
     *
     * @param sourceParams The set of params to query data from. Its up to this {@link DataSource} to handle the values.
     */
    public TResponse getStoredData(SourceParams sourceParams) {
        return null;
    }

    /**
     * Performs {@link #getStoredData(SourceParams)} with default params.
     */
    public final TResponse getStoredData() {
        return getStoredData(SourceParams.defaultParams);
    }

    /**
     * @return true if stored data exists. this is determined by nullability. Override for other kinds
     * of checks.
     */
    public boolean hasStoredData() {
        return getStoredData() != null;
    }

    /**
     * Clears out stored data. Will either delete its db instance, clear out memory,
     * or erase something on disk.
     */
    public void clearStoredData() {
        clearStoredData(SourceParams.defaultParams);
    }

    /**
     * Clears out stored data. Will either delete its db instance, clear out memory,
     * or erase something on disk.
     */
    public void clearStoredData(SourceParams sourceParams) {

    }

    /**
     * Calls {@link #doStore(DataControllerResponse)}
     * only if the {@link DataControllerResponse#getSourceType()} is different.
     * i.e comes from a different source.
     *
     * @param tResponse The response returned here.
     */
    public final void store(DataControllerResponse<TResponse> tResponse) {
        if (!tResponse.getSourceType().equals(getSourceType())) {
            doStore(tResponse);
        }
    }

    /**
     * Requests a call on the underlying data to return on the specified success and error callbacks. This
     * respects the {@link DataSource2.RefreshStrategy} set in the constructor of this source.
     * It is explicitly up to the source on what kinds of parameters it can handle or expect. The expectation
     * is that if no params returned or it is a default instance, all data should be returned.
     *
     * @param sourceParams The params used to retrieve information from the {@link DataSource}.
     * @param success      Called when a successful request returns.
     * @param error        Called when a request fails.
     */
    public final void get(SourceParams sourceParams, DataController.Success<TResponse> success,
                          DataController.Error error) {
        if (getRefreshStrategy().shouldRefresh(this) && !isBusy()) {
            setBusy(true);
            doGet(sourceParams, wrapBusySuccess(success), wrapBusyError(error));
        }
    }

    /**
     * Attempts to cancel any pending asynchronous operation on this {@link DataSource}.
     */
    public abstract void cancel();

    /**
     * Perform the actual information retrieval here. This might call a network, database, or file-based system.
     * Anything that is IO should be done on a separate thread. It is also up to the {@link DataSource}
     * to ensure that both success and error are properly called.
     * <p></p>
     * Also, it is up to the implementation of this method to provide a custom set
     * of {@link SourceParams} if needed, so it can retrieve the specific information it might
     * need to get.
     *
     * @param sourceParams The params used to retrieve information from the {@link DataSource}.
     * @param success      Called when a successful request returns.
     * @param error        Called when a request fails.
     */
    protected abstract void doGet(SourceParams sourceParams,
                                  DataController.Success<TResponse> success,
                                  DataController.Error error);

    /**
     * Perform the actual information storage here. This might call a network, database, or file-based system.
     * Anything that is IO should be done on a separate thread to prevent blocking.
     *
     * @param dataControllerResponse The response to store.
     */
    protected abstract void doStore(DataControllerResponse<TResponse> dataControllerResponse);

    /**
     * @return The kind of source, i.e. where it comes from. Must be defined.
     */
    public abstract SourceType getSourceType();

    protected void setBusy(boolean isBusy) {
        synchronized (syncLock) {
            this.isBusy = isBusy;
        }
    }

    protected boolean isBusy() {
        synchronized (syncLock) {
            return isBusy;
        }
    }

    /**
     * @return convenience method designed to communicate busy state completion.
     */
    protected DataController.Error wrapBusyError(final DataController.Error error) {
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
    protected DataController.Success<TResponse> wrapBusySuccess(
            final DataController.Success<TResponse> success) {
        return new DataController.Success<TResponse>() {
            @Override
            public void onSuccess(DataControllerResponse<TResponse> response) {
                setBusy(false);
                success.onSuccess(response);
            }
        };
    }

}
