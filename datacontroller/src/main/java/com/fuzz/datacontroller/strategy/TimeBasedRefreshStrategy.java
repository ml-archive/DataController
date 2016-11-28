package com.fuzz.datacontroller.strategy;

import com.fuzz.datacontroller.source.DataSource;

/**
 * Description: Stores a last update timestamp to only refresh content if its sufficiently "old". Which
 * the constructor will tell us how long before refreshing content.
 */
public interface TimeBasedRefreshStrategy<TResponse> extends DataSource.RefreshStrategy<TResponse> {

    /**
     * On next call to {@link #shouldRefresh(DataSource)}, it will always refresh.
     */
    void forceRefresh();

    void setLastUpdateTime(long lastUpdateTime);

    long getLastUpdateTime();

}