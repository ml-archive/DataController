package com.fuzz.datacontroller.test

import com.fuzz.datacontroller.DataController
import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.DataResponseError
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.source.DataSourceContainer
import com.fuzz.datacontroller.source.MemorySource
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

/**
 * Description:

 * @author Andrew Grosner (Fuzz)
 */
@Suppress("UNCHECKED_CAST")
class DataControllerRequestTest {


    @Test
    fun test_simpleRequest() {

        val mockCaller = mock(DataSource.DataSourceCaller::class.java) as DataSource.DataSourceCaller<String>
        val dataController = DataController.newBuilder<String>()
                .dataSource(DataSource.Builder(mockCaller, DataSource.SourceType.MEMORY).build())
                .build()

        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)
        `when`(mockCaller.get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())).then {
            successCaptor.value.onSuccess(DataControllerResponse("", DataSource.SourceType.MEMORY))
        }

        val callback: DataController.DataControllerCallback<String> =
                mock(DataController.DataControllerCallback::class.java) as DataController.DataControllerCallback<String>
        val request = dataController.request()
                .register(callback)
                .build()

        assertTrue(request.hasRequestCallbacks())

        request.execute()

        verify(callback).onSuccess(ArgumentCaptor.forClass(DataControllerResponse::class.java).capture()
                as DataControllerResponse<String?>?)
    }

    @Test
    fun test_simpleRequestFailure() {

        val mockCaller = mock(DataSource.DataSourceCaller::class.java) as DataSource.DataSourceCaller<String>
        val dataController = DataController.newBuilder<String>()
                .dataSource(DataSource.Builder(mockCaller, DataSource.SourceType.MEMORY).build())
                .build()

        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)
        `when`(mockCaller.get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())).then {
            errorCaptor.value.onFailure(
                    DataResponseError.Builder(DataSource.SourceType.MEMORY, "").build())
        }

        val callback: DataController.DataControllerCallback<String> =
                mock(DataController.DataControllerCallback::class.java) as DataController.DataControllerCallback<String>
        val request = dataController.request()
                .register(callback)
                .build()

        assertTrue(request.hasRequestCallbacks())

        request.execute()

        verify(callback).onFailure(ArgumentCaptor.forClass(DataResponseError::class.java).capture())
    }

    @Test
    fun test_canTargetProperSource() {

        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)

        val mockCaller = mock(DataSource.DataSourceCaller::class.java) as DataSource.DataSourceCaller<String>
        val dataController = DataController.newBuilder<String>()
                .dataSource(MemorySource.builderInstance<String>().build())
                .dataSource(DataSource.Builder(mockCaller, DataSource.SourceType.NETWORK).build())
                .build()

        dataController.request(DataSourceContainer.DataSourceParams.networkParams())
                .build().execute()

        verify(mockCaller).get(paramsCaptor.capture(), errorCaptor.capture(), successCaptor.capture())
    }
}
