package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.BaseRobolectricUnitTest;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.DataItem;
import com.fuzz.datacontroller.ValidateCallback;
import com.fuzz.datacontroller.source.DataSource;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AsyncDBFlowListDataSourceTest extends BaseRobolectricUnitTest {

    private DataSource<List<DataItem>> dataSource;

    @Before
    public void setup_test() {
        dataSource = DBFlowListSource.builderInstance(DataItem.class, true).build();
    }

    @Test
    public void test_canGet() {
        DataItem model = new DataItem();
        model.setId("Andrew");
        model.setData("Test");
        dataSource.store(new DataControllerResponse<>(Collections.singletonList(model),
                DataSource.SourceType.NETWORK));

        ValidateCallback<List<DataItem>> callback = new ValidateCallback<>();
        dataSource.get(null, callback, callback);
        assertTrue(callback.isSuccessCalled());
        assertNull(callback.getError());
        DataControllerResponse<List<DataItem>> response = callback.getResponse();
        assertNotNull(response);

        List<DataItem> items = response.getResponse();
        assertFalse(items.isEmpty());

        DataItem item = items.get(0);
        assertEquals("Andrew", item.getId());
    }

    @Test
    public void test_canStore() {
        List<DataItem> items = new ArrayList<>();

        DataItem model = new DataItem();
        model.setId("Andrew");
        items.add(model);

        model = new DataItem();
        model.setId("Andrew2");
        items.add(model);

        dataSource.store(new DataControllerResponse<>(items, DataSource.SourceType.NETWORK));

        items = dataSource.getStoredData();
        assertNotNull(items);
        assertFalse(items.isEmpty());

        DataItem item = items.get(0);
        assertEquals("Andrew", item.getId());
    }
}
