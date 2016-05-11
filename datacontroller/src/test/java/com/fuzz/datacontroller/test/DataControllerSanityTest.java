package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerBuilder;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;
import com.fuzz.datacontroller.data.MemoryDataStore;
import com.fuzz.datacontroller.fetcher.DataFetcher;
import com.fuzz.datacontroller.strategy.IRefreshStrategy;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description: Tests populating a {@link DataController} and asserts it is correct.
 */
public class DataControllerSanityTest {

    private DataController<String> dataController;

    @Before
    public void before_test_Init() {
        dataController = new DataControllerBuilder<String>()
                .setDataFetcher(new DataFetcher<String>() {
                    @Override
                    public void callAsync() {

                    }

                    @Override
                    public DataControllerResponse.ResponseType getResponseType() {
                        return DataControllerResponse.ResponseType.NETWORK;
                    }
                })
                .setRefreshStrategy(new IRefreshStrategy() {
                    @Override
                    public boolean shouldRefresh(DataController dataController) {
                        return true;
                    }
                })
                .setEmptyChecker(new DataController.IEmptyChecker<String>() {
                    @Override
                    public boolean isEmpty(String s) {
                        return s == null || s.trim().length() == 0;
                    }
                })
                .setDataStore(new MemoryDataStore<String>())
                .build();
    }

    @Test
    public void test_dataControllerPopulation() {
        assertNotNull(dataController.getDataFetcher());
        assertTrue(dataController.getDataFetcher().getResponseType().equals(DataControllerResponse.ResponseType.NETWORK));
        assertNotNull(dataController.getRefreshStrategy());
        assertNotNull(dataController.getState());
        assertNotNull(dataController.getDataStore());
    }

    @Test
    public void test_stateSetting() {
        dataController.setState(DataController.State.EMPTY);
        assertEquals(dataController.getState(), DataController.State.EMPTY);

        dataController.onSuccessfulResponse(new DataControllerResponse<>("This is a test", DataControllerResponse.ResponseType.NETWORK), "no url");

        assertEquals(dataController.getState(), DataController.State.SUCCESS);
        assertEquals("This is a test", dataController.getStoredData());

        dataController.onSuccessfulResponse(new DataControllerResponse<String>(null, DataControllerResponse.ResponseType.NETWORK), "no url");
        assertEquals(dataController.getState(), DataController.State.EMPTY);

        assertEquals(null, dataController.getStoredData());
    }


}
