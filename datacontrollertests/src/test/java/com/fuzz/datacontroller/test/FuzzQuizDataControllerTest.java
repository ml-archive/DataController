package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.DataResponseError;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertNotNull;

/**
 * Description:
 */
public class FuzzQuizDataControllerTest {

    @Test
    public void test_fuzzQuizDataController_DataFetcher() {g

        FuzzQuizDataController dataController = new FuzzQuizDataController();

        final CountDownLatch lock = new CountDownLatch(2);
        final DataControllerResponse[] responseArray = new DataControllerResponse[1];
        dataController.registerForCallbacks(new DataController.DataControllerCallback<List<DataItem>>() {
            @Override
            public void onFailure(DataResponseError dataResponseError) {
                lock.countDown();
            }

            @Override
            public void onSuccess(DataControllerResponse<List<DataItem>> response) {
                lock.countDown();
                responseArray[0] = response;
            }
        });

        dataController.requestData(); // call here
        try {
            lock.await();
        } catch (InterruptedException e) {
        }

        assertNotNull(responseArray[0]);

        DataSource<List<DataItem>> source = dataController.getSource(DataSource.SourceType.MEMORY);
        assertNotNull(source.getStoredData());


    }
}
