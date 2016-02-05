package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.data.MemoryDataStore;
import com.fuzz.datacontroller.strategy.TimebasedRefreshStrategy;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import okhttp3.Call;

/**
 * Description: Calls quiz data.
 */
public class FuzzQuizDataController extends DataController<List<DataItem>> {

    public FuzzQuizDataController() {
        setDataStore(new MemoryDataStore<List<DataItem>>());
        setRefreshStrategy(new TimebasedRefreshStrategy(5000L));
        setDataFetcher(new OkHttpDataFetcher<List<DataItem>>(getDataCallback(), new TypeToken<List<DataItem>>() {
        }) {
            @Override
            protected Call createCall() {
                return NetworkApiManager.get().createGet("http://quizzes.fuzzstaging.com/quizzes/mobile/1/data.json");
            }
        });
    }


}
