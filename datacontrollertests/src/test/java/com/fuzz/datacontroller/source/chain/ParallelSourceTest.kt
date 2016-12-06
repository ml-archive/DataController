package com.fuzz.datacontroller.source.chain

import com.fuzz.datacontroller.DataController
import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.DataResponseError
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.source.DataSource.SourceParams.defaultParams
import com.fuzz.datacontroller.source.DataSource.SourceType.MEMORY
import com.fuzz.datacontroller.source.DataSource.SourceType.NETWORK
import com.fuzz.datacontroller.source.DataSourceCaller
import com.fuzz.datacontroller.source.MemorySource
import com.fuzz.datacontroller.source.chain.ParallelConstruct.ParallelError
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description:
 *
 * @author Andrew Grosner (Fuzz)
 */
class ParallelSourceTest {

    @Test
    fun test_canParallelSimpleCalls() {

        val chainSource = ParallelConstruct.builderInstance(MemorySource<Boolean>(true),
                MemorySource<String>("Chained Type"),
                { parallelResponse, parallelResponse2 ->
                    ParallelConstruct.ParallelResponse(null,
                            DataControllerResponse("", MEMORY))
                })
                .chain<String>(MemorySource<String>("Final Chain Type"))
                .build(MEMORY)

        var success: DataControllerResponse<String>? = null
        chainSource[ParallelConstruct.ParallelParams(defaultParams, defaultParams), {},
                { success = it }]

        Assert.assertNotNull(success)

        success?.let {
            assertEquals("Final Chain Type", it.response)
            assertEquals(MEMORY, it.sourceType)
        }
    }

    @Test
    fun test_canChainParallelWithMergedResponse() {
        val chainSource = ChainConstruct.builderInstance(MemorySource<Boolean>(true),
                MemorySource<String>("Chained Type"))
                .parallel<Int, String>(MemorySource<Int>(),
                        ParallelConstruct.ParallelMerger { parallelResponse, parallelResponse2 ->
                            ParallelConstruct.ParallelResponse(null,
                                    DataControllerResponse("merged", MEMORY))
                        }).build(MEMORY)

        var success: DataControllerResponse<String>? = null
        chainSource[ParallelConstruct.ParallelParams(defaultParams, defaultParams), {},
                { success = it }]

        Assert.assertNotNull(success)

        success?.let {
            assertEquals("merged", it.response)
            assertEquals(MEMORY, it.sourceType)
        }
    }

    @Test
    fun test_canParallelMixedCalls() {

        // second source caller fails, first one succeeded, but we return a failure because we
        // didnt like the combination of responses.
        val chainSource = ParallelConstruct.builderInstance(MemorySource<Boolean>(true),
                object : DataSourceCaller<String> {
                    override fun get(sourceParams: DataSource.SourceParams?,
                                     error: DataController.Error?,
                                     success: DataController.Success<String>?) {
                        error?.onFailure(DataResponseError.Builder(NETWORK, "").build())
                    }

                    override fun cancel() {
                    }

                    override fun sourceType(): DataSource.SourceType {
                        return NETWORK
                    }

                },
                { parallelResponse, parallelResponse2 ->
                    ParallelConstruct.ParallelResponse<String>(
                            ParallelError(parallelResponse.dataResponseError,
                                    parallelResponse2.dataResponseError), null)
                })
                .build(NETWORK)

        var success: DataControllerResponse<String>? = null
        var failure: DataResponseError? = null
        chainSource[ParallelConstruct.ParallelParams(defaultParams, defaultParams), {}, { success = it }]

        assertNull(success)

        failure?.let {
            assertTrue(ParallelError.isParallelError(it))
            val error = ParallelError.getParallelError(it)
            assertTrue(error.isSecondFailure)
            assertFalse(error.isFullFailure)
            assertFalse(error.isFirstFailure)
            assertEquals(DataSource.SourceType.MULTIPLE, it.failedSource())
        }
    }

}