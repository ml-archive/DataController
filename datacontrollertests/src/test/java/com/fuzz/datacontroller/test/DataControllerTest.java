package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.source.DataSourceContainer.DataSourceParams;
import com.fuzz.datacontroller.source.MemorySource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Description: Tests populating a {@link DataController} and asserts it is correct.
 */
public class DataControllerTest {

    private DataController<String> dataController;
    public DataSource<String> mockDataSource;

    @Before
    public void before_test_Init() {
        //noinspection unchecked
        mockDataSource = mock(DataSource.class);
        when(mockDataSource.sourceType()).thenReturn(DataSource.SourceType.NETWORK);

        when(mockDataSource.refreshStrategy()).thenReturn(new DataSource.RefreshStrategy<String>() {
            @Override
            public boolean shouldRefresh(DataSource<String> dataSource) {
                return true;
            }
        });

        dataController = new DataController.Builder<String>()
                .dataSource(mockDataSource)
                .dataSource(MemorySource.<String>builderInstance().build())
                .build();

    }

    @Test
    public void test_Sources() {
        List<DataSource<String>> sources = new ArrayList<>(dataController.dataSources());
        assertEquals(2, sources.size());

        assertEquals(DataSource.SourceType.MEMORY, sources.get(0).sourceType());
        assertEquals(DataSource.SourceType.NETWORK, sources.get(1).sourceType());
    }

    @Test
    public void test_callSpecific() {
        ArgumentCaptor<DataSource.SourceParams> sourceParamsArgumentCaptor
                = ArgumentCaptor.forClass(DataSource.SourceParams.class);
        ArgumentCaptor<DataController.Success<String>> successArgumentCaptor
                = ArgumentCaptor.forClass(DataController.Success.class);
        ArgumentCaptor<DataController.Error> errorArgumentCaptor
                = ArgumentCaptor.forClass(DataController.Error.class);

        DataSourceParams networkParams = DataSourceParams.networkParams();

        dataController.request(networkParams)
                .build()
                .execute();


        verify(mockDataSource).get(sourceParamsArgumentCaptor.capture(), successArgumentCaptor.capture(),
                errorArgumentCaptor.capture());

        dataController.request(DataSourceParams.memoryParams())
                .build()
                .execute();

        ArgumentCaptor<DataControllerResponse<String>> captor
                = ArgumentCaptor.forClass(DataControllerResponse.class);
        verify(mockDataSource).store(captor.capture());

        dataController.getDataSource(networkParams).getStoredData(null);
        verify(mockDataSource).getStoredData(null);
    }

    @Test
    public void test_requestInvalidSource() {
        boolean failed = false;
        try {
            dataController.request(DataSourceParams.diskParams())
                    .sourceParams(null)
                    .build().execute();
        } catch (RuntimeException r) {
            failed = true;
        }
        assertTrue("Expected a failure for requesting invalid sourcetype: "
                + DataSource.SourceType.DISK, failed);
    }
}
