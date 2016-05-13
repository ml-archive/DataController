package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.FirstStorageDataSourceChainer;
import com.fuzz.datacontroller.source.MemoryDataSource;
import com.fuzz.datacontroller.source.TreeMapSingleTypeDataSourceContainer;
import com.fuzz.datacontroller.strategy.TimeBasedRefreshStrategy;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import okhttp3.Call;

/**
 * Description: Calls quiz data.
 */
public class FuzzQuizDataController extends DataController<List<DataItem>> {

    public FuzzQuizDataController() {
        super(new TreeMapSingleTypeDataSourceContainer<List<DataItem>>(),
                new FirstStorageDataSourceChainer<List<DataItem>>());
        registerDataSource(new MemoryDataSource<List<DataItem>>());
        registerDataSource(new DBFlowDataSource<>(DataItem.class));
        registerDataSource(new OkHttpDataSource<List<DataItem>>(
                new TimeBasedRefreshStrategy<List<DataItem>>(5000L),
                new TypeToken<List<DataItem>>() {
                }.getType()) {
            @Override
            protected Call createCall() {
                return NetworkApiManager.get().createGet("http://quizzes.fuzzstaging.com/quizzes/mobile/1/data.json");
            }
        });
    }
}
