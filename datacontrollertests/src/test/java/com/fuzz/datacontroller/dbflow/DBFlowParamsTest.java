package com.fuzz.datacontroller.dbflow;

import com.fuzz.datacontroller.BaseRobolectricUnitTest;
import com.fuzz.datacontroller.DataItem;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DBFlowParamsTest extends BaseRobolectricUnitTest {

    @Test
    public void test_init() {
        BaseDBFlowSource.DBFlowParams<DataItem> params = new BaseDBFlowSource.DBFlowParams<>(SQLite.select().from(DataItem.class));
        assertNotNull(params.getModelQueriable());
    }

}
