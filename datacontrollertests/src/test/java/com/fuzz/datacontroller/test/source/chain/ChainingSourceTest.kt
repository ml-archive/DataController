package com.fuzz.datacontroller.test.source.chain

import com.fuzz.datacontroller.DataControllerResponse
import com.fuzz.datacontroller.source.DataSource
import com.fuzz.datacontroller.source.MemorySource
import com.fuzz.datacontroller.source.chain.ChainConstruct
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

        val chainSource = ChainConstruct.builderInstance(MemorySource<Boolean>(true),
                MemorySource<String>("Chained Type"))
                .chain<String>(MemorySource<String>("Final Chain Type"))
                .build()

        var success: DataControllerResponse<String>? = null
        chainSource[DataSource.SourceParams.defaultParams, {}, { success = it }]

        assertNotNull(success)

        success?.let {
            assertEquals("Final Chain Type", it.response)
            assertEquals(DataSource.SourceType.MEMORY, it.sourceType)
        }
    }

}
