package com.fuzz.datacontroller;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Description:
 */
@Database(name = TestDatabase.NAME, version = TestDatabase.VERSION  )
public class TestDatabase {

    public static final String NAME = "Test";

    public static final int VERSION = 1;
}
