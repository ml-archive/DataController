package com.fuzz.datacontroller.test.dbflow;

import com.fuzz.datacontroller.dbflow.DBFlowParams;
import com.fuzz.datacontroller.dbflow.DBFlowParamsInterface;
import com.fuzz.datacontroller.source.DataSource;
import com.fuzz.datacontroller.test.BaseRobolectricUnitTest;
import com.fuzz.datacontroller.test.DataItem;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DBFlowParamsTest extends BaseRobolectricUnitTest {

    @Test
    public void test_init() {
        DBFlowParams<DataItem> params = new DBFlowParams<>(SQLite.select().from(DataItem.class));
        assertNotNull(params.getModelQueriable());
    }

    @Test
    public void test_canUseDefaultParams() {
        DBFlowParams<DataItem> defaultParams = new DBFlowParams<>(null);
        DBFlowParamsInterface<DataItem> foundParams = DBFlowParams
                .getParams(defaultParams, new DataSource.SourceParams());
        assertEquals(foundParams, defaultParams);
    }
}
