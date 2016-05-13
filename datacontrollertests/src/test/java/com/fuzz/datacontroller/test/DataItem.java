package com.fuzz.datacontroller.test;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = TestDatabase.class)
public class DataItem extends BaseModel {

    @PrimaryKey
    String id;

    @Column
    String type;

    @Column
    String date;

    @Column
    String data;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public String getData() {
        return data;
    }
}
