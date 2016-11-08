package com.fuzz.datacontroller.test.source.chain

import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.source.MemorySource
import com.fuzz.datacontroller.source.chain.ChainingSource
import com.fuzz.datacontroller.source.chain.DataSourceChain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Description:

 * @author Andrew Grosner (Fuzz)
 */

class ChainingSourceTest {


    @Test
    fun test_canChainSimpleCalls() {

        val chainSource = ChainingSource.builderInstance<String>()
                .chain<String>(MemorySource.builderInstance<String>("Storage1 Response").build(),
                        DataSourceChain.ResponseConverter { "Initial response" })
                .chain<Boolean>(MemorySource.builderInstance<Boolean>(true).build(),
                        DataSourceChain.ResponseConverter { if (it != null && it) "Chained Type" else "Failed Chain Type" })
                .build()

        var success: DataControllerResponse<String>? = null
        chainSource[DataSource.SourceParams.defaultParams, {}, { success = it }]

        assertNotNull(success)

        success?.let {
            assertEquals("Chained Type", it.response)
            assertEquals(DataSource.SourceType.MEMORY, it.sourceType)
        }
    }
}
