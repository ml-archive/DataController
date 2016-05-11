package com.fuzz.datacontroller.datacontroller2.strategy;

import com.fuzz.datacontroller.datacontroller2.source.DataSource;

/**
 * Description: Only ever gets called once, but can get reset.
 */
public class OneShotRefreshStrategy<TResponse> implements DataSource.RefreshStrategy<TResponse> {

    private boolean shouldRefresh = true;

    public void setShouldRefresh(boolean shouldRefresh) {
        this.shouldRefresh = shouldRefresh;
    }

    @Override
    public boolean shouldRefresh(DataSource<TResponse> dataSource) {
        boolean should = shouldRefresh;
        if (shouldRefresh) {
            shouldRefresh = false;
        }
        return should;
    }
}
