package com.fuzz.datacontroller.strategy;

import com.fuzz.datacontroller.DataController;

/**
 * Description: Only every gets called once, but can get reset.
 */
public class OneShotRefreshStrategy implements IRefreshStrategy {

    private boolean shouldRefresh = true;

    @Override
    public boolean shouldRefresh(DataController dataController) {
        boolean should = shouldRefresh;
        if (shouldRefresh) {
            shouldRefresh = false;
        }
        return should;
    }

    public void setShouldRefresh(boolean shouldRefresh) {
        this.shouldRefresh = shouldRefresh;
    }
}
