package com.fuzz.datacontroller.okhttp

import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.DataResponseError
import com.fuzz.datacontroller.source.DataSource
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.io.IOException

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */
class OkHttpDataSourceTest {

    @Test
    fun test_canEnqueueCall() {
        val source = OkHttpDataSource.builderInstance { _, _ -> "got response" }.build()
        var response: DataControllerResponse<String>? = null
        val mockedCall = mock(Call::class.java)
        val mockResponse = Response.Builder()
                .request(Request.Builder().url("https://www.wegmans.com").get().build())
                .protocol(Protocol.HTTP_2)
                .code(200).build()

        Mockito.`when`(mockedCall.enqueue(ArgumentCaptor.forClass(Callback::class.java).capture()))
                .then {
                    val callback: Callback? = it.getArgument(0)
                    callback?.onResponse(null, mockResponse)
                }

        source.get(OkHttpDataSource.OkHttpParams(mockedCall), { }, { response = it })

        assertNotNull(response)
        assertEquals("got response", response?.response)
    }

    @Test
    fun test_canEnqueueFailure() {
        val source = OkHttpDataSource.builderInstance { _, _ -> "got response" }.build()
        var error: DataResponseError? = null
        val mockedCall = mock(Call::class.java)
        val exception = IOException("error")

        Mockito.`when`(mockedCall.enqueue(ArgumentCaptor.forClass(Callback::class.java).capture()))
                .then {
                    val callback: Callback? = it.getArgument(0)
                    callback?.onFailure(null, exception)
                }

        source.get(OkHttpDataSource.OkHttpParams(mockedCall), { error = it }, { })

        assertNotNull(error)
        error?.apply {
            assertEquals(DataSource.SourceType.NETWORK, failedSource())
            assertEquals(exception, exception())
        }
    }
}