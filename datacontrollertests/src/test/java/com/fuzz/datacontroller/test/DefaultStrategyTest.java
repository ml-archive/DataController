package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.strategy.OneShotRefreshStrategy;
import com.fuzz.datacontroller.strategy.TimebasedRefreshStrategy;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class DefaultStrategyTest {

    @Test
    public void test_OneShotRefresh() {
        OneShotRefreshStrategy<List<DataItem>> oneShotRefreshStrategy = new OneShotRefreshStrategy<>();
        assertTrue(oneShotRefreshStrategy.shouldRefresh(null));
        assertFalse(oneShotRefreshStrategy.shouldRefresh(null));

        oneShotRefreshStrategy.setShouldRefresh(true);
        assertTrue(oneShotRefreshStrategy.shouldRefresh(null));
    }

    @Test
    public void test_TimeBasedRefresh() {
        TimebasedRefreshStrategy<List<DataItem>> timebasedRefreshStrategy = new TimebasedRefreshStrategy<>(1000L);
        assertTrue(timebasedRefreshStrategy.shouldRefresh(null));
        assertFalse(timebasedRefreshStrategy.shouldRefresh(null));

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }

        assertTrue(timebasedRefreshStrategy.shouldRefresh(null));

        timebasedRefreshStrategy.forceRefresh();
        assertTrue(timebasedRefreshStrategy.shouldRefresh(null));

        long time = System.currentTimeMillis();
        timebasedRefreshStrategy.setLastUpdateTime(time);
        assertEquals(time, timebasedRefreshStrategy.getLastUpdateTime());
    }
}
