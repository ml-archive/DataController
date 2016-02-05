package com.fuzz.datacontroller;

import com.fuzz.datacontroller.data.IDataStore;
import com.fuzz.datacontroller.fetcher.DataFetcher;
import com.fuzz.datacontroller.strategy.IRefreshStrategy;

/**
 * Description: A builder that helpfully constructs a custom {@link DataController} without need
 * to explicitly subclass it.
 */
public class DataControllerBuilder<TResponse> {

    private DataFetcher<TResponse> dataFetcher;
    private IRefreshStrategy refreshStrategy;
    private IDataStore<TResponse> dataStore;
    private DataController.IEmptyChecker<TResponse> emptyChecker;

    /**
     * Sets a {@link DataFetcher} that defines how this object fetches data.
     *
     * @param dataFetcher Defines how this {@link DataController} fetches data.
     */
    public DataControllerBuilder<TResponse> setDataFetcher(DataFetcher<TResponse> dataFetcher) {
        this.dataFetcher = dataFetcher;
        return this;
    }

    /**
     * Defines how data gets refreshed when a call to {@link DataController#requestDataAsync()} is called.
     *
     * @param refreshStrategy The strategy to use.
     */
    public DataControllerBuilder<TResponse> setRefreshStrategy(IRefreshStrategy refreshStrategy) {
        this.refreshStrategy = refreshStrategy;
        return this;
    }

    /**
     * Defines where this {@link DataController} stores its data. Could be a database, in memory, or
     * more.
     *
     * @param dataStore The storage interface container.
     */
    public DataControllerBuilder<TResponse> setDataStore(IDataStore<TResponse> dataStore) {
        this.dataStore = dataStore;
        return this;
    }

    /**
     * Sets the calculator for when a {@link TResponse} is considered empty. This will default to false.
     *
     * @param emptyChecker The empty checker.
     */
    public DataControllerBuilder<TResponse> setEmptyChecker(DataController.IEmptyChecker<TResponse> emptyChecker) {
        this.emptyChecker = emptyChecker;
        return this;
    }

    /**
     * @return A new {@link DataController}. Subsequent calls constructs a new instance. This is fully atomic.
     */
    public DataController<TResponse> build() {
        return new DataController<>(dataFetcher, dataStore, refreshStrategy, emptyChecker);
    }

}
