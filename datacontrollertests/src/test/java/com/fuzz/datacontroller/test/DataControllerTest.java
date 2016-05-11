package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.datacontroller2.DataController;
import com.fuzz.datacontroller.datacontroller2.source.DataSource;
import com.fuzz.datacontroller.datacontroller2.source.MemoryDataSource;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Description: Tests populating a {@link DataController} and asserts it is correct.
 */
public class DataControllerTest {

    private DataController<String> dataController;

    @Before
    public void before_test_Init() {
        dataController = new DataController<>();
        dataController.registerDataSource(new MockDataSource<String>() {
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

    @Test
    public void test_callSpecific() {
        MockDataSource<String> source = (MockDataSource<String>) dataController.getSource(DataSource.SourceType.NETWORK);
        dataController.requestSpecific(DataSource.SourceType.NETWORK, new DataSource.SourceParams());
        assertTrue(source.isGetCalled());

        dataController.requestSpecific(DataSource.SourceType.MEMORY, new DataSource.SourceParams());
        assertTrue(source.isStoreCalled());

        dataController.getSource(DataSource.SourceType.NETWORK).getStoredData(null);
        assertTrue(source.isGetStoredCalled());
    }

    @Test
    public void test_requestInvalidSource() {
        boolean failed = false;
        try {
            dataController.requestSpecific(DataSource.SourceType.DISK, null);
        } catch (RuntimeException r) {
            failed = true;
        }
        assertTrue("Expected a failure for requesting invalid sourcetype: "
                + DataSource.SourceType.DISK, failed);
    }
}
