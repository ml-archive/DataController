package com.fuzz.datacontroller.dbflow;

import android.support.annotation.Nullable;

import com.fuzz.datacontroller.DataController;
import com.fuzz.datacontroller.DataControllerResponse;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Description: Loads and saves a single {@link TModel} to/from the database.
 * Provides data from the DB when used.
 */
public class AsyncDBFlowSingleDataSource<TModel extends Model>
        extends BaseAsyncDBFlowDataSource<TModel, TModel> {

    public AsyncDBFlowSingleDataSource(Class<?> databaseClass) {
        super(databaseClass);
    }

    public AsyncDBFlowSingleDataSource(RefreshStrategy<TModel> refreshStrategy,
                                       Class<?> databaseClass) {
        super(refreshStrategy, databaseClass);
    }

    public AsyncDBFlowSingleDataSource(RefreshStrategy<TModel> refreshStrategy,
                                       Class<?> databaseClass,
                                       DBFlowParamsInterface<TModel> defaultParams) {
        super(refreshStrategy, databaseClass, defaultParams);
    }

    public AsyncDBFlowSingleDataSource(Class<?> databaseClass,
                                       DBFlowParamsInterface<TModel> defaultParams) {
        super(databaseClass, defaultParams);
    }

    @Override
    protected void prepareQuery(QueryTransaction.Builder<TModel> queryBuilder,
                                final DataController.Success<TModel> success) {
        queryBuilder.querySingleResult(new QueryTransaction.QueryResultSingleCallback<TModel>() {
            @Override
            public void onSingleQueryResult(QueryTransaction transaction,
                                            @Nullable TModel model) {
                success.onSuccess(new DataControllerResponse<>(model, getSourceType()));
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
