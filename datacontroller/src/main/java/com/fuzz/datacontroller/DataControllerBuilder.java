package com.fuzz.datacontroller;

import com.fuzz.datacontroller.fetcher.DataFetcher;
import com.fuzz.datacontroller.strategy.IRefreshStrategy;

/**
 * Description: A builder that helpfully constructs a custom {@link DataController} without need
 * to explicitly subclass it.
 */
public class DataControllerBuilder<TResponse> {

    /**
     * Simple interface for checking if empty.
     *
     * @param <TResponse>
     */
    public interface IEmpty<TResponse> {

        /**
         * @param response The response to validate.
         * @return True if the response is deemed empty here.
         */
        boolean isEmpty(TResponse response);
    }

    private DataFetcher<TResponse> dataFetcher;
    private IRefreshStrategy refreshStrategy;

    public DataControllerBuilder<TResponse> setDataFetcher(DataFetcher<TResponse> dataFetcher) {
        this.dataFetcher = dataFetcher;
        return this;
    }

    public DataControllerBuilder<TResponse> setRefreshStrategy(IRefreshStrategy refreshStrategy) {
        this.refreshStrategy = refreshStrategy;
        return this;
    }

    public DataController<TResponse> build(final IEmpty<TResponse> emptyMethod) {
        DataController<TResponse> dataController = new DataController<TResponse>() {
            @Override
            public boolean isEmpty(TResponse tResponse) {
                return emptyMethod != null && emptyMethod.isEmpty(tResponse);
            }

            @Override
            protected DataFetcher<TResponse> createDataFetcher() {
                return dataFetcher;
            }
        };
        if (refreshStrategy != null) {
            dataController.setRefreshStrategy(refreshStrategy);
        }
        return dataController;
    }

}
