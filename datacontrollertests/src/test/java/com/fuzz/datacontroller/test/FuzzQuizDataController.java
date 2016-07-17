package com.fuzz.datacontroller.test;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.FirstStorageDataSourceChainer;
import com.fuzz.datacontroller.dbflow.AsyncDBFlowListDataSource;
import com.fuzz.datacontroller.dbflow.DBFlowParams;
import com.fuzz.datacontroller.okhttp.OkHttpDataSource;
import com.fuzz.datacontroller.okhttp.OkHttpParams;
import com.fuzz.datacontroller.source.MemoryDataSource;
import com.fuzz.datacontroller.source.TreeMapSingleTypeDataSourceContainer;
import com.fuzz.datacontroller.strategy.TimeBasedRefreshStrategy;
import com.google.gson.reflect.TypeToken;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Description: Calls quiz data.
 */
public class FuzzQuizDataController extends DataController<List<DataItem>> {

    public FuzzQuizDataController() {
        super(new TreeMapSingleTypeDataSourceContainer<List<DataItem>>(),
                new FirstStorageDataSourceChainer<List<DataItem>>());
        registerDataSource(new MemoryDataSource<List<DataItem>>());
        registerDataSource(new AsyncDBFlowListDataSource<DataItem>(
                new DBFlowParams<>(SQLite.select().from(DataItem.class))) {
            @Override
            protected void prepareQuery(QueryTransaction.Builder<DataItem> queryBuilder,
                                        Success<List<DataItem>> success) {
                super.prepareQuery(queryBuilder, success);
                queryBuilder.runResultCallbacksOnSameThread(true);
            }
        });
        registerDataSource(new OkHttpDataSource<>(
                new TimeBasedRefreshStrategy<List<DataItem>>(5000L),
                new OkHttpDataSource.ResponseConverter<List<DataItem>>() {
                    @Override
                    public List<DataItem> convert(Call call, Response response) {
                        return NetworkApiManager.get().getGson()
                                .fromJson(response.body().charStream(),
                                        new TypeToken<List<DataItem>>() {
                                        }.getType());
                    }
                }, new OkHttpParams(NetworkApiManager.get()
                .createGet("http://quizzes.fuzzstaging.com/quizzes/mobile/1/data.json"))));
    }
}
