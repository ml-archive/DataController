package com.fuzz.datacontroller.test.source;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.MemorySource;
import com.fuzz.datacontroller.test.DataItem;
import com.fuzz.datacontroller.test.ValidateCallback;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class MemorySourceTest {

    private MemorySource<DataItem> dataSource;

    @Before
    public void setup_test() {
        dataSource = new MemorySource<>();
    }

    @Test
    public void test_get() {
        ValidateCallback<DataItem> callback = new ValidateCallback<>();
        dataSource.get(null, callback, callback);
        assertTrue(callback.isSuccessCalled());
    }

    @Test
    public void test_stored() {
        DataItem item = new DataItem();
        dataSource.store(new DataControllerResponse<>(item, DataSource.SourceType.NETWORK));

        assertNotNull(dataSource.getStoredData(new DataSource.SourceParams()));
    }

    @Test
    public void test_clearData() {
        DataSource.SourceParams sourceParams = new DataSource.SourceParams();

        DataItem item = new DataItem();
        dataSource.store(new DataControllerResponse<>(item, DataSource.SourceType.NETWORK));

        assertNotNull(dataSource.getStoredData(sourceParams));

        dataSource.clearStoredData(sourceParams);

        assertNull(dataSource.getStoredData(sourceParams));
    }
}
