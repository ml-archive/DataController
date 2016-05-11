package com.fuzz.datacontroller.test;

import android.os.Build.VERSION_CODES;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.DataResponseError;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;
import com.fuzz.datacontroller.tests.BuildConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Description:
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = VERSION_CODES.LOLLIPOP)
public class FuzzQuizDataControllerTest {

    @Before
    public void setup_test() {
        FlowManager.init(new FlowConfig.Builder(RuntimeEnvironment.application).build());
    }

    @Test
    public void test_fuzzQuizDataController_DataFetcher() {

        FuzzQuizDataController dataController = new FuzzQuizDataController();

        // get all responses first
        final CountDownLatch lock = new CountDownLatch(dataController.getSources().size());
        final DataControllerResponse[] responseArray = new DataControllerResponse[2];
        dataController.registerForCallbacks(new DataController.DataControllerCallback<List<DataItem>>() {
            @Override
            public void onFailure(DataResponseError dataResponseError) {
                lock.countDown();
            }

            @Override
            public void onSuccess(DataControllerResponse<List<DataItem>> response) {
                lock.countDown();
                if (response.getSourceType().equals(DataSource.SourceType.NETWORK)) {
                    responseArray[0] = response;
                } else if (response.getSourceType().equals(DataSource.SourceType.DISK)) {
                    responseArray[1] = response;
                }
            }
        });

        dataController.requestData(); // call here
        try {
            lock.await();
        } catch (InterruptedException e) {
        }

        assertNotNull(responseArray[0]);
        assertNotNull(responseArray[1]);

        DataSource<List<DataItem>> source = dataController.getSource(DataSource.SourceType.MEMORY);
        assertNotNull(source.getStoredData());

        List<DataItem> dataItems = dataController.getSource(DataSource.SourceType.DISK).getStoredData();
        assertFalse(dataItems.isEmpty());
    }
}
