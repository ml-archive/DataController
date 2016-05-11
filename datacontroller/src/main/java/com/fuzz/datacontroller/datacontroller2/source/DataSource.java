package com.fuzz.datacontroller.datacontroller2.source;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;

/**
 * Description: Provides a source of where information comes from.
 */
public abstract class DataSource<TResponse> {


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

    public static class SourceParams {

        /**
         * an optional index to use. -1 is default, meaning we should retrieve all information.
         */
        public int index = -1;

        /**
         * Data in this class.
         */
        public Object data;
    }

    /**
     * Description: A simple interface for determining when data should get refreshed. If the call
     * returns a false, a call to {@link #get(SourceParams, DataController.Success, DataController.Error)}
     * does not do anything.
     */
    public interface RefreshStrategy<TResponse> {

        /**
         * @param dataSource The data source that we're calling.
         * @return True if we should refresh by calling {@link #get(SourceParams, DataController.Success, DataController.Error)}.
         * If false, we do not refresh data.
         */
        boolean shouldRefresh(DataSource<TResponse> dataSource);
    }

    private final RefreshStrategy<TResponse> refreshStrategy;

    public DataSource(RefreshStrategy<TResponse> refreshStrategy) {
        this.refreshStrategy = refreshStrategy;
    }

    public DataSource() {
        this(new RefreshStrategy<TResponse>() {
            @Override
            public boolean shouldRefresh(DataSource<TResponse> dataSource) {
                return true;
            }
        });
    }


    public final RefreshStrategy<TResponse> getRefreshStrategy() {
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
        return getStoredData(new SourceParams());
    }

    /**
     * Stores a response.
     *
     * @param tResponse
     */
    public final void store(DataControllerResponse<TResponse> tResponse) {
        if (!tResponse.getSourceType().equals(getSourceType())) {
            doStore(tResponse);
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
    public final void get(SourceParams sourceParams, DataController.Success<TResponse> success, DataController.Error error) {
        if (getRefreshStrategy().shouldRefresh(this)) {
            doGet(sourceParams, success, error);
        }
    }

    /**
     * Perform the actual information retrieval here. This might call a network, database, or file-based system.
     * Anything that is IO should be done on a separate thread. It is also up to the {@link DataSource}
     * to ensure that both success and error are properly called.
     *
     * @param sourceParams The params used to retrieve information from the {@link DataSource}.
     * @param success      Called when a successful request returns.
     * @param error        Called when a request fails.
     */
    protected abstract void doGet(SourceParams sourceParams, DataController.Success<TResponse> success, DataController.Error error);

    /**
     * Perform the actual information storage here. This might call a network, database, or file-based system.
     * Anything that is IO should be done on a separate thread to prevent blocking.
     *
     * @param dataControllerResponse The response to store.
     */
    protected abstract void doStore(DataControllerResponse<TResponse> dataControllerResponse);

    public abstract SourceType getSourceType();

}
