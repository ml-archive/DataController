package com.fuzz.datacontroller.dbflow;

import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.List;

/**
 * Description: Loads and saves a {@link List} of {@link TModel} to/from the database.
 * Provides data from the DB when used.
 */
public class AsyncDBFlowListSource<TModel>
        extends BaseAsyncDBFlowSource<TModel, List<TModel>>
        implements ProcessModelTransaction.ProcessModel<TModel> {

    public static <TModel> DataSource.Builder<List<TModel>> builderInstance(Class<TModel> modelClass) {
        AsyncDBFlowListSource<TModel> source = new AsyncDBFlowListSource<>(modelClass);
        return new DataSource.Builder<>(source, DataSource.SourceType.DISK)
                .storage(source);
    }

    private AsyncDBFlowListSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }

    @Override
    public List<TModel> getStoredData(DataSource.SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().queryList();
    }

    @Override
    protected void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                final DataController.Success<List<TModel>> success) {
        queryBuilder.queryListResult(new QueryTransaction.QueryResultListCallback<TModel>() {
            @Override
            public void onListQueryResult(QueryTransaction transaction,
                                          @Nullable List<TModel> tResult) {
                success.onSuccess(new DataControllerResponse<>(tResult, DataSource.SourceType.DISK));
            }
        });
    }

    @Override
    protected void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                DataControllerResponse<List<TModel>> response) {
        processBuilder.addAll(response.getResponse());
    }
}
