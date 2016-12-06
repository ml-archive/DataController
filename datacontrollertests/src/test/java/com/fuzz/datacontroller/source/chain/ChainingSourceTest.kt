package com.fuzz.datacontroller.source.chain

import com.fuzz.datacontroller.DataController
import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.source.DataSource.SourceType.MEMORY
import com.fuzz.datacontroller.source.MemorySource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify

@Suppress("UNCHECKED_CAST")
/**
 * Description:

 * @author Andrew Grosner (Fuzz)
 */

class ChainingSourceTest {


    @Test
    fun test_canChainSimpleCalls() {

        val chainSource = ChainConstruct.builderInstance(MemorySource<Boolean>(true),
                MemorySource<String>("Chained Type"))
                .chain<String>(MemorySource<String>("Final Chain Type"))
                .build(MEMORY)

        var success: DataControllerResponse<String>? = null
        chainSource[DataSource.SourceParams.defaultParams, {}, { success = it }]

        assertNotNull(success)

        success?.let {
            assertEquals("Final Chain Type", it.response)
            assertEquals(MEMORY, it.sourceType)
        }
    }

    @Test
    fun test_canChainThenMergeCalls() {
        val chainSource = ChainConstruct.builderInstance(MemorySource<Boolean>(true),
                MemorySource<String>("Chained Type"))
                .merge<Int, String>(MemorySource<Int>(),
                        MergeConstruct.ResponseMerger<String, Int, String> {
                            firstResponse, secondResponse ->
                            DataControllerResponse("merged", MEMORY)
                        }).build(MEMORY)

        var success: DataControllerResponse<String>? = null
        chainSource[DataSource.SourceParams.defaultParams, {}, { success = it }]

        assertNotNull(success)

        success?.let {
            assertEquals("merged", it.response)
            assertEquals(MEMORY, it.sourceType)
        }
    }


    @Test
    fun test_canChainThenMergeCallResponseToNext() {
        val chainSource = ChainConstruct.builderInstance(MemorySource<Boolean>(true),
                MemorySource<String>("Chained Type"))
                .merge<Int, String>(MemorySource<Int>(),
                        MergeConstruct.ResponseMerger<String, Int, String> {
                            firstResponse, secondResponse ->
                            DataControllerResponse("merged", MEMORY)
                        })
                .responseToNextCallConverter(
                        ChainConstruct.ResponseToNextCallConverter<String> { first, sourceParams -> sourceParams })
                .build(MEMORY)

        var success: DataControllerResponse<String>? = null
        chainSource[DataSource.SourceParams.defaultParams, {}, { success = it }]

        assertNotNull(success)

        success?.let {
            assertEquals("merged", it.response)
            assertEquals(MEMORY, it.sourceType)
        }
    }

    @Test
    fun test_successfulCall() {

        val nextSourceParams = DataSource.SourceParams()

        val spiedBooleanSource = spy(MemorySource<Boolean>(true))
        val spiedStringSource = spy(MemorySource<String>("Chained Type"))
        val spyResponseValidator = spy(ChainConstruct.DefaultResponseValidator<Boolean>())

        open class TestConverter : ChainConstruct.ResponseToNextCallConverter<Boolean> {
            override fun provideNextParams(previousResponse: Boolean?, previousParams: DataSource.SourceParams?): DataSource.SourceParams {
                return nextSourceParams
            }

        }

        val responseToNextCallConverter = spy(TestConverter())
        val chainSource = ChainConstruct.builderInstance(spiedBooleanSource, spiedStringSource)
                .responseValidator(spyResponseValidator)
                .responseToNextCallConverter(responseToNextCallConverter)
                .build(MEMORY)

        assertEquals(chainSource.firstDataSource(), spiedBooleanSource)
        assertEquals(chainSource.secondDataSource(), spiedStringSource)

        val success = DataController.Success<String> { }
        val error = DataController.Error { throw RuntimeException("Should not be called ") }

        chainSource[DataSource.SourceParams.defaultParams, error, success]

        // verify we call response to next call converter
        val previousResponseCaptor = ArgumentCaptor.forClass(Boolean::class.java)
        val paramsCaptor = ArgumentCaptor.forClass(DataSource.SourceParams::class.java)
        verify(responseToNextCallConverter)
                .provideNextParams(previousResponseCaptor.capture(), paramsCaptor.capture())
        assertEquals(DataSource.SourceParams.defaultParams, paramsCaptor.value)

        // verify we called get on the source with the next params
        verify(spiedStringSource).get(nextSourceParams, error, success)

        // verify we called the response validator
        val responseCaptor = ArgumentCaptor.forClass(DataControllerResponse::class.java) as ArgumentCaptor<DataControllerResponse<Boolean>>
        verify(spyResponseValidator).isValid(responseCaptor.capture())

        // verify second datasource was called with the next set of params
        verify(spiedStringSource).get(nextSourceParams, error, success)
    }

}
