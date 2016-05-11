package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.strategy.OneShotRefreshStrategy;
import com.fuzz.datacontroller.strategy.TimeBasedRefreshStrategy;

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
        TimeBasedRefreshStrategy<List<DataItem>> timeBasedRefreshStrategy = new TimeBasedRefreshStrategy<>(1000L);
        assertTrue(timeBasedRefreshStrategy.shouldRefresh(null));
        assertFalse(timeBasedRefreshStrategy.shouldRefresh(null));

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
        }

        assertTrue(timeBasedRefreshStrategy.shouldRefresh(null));

        timeBasedRefreshStrategy.forceRefresh();
        assertTrue(timeBasedRefreshStrategy.shouldRefresh(null));

        long time = System.currentTimeMillis();
        timeBasedRefreshStrategy.setLastUpdateTime(time);
        assertEquals(time, timeBasedRefreshStrategy.getLastUpdateTime());
    }
}
