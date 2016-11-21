package com.fuzz.datacontroller

import com.fuzz.datacontroller.DataController
import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.source.DataSourceContainer.DataSourceParams
import com.fuzz.datacontroller.source.MemorySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.util.*

/**
 * Description: Tests populating a [DataController] and asserts it is correct.
 */
@Suppress("UNCHECKED_CAST")
class DataControllerTest {

    private lateinit var dataController: DataController<String>
    private lateinit var mockDataSource: DataSource<String>
    private lateinit var mockCaller: DataSource.DataSourceCaller<String>
    private lateinit var mockStorage: DataSource.DataSourceStorage<String>

    @Before
    fun before_test_Init() {
        mockCaller = mock<DataSource.DataSourceCaller<*>>(DataSource.DataSourceCaller::class.java)
                as DataSource.DataSourceCaller<String>
        mockStorage = mock<DataSource.DataSourceStorage<*>>(DataSource.DataSourceStorage::class.java)
                as DataSource.DataSourceStorage<String>
        mockDataSource = DataSource.Builder(mockCaller, DataSource.SourceType.NETWORK)
                .storage(mockStorage).build()

        dataController = DataController.Builder<String>()
                .dataSource(mockDataSource)
                .dataSource(MemorySource.builderInstance<String>()
                        .build())
                .build()

    }

    @Test
    fun test_Sources() {
        val sources = ArrayList(dataController.dataSources())
        assertEquals(2, sources.size.toLong())

        assertEquals(DataSource.SourceType.MEMORY, sources[0].sourceType())
        assertEquals(DataSource.SourceType.NETWORK, sources[1].sourceType())
    }

    @Test
    fun test_callSpecific() {
        val sourceParamsArgumentCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)
        val successArgumentCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorArgumentCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)

        val networkParams = DataSourceParams.networkParams()

        dataController.request(networkParams).build().execute()


        verify<DataSource.DataSourceCaller<String>>(mockCaller)
                .get(sourceParamsArgumentCaptor.capture(), errorArgumentCaptor.capture(),
                        successArgumentCaptor.capture())

        dataController.request(DataSourceParams.memoryParams())
                .build()
                .execute()

        val captor = ArgumentCaptor.forClass(DataControllerResponse::class.java)
                as ArgumentCaptor<DataControllerResponse<String>>
        verify(mockStorage).store(captor.capture())

        dataController.getDataSource(networkParams).getStoredData(null)
        verify(mockStorage).getStoredData(null)
    }

    @Test
    fun test_requestInvalidSource() {
        var failed = false
        try {
            dataController.request(DataSourceParams.diskParams())
                    .sourceParams(null).build()
                    .execute()
        } catch (r: RuntimeException) {
            failed = true
        }

        assertTrue("Expected a failure for requesting invalid sourcetype: " + DataSource.SourceType.DISK, failed)
    }
}
