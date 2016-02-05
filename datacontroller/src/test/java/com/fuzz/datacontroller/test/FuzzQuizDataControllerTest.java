package com.fuzz.datacontroller.test;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class FuzzQuizDataControllerTest {

    @Test
    public void test_fuzzQuizDataController_DataFetcher() {

        FuzzQuizDataController dataController = new FuzzQuizDataController();

        assertNotNull(dataController.getDataFetcher());
        List<DataItem> dataItems = dataController.getDataFetcher().call();
        assertNotNull(dataItems);
        assertTrue(!dataItems.isEmpty());
    }
}
