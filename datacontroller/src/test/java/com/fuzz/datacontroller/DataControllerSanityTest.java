package com.fuzz.datacontroller;

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
                    public void call() {

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
                }).build(new DataControllerBuilder.IEmpty<String>() {
                    @Override
                    public boolean isEmpty(String s) {
                        return false;
                    }
                });
    }

    @Test
    public void test_dataControllerPopulation() {

        assertNotNull(dataController.getDataFetcher());
        assertTrue(dataController.getDataFetcher().getResponseType().equals(DataControllerResponse.ResponseType.NETWORK));
        assertNotNull(dataController.getRefreshStrategy());
        assertNotNull(dataController.getState());
    }

    @Test
    public void test_stateSetting() {
        dataController.setState(DataController.State.EMPTY);
        assertEquals(dataController.getState(), DataController.State.EMPTY);
    }

    

}
