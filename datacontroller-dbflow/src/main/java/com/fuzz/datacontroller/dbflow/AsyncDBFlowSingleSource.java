package com.fuzz.datacontroller.dbflow;

import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.fuzz.datacontroller.source.DataSource;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Description: Loads and saves a single {@link TModel} to/from the database.
 * Provides data from the DB when used.
 */
public class AsyncDBFlowSingleSource<TModel>
        extends BaseAsyncDBFlowSource<TModel, TModel> {

    public static <TModel> DataSource.Builder<TModel> builderInstance(Class<TModel> modelClass) {
        AsyncDBFlowSingleSource<TModel> source = new AsyncDBFlowSingleSource<>(modelClass);
        return new DataSource.Builder<>(source, DataSource.SourceType.DISK)
                .storage(source);
    }

    public AsyncDBFlowSingleSource(Class<TModel> tModelClass) {
        super(tModelClass);
    }


    @Override
    public TModel getStoredData(DataSource.SourceParams sourceParams) {
        return getParams(sourceParams).getModelQueriable().querySingle();
    }

    @Override
    protected void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                final DataController.Success<TModel> success) {
        queryBuilder.querySingleResult(new QueryTransaction.QueryResultSingleCallback<TModel>() {
            @Override
            public void onSingleQueryResult(QueryTransaction transaction,
                                            @Nullable TModel model) {
                success.onSuccess(new DataControllerResponse<>(model, DataSource.SourceType.DISK));
            }
        });
    }

    @Override
    protected void prepareStore(ProcessModelTransaction.Builder<TModel> processBuilder,
                                DataControllerResponse<TModel> response) {
        if (response.getResponse() != null) {
            processBuilder.add(response.getResponse());
        }
    }
}
