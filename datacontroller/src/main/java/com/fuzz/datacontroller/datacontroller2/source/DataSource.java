package com.fuzz.datacontroller.datacontroller2.source;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.strategy.RefreshStrategy;

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


    public RefreshStrategy<TResponse> getRefreshStrategy() {
        return refreshStrategy;
    }

    /**
     * Stores a response.
     */
    public abstract void store(TResponse tResponse);

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

    protected abstract void doGet(SourceParams sourceParams, DataController.Success<TResponse> success, DataController.Error error);
}
