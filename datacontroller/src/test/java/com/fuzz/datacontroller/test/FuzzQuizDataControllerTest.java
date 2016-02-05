package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class FuzzQuizDataControllerTest {

    @Test
    public void test_fuzzQuizDataController_DataFetcher() {

        FuzzQuizDataController dataController = new FuzzQuizDataController();

        assertNotNull(dataController.getDataFetcher());
        List<DataItem> dataItems = dataController.requestDataSync();
        assertNotNull(dataItems);
        assertTrue(!dataItems.isEmpty());

        assertEquals(dataController.getState(), DataController.State.SUCCESS);

        assertNotNull(dataController.getStoredData());
        assertEquals(dataController.getStoredData(), dataItems);

        assertNull(dataController.requestDataSync()); // null due to refresh strategy


    }
}
