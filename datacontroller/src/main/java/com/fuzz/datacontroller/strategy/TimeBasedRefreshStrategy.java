package com.fuzz.datacontroller.strategy;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataResponseError;
import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: Stores a last update timestamp to only refresh content if its sufficiently "old". Which
 * the constructor will tell us how long before refreshing content.
 */
public class TimeBasedRefreshStrategy<TResponse> implements DataSource.RefreshStrategy<TResponse> {

    private final long refreshTime;
    private long lastUpdateTime;

    /**
     * New instance
     *
     * @param refreshTime The time in between refreshes that we deem refreshing to be valid.
     */
    public TimeBasedRefreshStrategy(long refreshTime) {
        this.refreshTime = refreshTime;
    }

    /**
     * On next call to {@link #shouldRefresh(DataSource)}, it will always refresh.
     */
    public void forceRefresh() {
        lastUpdateTime = 0;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    @Override
    public boolean shouldRefresh(DataSource<TResponse> dataSource) {
        long current = System.currentTimeMillis();
        boolean shouldRefresh = lastUpdateTime == 0 || (current - lastUpdateTime) >= refreshTime;

        // reset update time.
        if (shouldRefresh) {
            lastUpdateTime = System.currentTimeMillis();
        }
        return shouldRefresh;
    }


    @Override
    public void onRefreshFailed(DataSource<TResponse> dataSource,
                                DataResponseError responseError) {
        // reset for next refresh
        forceRefresh();
    }

    @Override
    public void onRefreshSucceeded(DataSource<TResponse> dataSource,
                                   DataControllerResponse<TResponse> response) {
        // do nothing here.
    }
}