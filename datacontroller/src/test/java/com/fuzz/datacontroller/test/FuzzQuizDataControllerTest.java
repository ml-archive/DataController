package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
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

        dataController.onSuccessfulResponse(new DataControllerResponse<>(dataItems, DataControllerResponse.ResponseType.NETWORK), "");

        assertEquals(dataController.getState(), DataController.State.SUCCESS);

        assertNotNull(dataController.getStoredData());
        assertEquals(dataController.getStoredData(), dataItems);
    }
}
