package com.fuzz.datacontroller.strategy;

import com.fuzz.datacontroller.DataController;

/**
 * Description: Simple interface for refreshing data when needed. Subclasses define the strategy to refresh
 * data here.
 */
public interface IRefreshStrategy {

    /**
     * @param dataController The data controller to evaluate.
     * @return true if we trigger a refresh and redownload, false if we do not.
     */
    boolean shouldRefresh(DataController dataController);
}
