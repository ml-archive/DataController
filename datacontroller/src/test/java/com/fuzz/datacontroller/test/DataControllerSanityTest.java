package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.DataControllerResponse;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;
import com.fuzz.datacontroller.datacontroller2.source.MemoryDataSource;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Description: Tests populating a {@link DataController} and asserts it is correct.
 */
public class DataControllerSanityTest {

    private DataController<String> dataController;

    @Before
    public void before_test_Init() {
        dataController = new DataController<>();
        dataController.registerDataSource(new DataSource<String>() {
            @Override
            public void doStore(DataControllerResponse<String> tResponse) {
            }

            @Override
            protected void doGet(SourceParams sourceParams, DataController.Success<String> success, DataController.Error error) {
            }

            @Override
            public SourceType getSourceType() {
                return SourceType.NETWORK;
            }
        });
        dataController.registerDataSource(new MemoryDataSource<String>());
    }

    @Test
    public void test_Sources() {
        List<DataSource<String>> sources = dataController.getSources();
        assertEquals(2, sources.size());

        assertEquals(DataSource.SourceType.MEMORY, sources.get(0).getSourceType());
        assertEquals(DataSource.SourceType.NETWORK, sources.get(1).getSourceType());
    }
}
