package com.fuzz.datacontroller

import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.source.DataSource.SourceType.MEMORY
import com.fuzz.datacontroller.source.DataSource.SourceType.NETWORK
import com.fuzz.datacontroller.source.DataSourceCaller
import com.fuzz.datacontroller.source.DataSourceContainer.DataSourceParams.memoryParams
import com.fuzz.datacontroller.source.DataSourceContainer.DataSourceParams.networkParams
import com.fuzz.datacontroller.source.DataSourceStorage
import com.fuzz.datacontroller.source.MemorySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Description:

 * @author Andrew Grosner (Fuzz)
 */
@Suppress("UNCHECKED_CAST")
class DataControllerRequestTest {


    @Test
    fun test_simpleRequest() {

        val mockCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockCaller.sourceType()).thenReturn(MEMORY)
        val dataController = DataController.newBuilder<String>()
                .dataSource(DataSource.Builder(mockCaller).build())
                .build()

        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)
        `when`(mockCaller.get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())).then {
            successCaptor.value.onSuccess(DataControllerResponse("", MEMORY))
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

        val mockCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockCaller.sourceType()).thenReturn(MEMORY)
        val dataController = DataController.newBuilder<String>()
                .dataSource(DataSource.Builder(mockCaller).build())
                .build()

        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)
        `when`(mockCaller.get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())).then {
            errorCaptor.value.onFailure(
                    DataResponseError.Builder(MEMORY, "").build())
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

        val mockCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockCaller.sourceType()).thenReturn(NETWORK)

        val dataController = DataController.newBuilder<String>()
                .dataSource(MemorySource.builderInstance<String>().build())
                .dataSource(DataSource.Builder(mockCaller).build())
                .build()

        dataController.request(networkParams())
                .build().execute()

        verify(mockCaller).get(paramsCaptor.capture(), errorCaptor.capture(), successCaptor.capture())
    }

    @Test
    fun test_filterCallbackSuccess() {

        val callback: DataController.DataControllerCallback<String> =
                mock(DataController.DataControllerCallback::class.java) as DataController.DataControllerCallback<String>
        val mockCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockCaller.sourceType()).thenReturn(NETWORK)

        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)

        val dataController = DataController.newBuilder<String>()
                .dataSource(DataSource.Builder<String>(mockCaller).build())
                .build()

        val memorySuccess = DataControllerResponse("Filtered", MEMORY)

        val request = dataController.request()
                .register(callback)
                .successFilter(DataControllerRequest.SuccessFilter<String> { memorySuccess })
                .build()

        `when`(mockCaller.get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())).then {
            successCaptor.value.onSuccess(DataControllerResponse("non", MEMORY))
        }

        request.execute()

        verify(callback).onSuccess(memorySuccess)
    }

    @Test
    fun test_filterCallbackFailure() {

        val callback: DataController.DataControllerCallback<String> =
                mock(DataController.DataControllerCallback::class.java) as DataController.DataControllerCallback<String>
        val mockCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockCaller.sourceType()).thenReturn(NETWORK)

        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)

        val dataController = DataController.newBuilder<String>()
                .dataSource(DataSource.Builder<String>(mockCaller).build())
                .build()

        val memoryError = DataResponseError.Builder(MEMORY, "").build()

        val request = dataController.request()
                .register(callback)
                .errorFilter(DataControllerRequest.ErrorFilter { memoryError })
                .build()

        `when`(mockCaller.get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())).then {
            errorCaptor.value.onFailure(DataResponseError.Builder(NETWORK, "").build())
        }

        request.execute()

        verify(callback).onFailure(memoryError)

    }

    @Test
    fun test_requestCallbacksDisposal() {

        val callbackCreationObject = {
            object : DataController.DataControllerCallback<String> {
                override fun onSuccess(response: DataControllerResponse<String>?) {

                }

                override fun onFailure(dataResponseError: DataResponseError?) {

                }
            }
        }

        val dataController = DataController.newBuilder<String>()
                .dataSource(MemorySource.builderInstance<String>().build())
                .build()
        dataController.registerForCallbacks(callbackCreationObject())

        val request = dataController.request()
                .register(callbackCreationObject())
                .register(callbackCreationObject())
                .build()

        assertTrue(request.hasRequestCallbacks())
        assertTrue(request.hasCallbacks())
        assertTrue(dataController.hasCallbacks())

        request.clearCallbacks()

        assertFalse(request.hasRequestCallbacks())
        assertTrue(request.hasCallbacks())
        assertTrue(dataController.hasCallbacks())
    }

    @Test
    fun test_requestSingularCallbackDisposal() {

        val callback = object : DataController.DataControllerCallback<String> {
            override fun onSuccess(response: DataControllerResponse<String>?) {

            }

            override fun onFailure(dataResponseError: DataResponseError?) {

            }
        }

        val dataController = DataController.newBuilder<String>()
                .dataSource(MemorySource.builderInstance<String>().build())
                .build()

        val request = dataController.request()
                .register(callback)
                .build()

        assertTrue(request.hasRequestCallbacks())

        request.deregister(callback)

        assertFalse(request.hasRequestCallbacks())
    }

    @Test
    fun test_canTargetSourceWithDifferentParams() {

        val callback: DataController.DataControllerCallback<String> =
                mock(DataController.DataControllerCallback::class.java) as DataController.DataControllerCallback<String>

        val mockNetworkCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockNetworkCaller.sourceType()).thenReturn(NETWORK)

        val mockMemoryCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockMemoryCaller.sourceType()).thenReturn(MEMORY)


        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)

        val dataController = DataController.newBuilder<String>()
                .dataSource(DataSource.Builder<String>(mockNetworkCaller).build())
                .dataSource(DataSource.Builder<String>(mockMemoryCaller).build())
                .build()

        val targetMemoryParams = DataSource.SourceParams()
        val targetNetworkParams = DataSource.SourceParams()

        val request = dataController.request()
                .register(callback)
                .targetSource(networkParams(), targetNetworkParams)
                .targetSource(memoryParams(), targetMemoryParams)
                .build()

        request.execute()

        verify(mockMemoryCaller).get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())
        assertEquals(targetMemoryParams, paramsCaptor.value)

        verify(mockNetworkCaller).get(paramsCaptor.capture(),
                errorCaptor.capture(), successCaptor.capture())
        assertEquals(targetNetworkParams, paramsCaptor.value)
    }

    @Test
    fun test_canTargetStorage() {
        val responseCaptor = ArgumentCaptor.forClass(DataControllerResponse::class.java)
                as ArgumentCaptor<DataControllerResponse<String>>

        val mockCaller = mock(DataSourceCaller::class.java) as DataSourceCaller<String>
        `when`(mockCaller.sourceType()).thenReturn(NETWORK)
        val successCaptor = ArgumentCaptor.forClass(DataController.Success::class.java)
                as ArgumentCaptor<DataController.Success<String>>
        val errorCaptor = ArgumentCaptor.forClass(DataController.Error::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)
        `when`(mockCaller.get(paramsCaptor.capture(), errorCaptor.capture(), successCaptor.capture()))
                .then {
                    successCaptor.value.onSuccess(DataControllerResponse("", NETWORK))
                }

        val mockStorage = mock(DataSourceStorage::class.java) as DataSourceStorage<String>
        val dataController = DataController.newBuilder<String>()
                .dataSource(MemorySource.builderInstance<String>().storage(mockStorage).build())
                .dataSource(DataSource.Builder(mockCaller).build())
                .build()

        dataController.request(networkParams())
                .addStorageSourceTarget(memoryParams())
                .build()
                .execute()

        verify(mockStorage).store(responseCaptor.capture())
    }
}
