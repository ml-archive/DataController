package com.fuzz.datacontroller.test.source;

import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.MemoryDataSource;
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
public class MemoryDataSourceTest {

    private MemoryDataSource<DataItem> dataSource;

    @Before
    public void setup_test() {
        dataSource = new MemoryDataSource<>();
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
        dataSource.doStore(new DataControllerResponse<>(item, DataSource.SourceType.NETWORK));

        assertNotNull(dataSource.getStoredData());
    }

    @Test
    public void test_clearData() {
        DataItem item = new DataItem();
        dataSource.store(new DataControllerResponse<>(item, DataSource.SourceType.NETWORK));

        assertNotNull(dataSource.getStoredData());

        dataSource.clearStoredData();

        assertNull(dataSource.getStoredData());
    }
}
